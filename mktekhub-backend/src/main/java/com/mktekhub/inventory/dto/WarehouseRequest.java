package com.mktekhub.inventory.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO for creating or updating a warehouse.
 * Max capacity is measured in cubic feet.
 */
public class WarehouseRequest {

    @NotBlank(message = "Warehouse name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    @NotNull(message = "Max capacity is required")
    @DecimalMin(value = "0.01", message = "Max capacity must be at least 0.01 cubic feet")
    private BigDecimal maxCapacity; // Max capacity in cubic feet

    @DecimalMin(value = "0.0", message = "Capacity alert threshold must be at least 0")
    @DecimalMax(value = "100.0", message = "Capacity alert threshold must not exceed 100")
    private BigDecimal capacityAlertThreshold;

    // Constructors
    public WarehouseRequest() {
    }

    public WarehouseRequest(String name, String location, BigDecimal maxCapacity, BigDecimal capacityAlertThreshold) {
        this.name = name;
        this.location = location;
        this.maxCapacity = maxCapacity;
        this.capacityAlertThreshold = capacityAlertThreshold;
    }

    // Getters and Setters
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

    public BigDecimal getCapacityAlertThreshold() {
        return capacityAlertThreshold;
    }

    public void setCapacityAlertThreshold(BigDecimal capacityAlertThreshold) {
        this.capacityAlertThreshold = capacityAlertThreshold;
    }
}
