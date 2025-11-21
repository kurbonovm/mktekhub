/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.BulkStockTransferRequest;
import com.mktekhub.inventory.dto.BulkStockTransferResponse;
import com.mktekhub.inventory.dto.StockTransferRequest;
import com.mktekhub.inventory.dto.StockTransferResponse;
import com.mktekhub.inventory.exception.*;
import com.mktekhub.inventory.model.*;
import com.mktekhub.inventory.repository.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for stock transfer operations between warehouses. */
@Service
public class StockTransferService {

  @Autowired private InventoryItemRepository inventoryItemRepository;
  @Autowired private WarehouseRepository warehouseRepository;
  @Autowired private StockActivityRepository stockActivityRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private EntityManager entityManager; // NEW: Autowire EntityManager

  /**
   * Transfer inventory between warehouses Validates stock availability, warehouse capacity, and
   * creates activity logs
   */
  @Transactional
  public StockTransferResponse transferStock(StockTransferRequest request) {
    // Validate that source and destination warehouses are different
    if (request.getSourceWarehouseId().equals(request.getDestinationWarehouseId())) {
      throw new InvalidOperationException("Source and destination warehouses must be different");
    }

    // Find and validate source warehouse
    Warehouse sourceWarehouse =
        warehouseRepository
            .findById(request.getSourceWarehouseId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Warehouse", "id", request.getSourceWarehouseId()));

    if (!sourceWarehouse.getIsActive()) {
      throw new InvalidOperationException(
          "Source warehouse '" + sourceWarehouse.getName() + "' is not active");
    }

    // Find and validate destination warehouse
    Warehouse destinationWarehouse =
        warehouseRepository
            .findById(request.getDestinationWarehouseId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Warehouse", "id", request.getDestinationWarehouseId()));

    if (!destinationWarehouse.getIsActive()) {
      throw new InvalidOperationException(
          "Destination warehouse '" + destinationWarehouse.getName() + "' is not active");
    }

    // Find inventory item in source warehouse by SKU and warehouse ID
    InventoryItem sourceItem =
        inventoryItemRepository.findBySkuAndWarehouseId(
            request.getItemSku(), request.getSourceWarehouseId());

    // Validate item exists in source warehouse
    if (sourceItem == null) {
      throw new ResourceNotFoundException(
          "InventoryItem with SKU '" + request.getItemSku() + "' not found in source warehouse");
    }

    // Validate sufficient stock in source warehouse
    if (sourceItem.getQuantity() < request.getQuantity()) {
      throw new InsufficientStockException(
          request.getItemSku(), sourceItem.getQuantity(), request.getQuantity());
    }

    // Get current authenticated user
    User currentUser = getCurrentUser();

    try {
      // Signal the PostgreSQL trigger to skip automatic ADJUSTMENT logging
      entityManager
          .createNativeQuery("SET inventory.transfer_in_progress TO 'true'")
          .executeUpdate();

      // Update warehouse capacities (volume-based)
      // Calculate volume being transferred
      BigDecimal volumePerUnit =
          sourceItem.getVolumePerUnit() != null
              ? sourceItem.getVolumePerUnit()
              : BigDecimal.ONE; // Use ONE instead of ZERO for safety
      BigDecimal volumeTransferred =
          volumePerUnit.multiply(BigDecimal.valueOf(request.getQuantity()));

      // Remove volume from source warehouse
      sourceWarehouse.setCurrentCapacity(
          sourceWarehouse.getCurrentCapacity().subtract(volumeTransferred));
      warehouseRepository.save(sourceWarehouse);

      // Add volume to destination warehouse
      destinationWarehouse.setCurrentCapacity(
          destinationWarehouse.getCurrentCapacity().add(volumeTransferred));
      warehouseRepository.save(destinationWarehouse);

      // Record previous quantities
      int previousSourceQuantity = sourceItem.getQuantity();

      // Update source item quantity
      sourceItem.setQuantity(sourceItem.getQuantity() - request.getQuantity());

      // Check if item already exists in destination warehouse
      InventoryItem destinationItem =
          inventoryItemRepository.findBySkuAndWarehouseId(
              request.getItemSku(), request.getDestinationWarehouseId());

      int previousDestinationQuantity = 0;

      if (destinationItem != null) {
        // Item exists in destination - update quantity
        previousDestinationQuantity = destinationItem.getQuantity();
        destinationItem.setQuantity(destinationItem.getQuantity() + request.getQuantity());
      } else {
        // Item doesn't exist in destination - create new entry
        // NOTE: Copying all necessary attributes from source
        destinationItem = new InventoryItem();
        destinationItem.setSku(sourceItem.getSku());
        destinationItem.setName(sourceItem.getName());
        destinationItem.setDescription(sourceItem.getDescription());
        destinationItem.setCategory(sourceItem.getCategory());
        destinationItem.setBrand(sourceItem.getBrand());
        destinationItem.setQuantity(request.getQuantity());
        destinationItem.setUnitPrice(sourceItem.getUnitPrice());
        destinationItem.setVolumePerUnit(sourceItem.getVolumePerUnit());
        destinationItem.setReorderLevel(sourceItem.getReorderLevel());
        destinationItem.setWarrantyEndDate(sourceItem.getWarrantyEndDate());
        destinationItem.setExpirationDate(sourceItem.getExpirationDate());
        destinationItem.setBarcode(sourceItem.getBarcode());
        destinationItem.setWarehouse(destinationWarehouse);
      }

      // Save updated inventory items (Triggers now skip logging due to session variable)
      inventoryItemRepository.save(sourceItem);
      inventoryItemRepository.save(destinationItem);

      // Log the SOURCE DEPARTURE (Quantity DECREASE)
      StockActivity sourceActivity = new StockActivity();
      sourceActivity.setItem(sourceItem);
      sourceActivity.setItemSku(request.getItemSku());
      sourceActivity.setActivityType(ActivityType.TRANSFER);
      sourceActivity.setQuantityChange(-request.getQuantity()); // Negative change for departure
      sourceActivity.setPreviousQuantity(previousSourceQuantity);
      sourceActivity.setNewQuantity(sourceItem.getQuantity());
      sourceActivity.setTimestamp(LocalDateTime.now());
      sourceActivity.setPerformedBy(currentUser);
      sourceActivity.setSourceWarehouse(sourceWarehouse);
      sourceActivity.setDestinationWarehouse(destinationWarehouse);
      sourceActivity.setNotes(
          request.getNotes() != null
              ? request.getNotes()
              : String.format(
                  "Transfer OUT: %d units from %s to %s",
                  request.getQuantity(),
                  sourceWarehouse.getName(),
                  destinationWarehouse.getName()));
      stockActivityRepository.save(sourceActivity);

      // Log the DESTINATION ARRIVAL (Quantity INCREASE)
      StockActivity destinationActivity = new StockActivity();
      destinationActivity.setItem(destinationItem);
      destinationActivity.setItemSku(request.getItemSku());
      destinationActivity.setActivityType(ActivityType.TRANSFER); // Can also be RECEIVE
      destinationActivity.setQuantityChange(request.getQuantity()); // Positive change for arrival
      destinationActivity.setPreviousQuantity(previousDestinationQuantity);
      destinationActivity.setNewQuantity(destinationItem.getQuantity());
      destinationActivity.setTimestamp(LocalDateTime.now());
      destinationActivity.setPerformedBy(currentUser);
      destinationActivity.setSourceWarehouse(sourceWarehouse);
      destinationActivity.setDestinationWarehouse(destinationWarehouse);
      destinationActivity.setNotes(
          request.getNotes() != null
              ? request.getNotes()
              : String.format(
                  "Transfer IN: %d units from %s to %s",
                  request.getQuantity(),
                  sourceWarehouse.getName(),
                  destinationWarehouse.getName()));

      StockActivity savedActivity =
          stockActivityRepository.save(
              destinationActivity); // Use the destination log for the final response

      // Return response
      return StockTransferResponse.fromEntity(savedActivity);
    } finally {
      // Clear the session variable regardless of success or failure
      entityManager.createNativeQuery("RESET inventory.transfer_in_progress").executeUpdate();
    }
  }

  /**
   * Perform bulk stock transfers Processes multiple transfers and returns success/failure results
   * Does not use transactions - continues processing even if some transfers fail
   */
  @Transactional
  public BulkStockTransferResponse bulkTransferStock(BulkStockTransferRequest request) {
    List<StockTransferResponse> successResults = new ArrayList<>();
    List<BulkStockTransferResponse.TransferError> errors = new ArrayList<>();

    List<StockTransferRequest> transfers = request.getTransfers();
    System.out.println("Transfers received: " + transfers.size());
    int totalTransfers = transfers.size();

    for (int i = 0; i < transfers.size(); i++) {
      StockTransferRequest transferRequest = transfers.get(i);
      try {
        StockTransferResponse response = transferStock(transferRequest);
        successResults.add(response);
      } catch (Exception e) {
        BulkStockTransferResponse.TransferError error =
            new BulkStockTransferResponse.TransferError(
                i, transferRequest.getItemSku(), e.getMessage());
        errors.add(error);
      }
    }

    int successfulTransfers = successResults.size();
    int failedTransfers = errors.size();

    return new BulkStockTransferResponse(
        totalTransfers, successfulTransfers, failedTransfers, successResults, errors);
  }

  /** Get the currently authenticated user */
  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
  }
}
