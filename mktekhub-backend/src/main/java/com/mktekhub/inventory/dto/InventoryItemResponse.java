/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.dto;

import com.mktekhub.inventory.model.InventoryItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** DTO for inventory item response data. */
public class InventoryItemResponse {

  private Long id;
  private String sku;
  private String name;
  private String description;
  private String category;
  private String brand;
  private Integer quantity;
  private BigDecimal unitPrice;
  private BigDecimal volumePerUnit; // Volume in cubic feet per unit
  private BigDecimal totalVolume; // Total volume in cubic feet
  private BigDecimal totalValue;
  private Integer reorderLevel;
  private Long warehouseId;
  private String warehouseName;
  private LocalDate warrantyEndDate;
  private LocalDate expirationDate;
  private String barcode;
  private Boolean isLowStock;
  private Boolean isExpired;
  private Boolean isWarrantyValid;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Constructors
  public InventoryItemResponse() {}

  /** Convert InventoryItem entity to InventoryItemResponse DTO. */
  public static InventoryItemResponse fromEntity(InventoryItem item) {
    InventoryItemResponse response = new InventoryItemResponse();
    response.setId(item.getId());
    response.setSku(item.getSku());
    response.setName(item.getName());
    response.setDescription(item.getDescription());
    response.setCategory(item.getCategory());
    response.setBrand(item.getBrand());
    response.setQuantity(item.getQuantity());
    response.setUnitPrice(item.getUnitPrice());
    response.setVolumePerUnit(item.getVolumePerUnit());
    response.setTotalVolume(item.getTotalVolume());
    response.setTotalValue(item.getTotalValue());
    response.setReorderLevel(item.getReorderLevel());
    response.setWarehouseId(item.getWarehouse().getId());
    response.setWarehouseName(item.getWarehouse().getName());
    response.setWarrantyEndDate(item.getWarrantyEndDate());
    response.setExpirationDate(item.getExpirationDate());
    response.setBarcode(item.getBarcode());
    response.setIsLowStock(item.isLowStock());
    response.setIsExpired(item.isExpired());
    response.setIsWarrantyValid(item.isWarrantyValid());
    response.setCreatedAt(item.getCreatedAt());
    response.setUpdatedAt(item.getUpdatedAt());
    return response;
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

  public BigDecimal getVolumePerUnit() {
    return volumePerUnit;
  }

  public void setVolumePerUnit(BigDecimal volumePerUnit) {
    this.volumePerUnit = volumePerUnit;
  }

  public BigDecimal getTotalVolume() {
    return totalVolume;
  }

  public void setTotalVolume(BigDecimal totalVolume) {
    this.totalVolume = totalVolume;
  }

  public BigDecimal getTotalValue() {
    return totalValue;
  }

  public void setTotalValue(BigDecimal totalValue) {
    this.totalValue = totalValue;
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

  public String getWarehouseName() {
    return warehouseName;
  }

  public void setWarehouseName(String warehouseName) {
    this.warehouseName = warehouseName;
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

  public Boolean getIsLowStock() {
    return isLowStock;
  }

  public void setIsLowStock(Boolean isLowStock) {
    this.isLowStock = isLowStock;
  }

  public Boolean getIsExpired() {
    return isExpired;
  }

  public void setIsExpired(Boolean isExpired) {
    this.isExpired = isExpired;
  }

  public Boolean getIsWarrantyValid() {
    return isWarrantyValid;
  }

  public void setIsWarrantyValid(Boolean isWarrantyValid) {
    this.isWarrantyValid = isWarrantyValid;
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
