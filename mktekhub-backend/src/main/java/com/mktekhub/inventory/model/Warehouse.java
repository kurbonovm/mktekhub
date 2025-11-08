package com.mktekhub.inventory.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a warehouse in the mktekhub inventory management system.
 * Warehouses store inventory items and have capacity limits and alert thresholds.
 */
@Entity
@Table(name = "warehouse")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name = "current_capacity", nullable = false)
    private Integer currentCapacity = 0;

    @Column(name = "capacity_alert_threshold", precision = 5, scale = 2)
    private BigDecimal capacityAlertThreshold = new BigDecimal("80.00");

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<InventoryItem> inventoryItems = new ArrayList<>();

    // Constructors
    public Warehouse() {
    }

    public Warehouse(String name, String location, Integer maxCapacity) {
        this.name = name;
        this.location = location;
        this.maxCapacity = maxCapacity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Integer getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(Integer currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public BigDecimal getCapacityAlertThreshold() {
        return capacityAlertThreshold;
    }

    public void setCapacityAlertThreshold(BigDecimal capacityAlertThreshold) {
        this.capacityAlertThreshold = capacityAlertThreshold;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    public List<InventoryItem> getInventoryItems() {
        return inventoryItems;
    }

    public void setInventoryItems(List<InventoryItem> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }

    // Business logic methods

    /**
     * Calculates the current utilization percentage of the warehouse.
     * @return Utilization percentage as BigDecimal
     */
    public BigDecimal getUtilizationPercentage() {
        if (maxCapacity == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(currentCapacity)
                .divide(BigDecimal.valueOf(maxCapacity), 2, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Checks if the warehouse capacity alert should be triggered.
     * @return true if utilization exceeds threshold, false otherwise
     */
    public boolean isAlertTriggered() {
        return getUtilizationPercentage().compareTo(capacityAlertThreshold) >= 0;
    }

    /**
     * Checks if adding the specified quantity would exceed warehouse capacity.
     * @param quantity Quantity to check
     * @return true if capacity would be exceeded, false otherwise
     */
    public boolean wouldExceedCapacity(Integer quantity) {
        return (currentCapacity + quantity) > maxCapacity;
    }

    /**
     * Gets the available capacity in the warehouse.
     * @return Available capacity as Integer
     */
    public Integer getAvailableCapacity() {
        return maxCapacity - currentCapacity;
    }

    @Override
    public String toString() {
        return "Warehouse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", maxCapacity=" + maxCapacity +
                ", currentCapacity=" + currentCapacity +
                ", utilizationPercentage=" + getUtilizationPercentage() + "%" +
                ", isActive=" + isActive +
                '}';
    }
}
