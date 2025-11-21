/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** DTO for creating or updating an inventory item. */
public class InventoryItemRequest {

  @NotBlank(message = "SKU is required")
  @Size(max = 50, message = "SKU must not exceed 50 characters")
  private String sku;

  @NotBlank(message = "Item name is required")
  @Size(max = 255, message = "Name must not exceed 255 characters")
  private String name;

  @Size(max = 1000, message = "Description must not exceed 1000 characters")
  private String description;

  @Size(max = 100, message = "Category must not exceed 100 characters")
  private String category;

  @Size(max = 100, message = "Brand must not exceed 100 characters")
  private String brand;

  @NotNull(message = "Quantity is required")
  @Min(value = 0, message = "Quantity must be at least 0")
  private Integer quantity;

  @Min(value = 0, message = "Unit price must be at least 0")
  private BigDecimal unitPrice;

  @Min(value = 0, message = "Volume per unit must be at least 0")
  private BigDecimal volumePerUnit; // Volume in cubic feet per unit

  @Min(value = 0, message = "Reorder level must be at least 0")
  private Integer reorderLevel;

  @NotNull(message = "Warehouse ID is required")
  private Long warehouseId;

  private LocalDate warrantyEndDate;

  private LocalDate expirationDate;

  @Size(max = 100, message = "Barcode must not exceed 100 characters")
  private String barcode;

  // Constructors
  public InventoryItemRequest() {}

  // Getters and Setters
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

  public BigDecimal getVolumePerUnit() {
    return volumePerUnit;
  }

  public void setVolumePerUnit(BigDecimal volumePerUnit) {
    this.volumePerUnit = volumePerUnit;
  }

  public Integer getReorderLevel() {
    return reorderLevel;
  }

  public void setReorderLevel(Integer reorderLevel) {
    this.reorderLevel = reorderLevel;
  }

  public Long getWarehouseId() {
    return warehouseId;
  }

  public void setWarehouseId(Long warehouseId) {
    this.warehouseId = warehouseId;
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
}
