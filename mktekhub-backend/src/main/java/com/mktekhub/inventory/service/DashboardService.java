/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.DashboardSummary;
import com.mktekhub.inventory.model.InventoryItem;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.InventoryItemRepository;
import com.mktekhub.inventory.repository.WarehouseRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for dashboard statistics and reports. */
@Service
public class DashboardService {

  @Autowired private WarehouseRepository warehouseRepository;

  @Autowired private InventoryItemRepository inventoryItemRepository;

  /** Get comprehensive dashboard summary. */
  public DashboardSummary getDashboardSummary() {
    DashboardSummary.WarehouseSummary warehouseSummary = getWarehouseSummary();
    DashboardSummary.InventorySummary inventorySummary = getInventorySummary();
    DashboardSummary.AlertsSummary alertsSummary = getAlertsSummary();

    return new DashboardSummary(warehouseSummary, inventorySummary, alertsSummary);
  }

  /** Get warehouse summary statistics. */
  public DashboardSummary.WarehouseSummary getWarehouseSummary() {
    List<Warehouse> allWarehouses = warehouseRepository.findAll();
    List<Warehouse> activeWarehouses = warehouseRepository.findByIsActiveTrue();
    List<Warehouse> warehousesWithAlerts = warehouseRepository.findWarehousesWithCapacityAlert();

    BigDecimal totalCapacity =
        allWarehouses.stream()
            .map(Warehouse::getMaxCapacity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal usedCapacity =
        allWarehouses.stream()
            .map(Warehouse::getCurrentCapacity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal averageUtilization = BigDecimal.ZERO;
    if (!allWarehouses.isEmpty()) {
      BigDecimal totalUtilization =
          allWarehouses.stream()
              .map(Warehouse::getUtilizationPercentage)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      averageUtilization =
          totalUtilization.divide(
              BigDecimal.valueOf(allWarehouses.size()), 2, RoundingMode.HALF_UP);
    }

    return new DashboardSummary.WarehouseSummary(
        allWarehouses.size(),
        activeWarehouses.size(),
        totalCapacity,
        usedCapacity,
        averageUtilization,
        warehousesWithAlerts.size());
  }

  /** Get inventory summary statistics. */
  public DashboardSummary.InventorySummary getInventorySummary() {
    List<InventoryItem> allItems = inventoryItemRepository.findAll();

    long totalItems = allItems.size();

    long totalQuantity = allItems.stream().mapToLong(InventoryItem::getQuantity).sum();

    BigDecimal totalValue =
        allItems.stream()
            .map(
                item ->
                    item.getUnitPrice() != null
                        ? item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                        : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    long uniqueSkus = allItems.stream().map(InventoryItem::getSku).distinct().count();

    long categoriesCount =
        allItems.stream()
            .map(InventoryItem::getCategory)
            .filter(category -> category != null && !category.isEmpty())
            .distinct()
            .count();

    return new DashboardSummary.InventorySummary(
        totalItems, totalQuantity, totalValue, uniqueSkus, categoriesCount);
  }

  /** Get alerts summary statistics. */
  public DashboardSummary.AlertsSummary getAlertsSummary() {
    long lowStockItems = inventoryItemRepository.findLowStockItems().size();
    long expiredItems = inventoryItemRepository.findByExpirationDateBefore(LocalDate.now()).size();

    LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
    long expiringSoonItems =
        inventoryItemRepository
            .findByExpirationDateBetween(LocalDate.now(), thirtyDaysFromNow)
            .size();

    long capacityAlerts = warehouseRepository.findWarehousesWithCapacityAlert().size();

    return new DashboardSummary.AlertsSummary(
        lowStockItems, expiredItems, expiringSoonItems, capacityAlerts);
  }
}
