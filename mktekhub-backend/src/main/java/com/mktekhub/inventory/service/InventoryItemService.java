package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.InventoryItemRequest;
import com.mktekhub.inventory.dto.InventoryItemResponse;
import com.mktekhub.inventory.model.InventoryItem;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.InventoryItemRepository;
import com.mktekhub.inventory.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
                .orElseThrow(() -> new RuntimeException("Inventory item not found with id: " + id));
        return InventoryItemResponse.fromEntity(item);
    }

    /**
     * Get inventory item by SKU.
     */
    public InventoryItemResponse getItemBySku(String sku) {
        InventoryItem item = inventoryItemRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Inventory item not found with SKU: " + sku));
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
            throw new RuntimeException("Item with SKU '" + request.getSku() + "' already exists");
        }

        // Validate warehouse exists
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + request.getWarehouseId()));

        // Check warehouse capacity
        if (warehouse.wouldExceedCapacity(request.getQuantity())) {
            throw new RuntimeException("Adding " + request.getQuantity() + " items would exceed warehouse capacity. " +
                    "Available capacity: " + warehouse.getAvailableCapacity());
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
        item.setReorderLevel(request.getReorderLevel());
        item.setWarehouse(warehouse);
        item.setWarrantyEndDate(request.getWarrantyEndDate());
        item.setExpirationDate(request.getExpirationDate());
        item.setBarcode(request.getBarcode());

        InventoryItem saved = inventoryItemRepository.save(item);
        return InventoryItemResponse.fromEntity(saved);
    }

    /**
     * Update an existing inventory item.
     */
    @Transactional
    public InventoryItemResponse updateItem(Long id, InventoryItemRequest request) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory item not found with id: " + id));

        // Check if new SKU conflicts with existing item
        if (!item.getSku().equals(request.getSku()) && inventoryItemRepository.existsBySku(request.getSku())) {
            throw new RuntimeException("Item with SKU '" + request.getSku() + "' already exists");
        }

        // If warehouse is changing, validate new warehouse and capacity
        if (!item.getWarehouse().getId().equals(request.getWarehouseId())) {
            Warehouse newWarehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + request.getWarehouseId()));

            if (newWarehouse.wouldExceedCapacity(request.getQuantity())) {
                throw new RuntimeException("Adding " + request.getQuantity() + " items would exceed warehouse capacity. " +
                        "Available capacity: " + newWarehouse.getAvailableCapacity());
            }

            item.setWarehouse(newWarehouse);
        }

        // Update item fields
        item.setSku(request.getSku());
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setBrand(request.getBrand());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.setReorderLevel(request.getReorderLevel());
        item.setWarrantyEndDate(request.getWarrantyEndDate());
        item.setExpirationDate(request.getExpirationDate());
        item.setBarcode(request.getBarcode());

        InventoryItem updated = inventoryItemRepository.save(item);
        return InventoryItemResponse.fromEntity(updated);
    }

    /**
     * Delete an inventory item.
     */
    @Transactional
    public void deleteItem(Long id) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory item not found with id: " + id));

        inventoryItemRepository.delete(item);
    }

    /**
     * Adjust inventory quantity.
     */
    @Transactional
    public InventoryItemResponse adjustQuantity(Long id, Integer quantityChange) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory item not found with id: " + id));

        int newQuantity = item.getQuantity() + quantityChange;

        if (newQuantity < 0) {
            throw new RuntimeException("Quantity adjustment would result in negative quantity");
        }

        // Check warehouse capacity if increasing quantity
        if (quantityChange > 0 && item.getWarehouse().wouldExceedCapacity(quantityChange)) {
            throw new RuntimeException("Adding " + quantityChange + " items would exceed warehouse capacity. " +
                    "Available capacity: " + item.getWarehouse().getAvailableCapacity());
        }

        item.setQuantity(newQuantity);
        InventoryItem updated = inventoryItemRepository.save(item);
        return InventoryItemResponse.fromEntity(updated);
    }
}
