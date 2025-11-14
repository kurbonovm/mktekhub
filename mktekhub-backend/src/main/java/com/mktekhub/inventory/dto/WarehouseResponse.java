package com.mktekhub.inventory.dto;

import com.mktekhub.inventory.model.Warehouse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for warehouse response data.
 * Capacity values are in cubic feet.
 */
public class WarehouseResponse {

    private Long id;
    private String name;
    private String location;
    private BigDecimal maxCapacity; // Max capacity in cubic feet
    private BigDecimal currentCapacity; // Current capacity in cubic feet
    private BigDecimal capacityAlertThreshold;
    private BigDecimal utilizationPercentage;
    private Boolean isAlertTriggered;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public WarehouseResponse() {
    }

    /**
     * Convert Warehouse entity to WarehouseResponse DTO.
     */
    public static WarehouseResponse fromEntity(Warehouse warehouse) {
        WarehouseResponse response = new WarehouseResponse();
        response.setId(warehouse.getId());
        response.setName(warehouse.getName());
        response.setLocation(warehouse.getLocation());
        response.setMaxCapacity(warehouse.getMaxCapacity());
        response.setCurrentCapacity(warehouse.getCurrentCapacity());
        response.setCapacityAlertThreshold(warehouse.getCapacityAlertThreshold());
        response.setUtilizationPercentage(warehouse.getUtilizationPercentage());
        response.setIsAlertTriggered(warehouse.isAlertTriggered());
        response.setIsActive(warehouse.getIsActive());
        response.setCreatedAt(warehouse.getCreatedAt());
        response.setUpdatedAt(warehouse.getUpdatedAt());
        return response;
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

    public BigDecimal getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(BigDecimal maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public BigDecimal getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(BigDecimal currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public BigDecimal getCapacityAlertThreshold() {
        return capacityAlertThreshold;
    }

    public void setCapacityAlertThreshold(BigDecimal capacityAlertThreshold) {
        this.capacityAlertThreshold = capacityAlertThreshold;
    }

    public BigDecimal getUtilizationPercentage() {
        return utilizationPercentage;
    }

    public void setUtilizationPercentage(BigDecimal utilizationPercentage) {
        this.utilizationPercentage = utilizationPercentage;
    }

    public Boolean getIsAlertTriggered() {
        return isAlertTriggered;
    }

    public void setIsAlertTriggered(Boolean isAlertTriggered) {
        this.isAlertTriggered = isAlertTriggered;
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
}
