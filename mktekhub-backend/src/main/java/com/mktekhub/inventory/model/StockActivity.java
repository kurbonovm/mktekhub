/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entity representing a stock activity in the mktekhub inventory management system. Tracks all
 * inventory movements, transfers, and adjustments for audit purposes.
 */
@Entity
@Table(name = "stock_activity")
public class StockActivity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id", nullable = false)
  private InventoryItem item;

  @Column(name = "item_sku", nullable = false, length = 50)
  private String itemSku;

  @Enumerated(EnumType.STRING)
  @Column(name = "activity_type", nullable = false)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private ActivityType activityType;

  @Column(name = "quantity_change", nullable = false)
  private Integer quantityChange;

  @Column(name = "previous_quantity")
  private Integer previousQuantity;

  @Column(name = "new_quantity")
  private Integer newQuantity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_warehouse_id")
  private Warehouse sourceWarehouse;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "destination_warehouse_id")
  private Warehouse destinationWarehouse;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "performed_by", nullable = false)
  private User performedBy;

  @CreationTimestamp
  @Column(name = "timestamp", nullable = false, updatable = false)
  private LocalDateTime timestamp;

  @Column(columnDefinition = "TEXT")
  private String notes;

  // Constructors
  public StockActivity() {}

  public StockActivity(
      InventoryItem item, ActivityType activityType, Integer quantityChange, User performedBy) {
    this.item = item;
    this.itemSku = item.getSku();
    this.activityType = activityType;
    this.quantityChange = quantityChange;
    this.performedBy = performedBy;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public InventoryItem getItem() {
    return item;
  }

  public void setItem(InventoryItem item) {
    this.item = item;
    if (item != null) {
      this.itemSku = item.getSku();
    }
  }

  public String getItemSku() {
    return itemSku;
  }

  public void setItemSku(String itemSku) {
    this.itemSku = itemSku;
  }

  public ActivityType getActivityType() {
    return activityType;
  }

  public void setActivityType(ActivityType activityType) {
    this.activityType = activityType;
  }

  public Integer getQuantityChange() {
    return quantityChange;
  }

  public void setQuantityChange(Integer quantityChange) {
    this.quantityChange = quantityChange;
  }

  public Integer getPreviousQuantity() {
    return previousQuantity;
  }

  public void setPreviousQuantity(Integer previousQuantity) {
    this.previousQuantity = previousQuantity;
  }

  public Integer getNewQuantity() {
    return newQuantity;
  }

  public void setNewQuantity(Integer newQuantity) {
    this.newQuantity = newQuantity;
  }

  public Warehouse getSourceWarehouse() {
    return sourceWarehouse;
  }

  public void setSourceWarehouse(Warehouse sourceWarehouse) {
    this.sourceWarehouse = sourceWarehouse;
  }

  public Warehouse getDestinationWarehouse() {
    return destinationWarehouse;
  }

  public void setDestinationWarehouse(Warehouse destinationWarehouse) {
    this.destinationWarehouse = destinationWarehouse;
  }

  public User getPerformedBy() {
    return performedBy;
  }

  public void setPerformedBy(User performedBy) {
    this.performedBy = performedBy;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  // Business logic methods

  /**
   * Checks if this is a transfer activity between warehouses.
   *
   * @return true if activity is a transfer, false otherwise
   */
  public boolean isTransfer() {
    return activityType == ActivityType.TRANSFER;
  }

  /**
   * Validates transfer activity has both source and destination warehouses.
   *
   * @return true if valid transfer, false otherwise
   */
  public boolean isValidTransfer() {
    return isTransfer()
        && sourceWarehouse != null
        && destinationWarehouse != null
        && !sourceWarehouse.getId().equals(destinationWarehouse.getId());
  }

  @Override
  public String toString() {
    return "StockActivity{"
        + "id="
        + id
        + ", itemSku='"
        + itemSku
        + '\''
        + ", activityType="
        + activityType
        + ", quantityChange="
        + quantityChange
        + ", previousQuantity="
        + previousQuantity
        + ", newQuantity="
        + newQuantity
        + ", timestamp="
        + timestamp
        + ", performedBy="
        + (performedBy != null ? performedBy.getUsername() : "null")
        + '}';
  }
}
