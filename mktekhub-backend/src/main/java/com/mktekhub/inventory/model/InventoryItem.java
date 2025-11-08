package com.mktekhub.inventory.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an inventory item in the mktekhub inventory management system.
 * Items are stored in warehouses and tracked through stock activities.
 */
@Entity
@Table(name = "inventory_item")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String category;

    @Column(length = 100)
    private String brand;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "warranty_end_date")
    private LocalDate warrantyEndDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(length = 100)
    private String barcode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockActivity> stockActivities = new ArrayList<>();

    // Constructors
    public InventoryItem() {
    }

    public InventoryItem(String sku, String name, Integer quantity, Warehouse warehouse) {
        this.sku = sku;
        this.name = name;
        this.quantity = quantity;
        this.warehouse = warehouse;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public LocalDate getWarrantyEndDate() {
        return warrantyEndDate;
    }

    public void setWarrantyEndDate(LocalDate warrantyEndDate) {
        this.warrantyEndDate = warrantyEndDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<StockActivity> getStockActivities() {
        return stockActivities;
    }

    public void setStockActivities(List<StockActivity> stockActivities) {
        this.stockActivities = stockActivities;
    }

    // Business logic methods

    /**
     * Checks if the item stock is at or below the reorder level.
     * @return true if stock is low, false otherwise
     */
    public boolean isLowStock() {
        return reorderLevel != null && quantity <= reorderLevel;
    }

    /**
     * Checks if the item is expiring within the specified number of days.
     * @param days Number of days to check
     * @return true if item is expiring soon, false otherwise
     */
    public boolean isExpiringSoon(int days) {
        if (expirationDate == null) {
            return false;
        }
        LocalDate threshold = LocalDate.now().plusDays(days);
        return expirationDate.isBefore(threshold) || expirationDate.isEqual(threshold);
    }

    /**
     * Checks if the item has expired.
     * @return true if item is expired, false otherwise
     */
    public boolean isExpired() {
        return expirationDate != null && expirationDate.isBefore(LocalDate.now());
    }

    /**
     * Calculates the total value of this inventory item.
     * @return Total value (quantity * unit price)
     */
    public BigDecimal getTotalValue() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Checks if the warranty is still valid.
     * @return true if warranty is valid, false otherwise
     */
    public boolean isWarrantyValid() {
        return warrantyEndDate != null && warrantyEndDate.isAfter(LocalDate.now());
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                ", warehouse=" + (warehouse != null ? warehouse.getName() : "null") +
                ", unitPrice=" + unitPrice +
                ", totalValue=" + getTotalValue() +
                '}';
    }
}
