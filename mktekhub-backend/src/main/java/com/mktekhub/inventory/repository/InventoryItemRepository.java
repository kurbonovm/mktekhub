package com.mktekhub.inventory.repository;

import com.mktekhub.inventory.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for InventoryItem entity.
 * Provides database operations for inventory management.
 */
@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    /**
     * Find an inventory item by SKU.
     */
    Optional<InventoryItem> findBySku(String sku);

    /**
     * Find all items in a specific warehouse.
     */
    List<InventoryItem> findByWarehouseId(Long warehouseId);

    /**
     * Find all items by category (case-insensitive).
     */
    @Query("SELECT i FROM InventoryItem i WHERE LOWER(i.category) = LOWER(?1)")
    List<InventoryItem> findByCategory(String category);

    /**
     * Find all items with low stock (quantity <= reorder level).
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.reorderLevel IS NOT NULL AND i.quantity <= i.reorderLevel")
    List<InventoryItem> findLowStockItems();

    /**
     * Find all items expiring before a specific date.
     */
    List<InventoryItem> findByExpirationDateBefore(LocalDate date);

    /**
     * Find all items expiring between two dates.
     */
    List<InventoryItem> findByExpirationDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Check if an item exists by SKU.
     */
    boolean existsBySku(String sku);

    /**
     * Find an inventory item by SKU and warehouse ID.
     * Used for checking if an item already exists in a warehouse during transfers.
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.sku = ?1 AND i.warehouse.id = ?2")
    InventoryItem findBySkuAndWarehouseId(String sku, Long warehouseId);
}
