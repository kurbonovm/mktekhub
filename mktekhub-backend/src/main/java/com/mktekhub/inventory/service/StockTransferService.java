package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.BulkStockTransferRequest;
import com.mktekhub.inventory.dto.BulkStockTransferResponse;
import com.mktekhub.inventory.dto.StockTransferRequest;
import com.mktekhub.inventory.dto.StockTransferResponse;
import com.mktekhub.inventory.exception.*;
import com.mktekhub.inventory.model.*;
import com.mktekhub.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for stock transfer operations between warehouses.
 */
@Service
public class StockTransferService {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private StockActivityRepository stockActivityRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Transfer inventory between warehouses
     * Validates stock availability, warehouse capacity, and creates activity logs
     */
    @Transactional
    public StockTransferResponse transferStock(StockTransferRequest request) {
        // 1. Validate that source and destination warehouses are different
        if (request.getSourceWarehouseId().equals(request.getDestinationWarehouseId())) {
            throw new InvalidOperationException(
                "Source and destination warehouses must be different");
        }

        // 2. Find and validate source warehouse
        Warehouse sourceWarehouse = warehouseRepository.findById(request.getSourceWarehouseId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Warehouse", "id", request.getSourceWarehouseId()));

        if (!sourceWarehouse.getIsActive()) {
            throw new InvalidOperationException(
                "Source warehouse '" + sourceWarehouse.getName() + "' is not active");
        }

        // 3. Find and validate destination warehouse
        Warehouse destinationWarehouse = warehouseRepository.findById(request.getDestinationWarehouseId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Warehouse", "id", request.getDestinationWarehouseId()));

        if (!destinationWarehouse.getIsActive()) {
            throw new InvalidOperationException(
                "Destination warehouse '" + destinationWarehouse.getName() + "' is not active");
        }

        // 4. Find inventory item in source warehouse by SKU and warehouse ID
        InventoryItem sourceItem = inventoryItemRepository
            .findBySkuAndWarehouseId(request.getItemSku(), request.getSourceWarehouseId());

        // 5. Validate item exists in source warehouse
        if (sourceItem == null) {
            throw new ResourceNotFoundException(
                "InventoryItem with SKU '" + request.getItemSku() +
                "' not found in source warehouse");
        }

        // 6. Validate sufficient stock in source warehouse
        if (sourceItem.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                request.getItemSku(),
                sourceItem.getQuantity(),
                request.getQuantity());
        }

        // Note: Warehouse capacity is not updated during transfers since items are just
        // moving between locations. Capacity tracking is only relevant for add/remove operations.

        // 7. Get current authenticated user
        User currentUser = getCurrentUser();

        // 8. Record previous quantities
        int previousSourceQuantity = sourceItem.getQuantity();

        // 9. Update source item quantity
        sourceItem.setQuantity(sourceItem.getQuantity() - request.getQuantity());

        // 10. Check if item already exists in destination warehouse
        InventoryItem destinationItem = inventoryItemRepository
            .findBySkuAndWarehouseId(request.getItemSku(), request.getDestinationWarehouseId());

        int previousDestinationQuantity = 0;

        if (destinationItem != null) {
            // Item exists in destination - update quantity
            previousDestinationQuantity = destinationItem.getQuantity();
            destinationItem.setQuantity(destinationItem.getQuantity() + request.getQuantity());
        } else {
            // Item doesn't exist in destination - create new entry
            destinationItem = new InventoryItem();
            destinationItem.setSku(sourceItem.getSku());
            destinationItem.setName(sourceItem.getName());
            destinationItem.setDescription(sourceItem.getDescription());
            destinationItem.setCategory(sourceItem.getCategory());
            destinationItem.setBrand(sourceItem.getBrand());
            destinationItem.setQuantity(request.getQuantity());
            destinationItem.setUnitPrice(sourceItem.getUnitPrice());
            destinationItem.setReorderLevel(sourceItem.getReorderLevel());
            destinationItem.setWarrantyEndDate(sourceItem.getWarrantyEndDate());
            destinationItem.setExpirationDate(sourceItem.getExpirationDate());
            destinationItem.setBarcode(sourceItem.getBarcode());
            destinationItem.setWarehouse(destinationWarehouse);
        }

        // 11. Save updated inventory items
        inventoryItemRepository.save(sourceItem);
        inventoryItemRepository.save(destinationItem);

        // 12. Create stock activity record for the transfer
        StockActivity activity = new StockActivity();
        activity.setItem(destinationItem);
        activity.setItemSku(request.getItemSku());
        activity.setActivityType(ActivityType.TRANSFER);
        activity.setQuantityChange(request.getQuantity());
        activity.setPreviousQuantity(previousDestinationQuantity);
        activity.setNewQuantity(destinationItem.getQuantity());
        activity.setTimestamp(LocalDateTime.now());
        activity.setPerformedBy(currentUser);
        activity.setSourceWarehouse(sourceWarehouse);
        activity.setDestinationWarehouse(destinationWarehouse);
        activity.setNotes(request.getNotes() != null ? request.getNotes() :
            String.format("Transferred %d units from %s to %s",
                request.getQuantity(),
                sourceWarehouse.getName(),
                destinationWarehouse.getName()));

        StockActivity savedActivity = stockActivityRepository.save(activity);

        // 13. Return response
        return StockTransferResponse.fromEntity(savedActivity);
    }

    /**
     * Perform bulk stock transfers
     * Processes multiple transfers and returns success/failure results
     * Does not use transactions - continues processing even if some transfers fail
     */
    public BulkStockTransferResponse bulkTransferStock(BulkStockTransferRequest request) {
        List<StockTransferResponse> successResults = new ArrayList<>();
        List<BulkStockTransferResponse.TransferError> errors = new ArrayList<>();

        List<StockTransferRequest> transfers = request.getTransfers();
        int totalTransfers = transfers.size();

        for (int i = 0; i < transfers.size(); i++) {
            StockTransferRequest transferRequest = transfers.get(i);
            try {
                StockTransferResponse response = transferStock(transferRequest);
                successResults.add(response);
            } catch (Exception e) {
                BulkStockTransferResponse.TransferError error =
                    new BulkStockTransferResponse.TransferError(
                        i,
                        transferRequest.getItemSku(),
                        e.getMessage()
                    );
                errors.add(error);
            }
        }

        int successfulTransfers = successResults.size();
        int failedTransfers = errors.size();

        return new BulkStockTransferResponse(
            totalTransfers,
            successfulTransfers,
            failedTransfers,
            successResults,
            errors
        );
    }

    /**
     * Get the currently authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }
}
