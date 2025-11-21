/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.dto;

import java.util.List;

/** DTO for combined alerts response. */
public class CombinedAlertsResponse {
  private List<InventoryItemResponse> lowStockItems;
  private List<InventoryItemResponse> expiredItems;
  private List<InventoryItemResponse> expiringSoonItems;
  private List<WarehouseResponse> capacityAlerts;

  // Constructors
  public CombinedAlertsResponse() {}

  public CombinedAlertsResponse(
      List<InventoryItemResponse> lowStockItems,
      List<InventoryItemResponse> expiredItems,
      List<InventoryItemResponse> expiringSoonItems,
      List<WarehouseResponse> capacityAlerts) {
    this.lowStockItems = lowStockItems;
    this.expiredItems = expiredItems;
    this.expiringSoonItems = expiringSoonItems;
    this.capacityAlerts = capacityAlerts;
  }

  // Getters and Setters
  public List<InventoryItemResponse> getLowStockItems() {
    return lowStockItems;
  }

  public void setLowStockItems(List<InventoryItemResponse> lowStockItems) {
    this.lowStockItems = lowStockItems;
  }

  public List<InventoryItemResponse> getExpiredItems() {
    return expiredItems;
  }

  public void setExpiredItems(List<InventoryItemResponse> expiredItems) {
    this.expiredItems = expiredItems;
  }

  public List<InventoryItemResponse> getExpiringSoonItems() {
    return expiringSoonItems;
  }

  public void setExpiringSoonItems(List<InventoryItemResponse> expiringSoonItems) {
    this.expiringSoonItems = expiringSoonItems;
  }

  public List<WarehouseResponse> getCapacityAlerts() {
    return capacityAlerts;
  }

  public void setCapacityAlerts(List<WarehouseResponse> capacityAlerts) {
    this.capacityAlerts = capacityAlerts;
  }
}
