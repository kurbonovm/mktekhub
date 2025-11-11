package com.mktekhub.inventory.dto;

import java.math.BigDecimal;

/**
 * DTO for overall dashboard summary.
 */
public class DashboardSummary {
    private WarehouseSummary warehouseSummary;
    private InventorySummary inventorySummary;
    private AlertsSummary alertsSummary;

    // Constructors
    public DashboardSummary() {
    }

    public DashboardSummary(WarehouseSummary warehouseSummary, InventorySummary inventorySummary,
                            AlertsSummary alertsSummary) {
        this.warehouseSummary = warehouseSummary;
        this.inventorySummary = inventorySummary;
        this.alertsSummary = alertsSummary;
    }

    // Getters and Setters
    public WarehouseSummary getWarehouseSummary() {
        return warehouseSummary;
    }

    public void setWarehouseSummary(WarehouseSummary warehouseSummary) {
        this.warehouseSummary = warehouseSummary;
    }

    public InventorySummary getInventorySummary() {
        return inventorySummary;
    }

    public void setInventorySummary(InventorySummary inventorySummary) {
        this.inventorySummary = inventorySummary;
    }

    public AlertsSummary getAlertsSummary() {
        return alertsSummary;
    }

    public void setAlertsSummary(AlertsSummary alertsSummary) {
        this.alertsSummary = alertsSummary;
    }

    /**
     * Warehouse summary statistics.
     */
    public static class WarehouseSummary {
        private int totalWarehouses;
        private int activeWarehouses;
        private int totalCapacity;
        private int usedCapacity;
        private BigDecimal averageUtilization;
        private int warehousesWithAlerts;

        public WarehouseSummary() {
        }

        public WarehouseSummary(int totalWarehouses, int activeWarehouses, int totalCapacity,
                                int usedCapacity, BigDecimal averageUtilization, int warehousesWithAlerts) {
            this.totalWarehouses = totalWarehouses;
            this.activeWarehouses = activeWarehouses;
            this.totalCapacity = totalCapacity;
            this.usedCapacity = usedCapacity;
            this.averageUtilization = averageUtilization;
            this.warehousesWithAlerts = warehousesWithAlerts;
        }

        public int getTotalWarehouses() {
            return totalWarehouses;
        }

        public void setTotalWarehouses(int totalWarehouses) {
            this.totalWarehouses = totalWarehouses;
        }

        public int getActiveWarehouses() {
            return activeWarehouses;
        }

        public void setActiveWarehouses(int activeWarehouses) {
            this.activeWarehouses = activeWarehouses;
        }

        public int getTotalCapacity() {
            return totalCapacity;
        }

        public void setTotalCapacity(int totalCapacity) {
            this.totalCapacity = totalCapacity;
        }

        public int getUsedCapacity() {
            return usedCapacity;
        }

        public void setUsedCapacity(int usedCapacity) {
            this.usedCapacity = usedCapacity;
        }

        public BigDecimal getAverageUtilization() {
            return averageUtilization;
        }

        public void setAverageUtilization(BigDecimal averageUtilization) {
            this.averageUtilization = averageUtilization;
        }

        public int getWarehousesWithAlerts() {
            return warehousesWithAlerts;
        }

        public void setWarehousesWithAlerts(int warehousesWithAlerts) {
            this.warehousesWithAlerts = warehousesWithAlerts;
        }
    }

    /**
     * Inventory summary statistics.
     */
    public static class InventorySummary {
        private long totalItems;
        private long totalQuantity;
        private BigDecimal totalValue;
        private long uniqueSkus;
        private long categoriesCount;

        public InventorySummary() {
        }

        public InventorySummary(long totalItems, long totalQuantity, BigDecimal totalValue,
                                long uniqueSkus, long categoriesCount) {
            this.totalItems = totalItems;
            this.totalQuantity = totalQuantity;
            this.totalValue = totalValue;
            this.uniqueSkus = uniqueSkus;
            this.categoriesCount = categoriesCount;
        }

        public long getTotalItems() {
            return totalItems;
        }

        public void setTotalItems(long totalItems) {
            this.totalItems = totalItems;
        }

        public long getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(long totalQuantity) {
            this.totalQuantity = totalQuantity;
        }

        public BigDecimal getTotalValue() {
            return totalValue;
        }

        public void setTotalValue(BigDecimal totalValue) {
            this.totalValue = totalValue;
        }

        public long getUniqueSkus() {
            return uniqueSkus;
        }

        public void setUniqueSkus(long uniqueSkus) {
            this.uniqueSkus = uniqueSkus;
        }

        public long getCategoriesCount() {
            return categoriesCount;
        }

        public void setCategoriesCount(long categoriesCount) {
            this.categoriesCount = categoriesCount;
        }
    }

    /**
     * Alerts summary statistics.
     */
    public static class AlertsSummary {
        private long lowStockItems;
        private long expiredItems;
        private long expiringSoonItems;
        private long capacityAlerts;

        public AlertsSummary() {
        }

        public AlertsSummary(long lowStockItems, long expiredItems, long expiringSoonItems, long capacityAlerts) {
            this.lowStockItems = lowStockItems;
            this.expiredItems = expiredItems;
            this.expiringSoonItems = expiringSoonItems;
            this.capacityAlerts = capacityAlerts;
        }

        public long getLowStockItems() {
            return lowStockItems;
        }

        public void setLowStockItems(long lowStockItems) {
            this.lowStockItems = lowStockItems;
        }

        public long getExpiredItems() {
            return expiredItems;
        }

        public void setExpiredItems(long expiredItems) {
            this.expiredItems = expiredItems;
        }

        public long getExpiringSoonItems() {
            return expiringSoonItems;
        }

        public void setExpiringSoonItems(long expiringSoonItems) {
            this.expiringSoonItems = expiringSoonItems;
        }

        public long getCapacityAlerts() {
            return capacityAlerts;
        }

        public void setCapacityAlerts(long capacityAlerts) {
            this.capacityAlerts = capacityAlerts;
        }
    }
}
