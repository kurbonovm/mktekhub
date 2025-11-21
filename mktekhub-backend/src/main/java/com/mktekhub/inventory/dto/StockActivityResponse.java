/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.dto;

import com.mktekhub.inventory.model.ActivityType;
import com.mktekhub.inventory.model.StockActivity;
import java.time.LocalDateTime;

/** DTO for stock activity response. */
public class StockActivityResponse {
  private Long id;
  private String itemSku;
  private String itemName;
  private ActivityType activityType;
  private Integer quantityChange;
  private Integer previousQuantity;
  private Integer newQuantity;
  private LocalDateTime timestamp;
  private String performedBy;
  private String sourceWarehouseName;
  private String destinationWarehouseName;
  private String notes;

  // Constructors
  public StockActivityResponse() {}

  /** Convert StockActivity entity to StockActivityResponse DTO. */
  public static StockActivityResponse fromEntity(StockActivity activity) {
    StockActivityResponse response = new StockActivityResponse();
    response.setId(activity.getId());
    response.setItemSku(activity.getItemSku());
    response.setItemName(activity.getItem() != null ? activity.getItem().getName() : null);
    response.setActivityType(activity.getActivityType());
    response.setQuantityChange(activity.getQuantityChange());
    response.setPreviousQuantity(activity.getPreviousQuantity());
    response.setNewQuantity(activity.getNewQuantity());
    response.setTimestamp(activity.getTimestamp());
    response.setPerformedBy(
        activity.getPerformedBy() != null ? activity.getPerformedBy().getUsername() : null);
    response.setSourceWarehouseName(
        activity.getSourceWarehouse() != null ? activity.getSourceWarehouse().getName() : null);
    response.setDestinationWarehouseName(
        activity.getDestinationWarehouse() != null
            ? activity.getDestinationWarehouse().getName()
            : null);
    response.setNotes(activity.getNotes());
    return response;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getItemSku() {
    return itemSku;
  }

  public void setItemSku(String itemSku) {
    this.itemSku = itemSku;
  }

  public String getItemName() {
    return itemName;
  }

  public void setItemName(String itemName) {
    this.itemName = itemName;
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

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getPerformedBy() {
    return performedBy;
  }

  public void setPerformedBy(String performedBy) {
    this.performedBy = performedBy;
  }

  public String getSourceWarehouseName() {
    return sourceWarehouseName;
  }

  public void setSourceWarehouseName(String sourceWarehouseName) {
    this.sourceWarehouseName = sourceWarehouseName;
  }

  public String getDestinationWarehouseName() {
    return destinationWarehouseName;
  }

  public void setDestinationWarehouseName(String destinationWarehouseName) {
    this.destinationWarehouseName = destinationWarehouseName;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
