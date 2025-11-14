package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.InventoryItemRequest;
import com.mktekhub.inventory.dto.InventoryItemResponse;
import com.mktekhub.inventory.exception.DuplicateResourceException;
import com.mktekhub.inventory.exception.InvalidOperationException;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.exception.WarehouseCapacityExceededException;
import com.mktekhub.inventory.model.ActivityType;
import com.mktekhub.inventory.model.InventoryItem;
import com.mktekhub.inventory.model.StockActivity;
import com.mktekhub.inventory.model.User;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.InventoryItemRepository;
import com.mktekhub.inventory.repository.StockActivityRepository;
import com.mktekhub.inventory.repository.UserRepository;
import com.mktekhub.inventory.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for inventory item management operations.
 */
@Service
public class InventoryItemService {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private StockActivityRepository stockActivityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityLoggerService activityLoggerService;

    /**
     * Get all inventory items.
     */
    public List<InventoryItemResponse> getAllItems() {
        return inventoryItemRepository.findAll().stream()
                .map(InventoryItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get inventory item by ID.
     */
    public InventoryItemResponse getItemById(Long id) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", id));
        return InventoryItemResponse.fromEntity(item);
    }

    /**
     * Get inventory item by SKU.
     */
    public InventoryItemResponse getItemBySku(String sku) {
        InventoryItem item = inventoryItemRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "sku", sku));
        return InventoryItemResponse.fromEntity(item);
    }

    /**
     * Get all items in a specific warehouse.
     */
    public List<InventoryItemResponse> getItemsByWarehouse(Long warehouseId) {
        return inventoryItemRepository.findByWarehouseId(warehouseId).stream()
                .map(InventoryItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all items by category.
     */
    public List<InventoryItemResponse> getItemsByCategory(String category) {
        return inventoryItemRepository.findByCategory(category).stream()
                .map(InventoryItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all low stock items.
     */
    public List<InventoryItemResponse> getLowStockItems() {
        return inventoryItemRepository.findLowStockItems().stream()
                .map(InventoryItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all expired items.
     */
    public List<InventoryItemResponse> getExpiredItems() {
        return inventoryItemRepository.findByExpirationDateBefore(LocalDate.now()).stream()
                .map(InventoryItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get items expiring within specified days.
     */
    public List<InventoryItemResponse> getItemsExpiringSoon(int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(days);
        return inventoryItemRepository.findByExpirationDateBetween(startDate, endDate).stream()
                .map(InventoryItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Create a new inventory item.
     */
    @Transactional
    public InventoryItemResponse createItem(InventoryItemRequest request) {
        // Check if SKU already exists
        if (inventoryItemRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("InventoryItem", "sku", request.getSku());
        }

        // Validate warehouse exists
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", request.getWarehouseId()));

        // Calculate total volume for this item
        BigDecimal volumePerUnit = request.getVolumePerUnit() != null ? request.getVolumePerUnit() : BigDecimal.ZERO;
        BigDecimal totalVolume = volumePerUnit.multiply(BigDecimal.valueOf(request.getQuantity()));

        // Check warehouse capacity
        if (warehouse.wouldExceedCapacity(totalVolume)) {
            throw new WarehouseCapacityExceededException(
                    warehouse.getName(),
                    warehouse.getAvailableCapacity(),
                    totalVolume);
        }

        // Create inventory item
        InventoryItem item = new InventoryItem();
        item.setSku(request.getSku());
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setBrand(request.getBrand());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.setVolumePerUnit(volumePerUnit);
        item.setReorderLevel(request.getReorderLevel());
        item.setWarehouse(warehouse);
        item.setWarrantyEndDate(request.getWarrantyEndDate());
        item.setExpirationDate(request.getExpirationDate());
        item.setBarcode(request.getBarcode());

        InventoryItem saved = inventoryItemRepository.save(item);

        // Update warehouse current capacity (volume-based)
        warehouse.setCurrentCapacity(warehouse.getCurrentCapacity().add(totalVolume));
        warehouseRepository.save(warehouse);

        return InventoryItemResponse.fromEntity(saved);
    }

    /**
     * Update an existing inventory item.
     */
    @Transactional
    public InventoryItemResponse updateItem(Long id, InventoryItemRequest request) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", id));

        // Check if new SKU conflicts with existing item
        if (!item.getSku().equals(request.getSku()) && inventoryItemRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("InventoryItem", "sku", request.getSku());
        }

        // Store old values for capacity adjustment and activity logging
        Warehouse oldWarehouse = item.getWarehouse();
        BigDecimal oldVolume = item.getTotalVolume();
        int oldQuantity = item.getQuantity();

        // Calculate new volume
        BigDecimal newVolumePerUnit = request.getVolumePerUnit() != null ? request.getVolumePerUnit() : BigDecimal.ZERO;
        BigDecimal newTotalVolume = newVolumePerUnit.multiply(BigDecimal.valueOf(request.getQuantity()));

        // If warehouse is changing, validate new warehouse and capacity
        if (!item.getWarehouse().getId().equals(request.getWarehouseId())) {
            Warehouse newWarehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", request.getWarehouseId()));

            if (newWarehouse.wouldExceedCapacity(newTotalVolume)) {
                throw new WarehouseCapacityExceededException(
                        newWarehouse.getName(),
                        newWarehouse.getAvailableCapacity(),
                        newTotalVolume);
            }

            // Update warehouse capacities: remove from old, add to new
            oldWarehouse.setCurrentCapacity(oldWarehouse.getCurrentCapacity().subtract(oldVolume));
            warehouseRepository.save(oldWarehouse);

            newWarehouse.setCurrentCapacity(newWarehouse.getCurrentCapacity().add(newTotalVolume));
            warehouseRepository.save(newWarehouse);

            item.setWarehouse(newWarehouse);
        } else if (oldVolume.compareTo(newTotalVolume) != 0) {
            // Same warehouse but volume changed (due to quantity or volumePerUnit change)
            BigDecimal volumeDifference = newTotalVolume.subtract(oldVolume);

            // Validate capacity if increasing volume
            if (volumeDifference.compareTo(BigDecimal.ZERO) > 0 && oldWarehouse.wouldExceedCapacity(volumeDifference)) {
                throw new WarehouseCapacityExceededException(
                        oldWarehouse.getName(),
                        oldWarehouse.getAvailableCapacity(),
                        volumeDifference);
            }

            oldWarehouse.setCurrentCapacity(oldWarehouse.getCurrentCapacity().add(volumeDifference));
            warehouseRepository.save(oldWarehouse);
        }

        // Update item fields
        item.setSku(request.getSku());
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setBrand(request.getBrand());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.setVolumePerUnit(newVolumePerUnit);
        item.setReorderLevel(request.getReorderLevel());
        item.setWarrantyEndDate(request.getWarrantyEndDate());
        item.setExpirationDate(request.getExpirationDate());
        item.setBarcode(request.getBarcode());

        InventoryItem updated = inventoryItemRepository.save(item);

        // Log UPDATE activity
        User currentUser = getCurrentUser();
        StockActivity activity = new StockActivity();
        activity.setItem(updated);
        activity.setItemSku(updated.getSku());
        activity.setActivityType(ActivityType.UPDATE);
        activity.setQuantityChange(updated.getQuantity() - oldQuantity);
        activity.setPreviousQuantity(oldQuantity);
        activity.setNewQuantity(updated.getQuantity());
        activity.setPerformedBy(currentUser);

        // Build notes describing what changed
        List<String> changes = new ArrayList<>();
        if (!oldWarehouse.getId().equals(updated.getWarehouse().getId())) {
            changes.add("Warehouse changed from '" + oldWarehouse.getName() + "' to '" + updated.getWarehouse().getName() + "'");
            activity.setSourceWarehouse(oldWarehouse);
            activity.setDestinationWarehouse(updated.getWarehouse());
        }
        if (oldVolume.compareTo(newTotalVolume) != 0) {
            changes.add("Volume changed from " + oldVolume + " to " + newTotalVolume + " ft³");
        }

        if (!changes.isEmpty()) {
            activity.setNotes("Item updated: " + String.join("; ", changes));
        } else {
            activity.setNotes("Item details updated");
        }

        stockActivityRepository.save(activity);

        return InventoryItemResponse.fromEntity(updated);
    }

    /**
     * Delete an inventory item.
     */
    @Transactional
    public void deleteItem(Long id) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", id));

        // Update warehouse current capacity (volume-based)
        Warehouse warehouse = item.getWarehouse();
        warehouse.setCurrentCapacity(warehouse.getCurrentCapacity().subtract(item.getTotalVolume()));
        warehouseRepository.save(warehouse);

        inventoryItemRepository.delete(item);
    }

    /**
     * Adjust inventory quantity.
     */
    @Transactional
    public InventoryItemResponse adjustQuantity(Long id, Integer quantityChange) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "id", id));

        int oldQuantity = item.getQuantity();
        int newQuantity = oldQuantity + quantityChange;

        if (newQuantity < 0) {
            throw new InvalidOperationException("Quantity adjustment would result in negative quantity");
        }

        // Calculate volume change
        BigDecimal volumePerUnit = item.getVolumePerUnit() != null ? item.getVolumePerUnit() : BigDecimal.ZERO;
        BigDecimal volumeChange = volumePerUnit.multiply(BigDecimal.valueOf(quantityChange));

        // Check warehouse capacity if increasing volume
        if (volumeChange.compareTo(BigDecimal.ZERO) > 0 && item.getWarehouse().wouldExceedCapacity(volumeChange)) {
            throw new WarehouseCapacityExceededException(
                    item.getWarehouse().getName(),
                    item.getWarehouse().getAvailableCapacity(),
                    volumeChange);
        }

        item.setQuantity(newQuantity);
        InventoryItem updated = inventoryItemRepository.save(item);

        // Update warehouse current capacity (volume-based)
        Warehouse warehouse = item.getWarehouse();
        warehouse.setCurrentCapacity(warehouse.getCurrentCapacity().add(volumeChange));
        warehouseRepository.save(warehouse);

        // Log ADJUSTMENT activity
        User currentUser = getCurrentUser();
        activityLoggerService.logAdjustment(updated, quantityChange, oldQuantity, newQuantity, currentUser);

        return InventoryItemResponse.fromEntity(updated);
    }

    /**
     * Get the currently authenticated user from Spring Security context.
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }
}
