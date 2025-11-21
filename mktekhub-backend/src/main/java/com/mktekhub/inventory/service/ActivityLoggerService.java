/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.service;

import com.mktekhub.inventory.model.ActivityType;
import com.mktekhub.inventory.model.InventoryItem;
import com.mktekhub.inventory.model.StockActivity;
import com.mktekhub.inventory.model.User;
import com.mktekhub.inventory.repository.StockActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for logging stock activities consistently across the application. Eliminates code
 * duplication in activity logging.
 */
@Service
public class ActivityLoggerService {

  @Autowired private StockActivityRepository stockActivityRepository;

  /** Logs a stock activity with all required details */
  public StockActivity logActivity(
      InventoryItem item,
      ActivityType activityType,
      Integer quantityChange,
      Integer previousQuantity,
      Integer newQuantity,
      User performedBy,
      String notes) {

    StockActivity activity = new StockActivity();
    activity.setItem(item);
    activity.setItemSku(item.getSku());
    activity.setActivityType(activityType);
    activity.setQuantityChange(quantityChange);
    activity.setPreviousQuantity(previousQuantity);
    activity.setNewQuantity(newQuantity);
    activity.setPerformedBy(performedBy);
    activity.setNotes(notes);

    return stockActivityRepository.save(activity);
  }

  /** Logs an adjustment activity */
  public StockActivity logAdjustment(
      InventoryItem item,
      int quantityChange,
      int previousQuantity,
      int newQuantity,
      User performedBy) {

    String notes =
        "Manual quantity adjustment: " + (quantityChange > 0 ? "+" : "") + quantityChange;

    return logActivity(
        item,
        ActivityType.ADJUSTMENT,
        quantityChange,
        previousQuantity,
        newQuantity,
        performedBy,
        notes);
  }

  /** Logs a transfer activity */
  public StockActivity logTransfer(
      InventoryItem item,
      int quantityChange,
      int previousQuantity,
      int newQuantity,
      User performedBy,
      String notes) {

    return logActivity(
        item,
        ActivityType.TRANSFER,
        quantityChange,
        previousQuantity,
        newQuantity,
        performedBy,
        notes);
  }

  /** Logs a receive activity */
  public StockActivity logReceive(
      InventoryItem item, int quantity, User performedBy, String notes) {

    return logActivity(item, ActivityType.RECEIVE, quantity, 0, quantity, performedBy, notes);
  }

  /** Logs an update activity */
  public StockActivity logUpdate(InventoryItem item, User performedBy, String notes) {

    return logActivity(
        item, ActivityType.UPDATE, 0, item.getQuantity(), item.getQuantity(), performedBy, notes);
  }
}
