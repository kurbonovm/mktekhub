package com.mktekhub.inventory.service;

import com.mktekhub.inventory.model.*;
import com.mktekhub.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for generating reports and exports
 */
@Service
public class ReportService {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private StockActivityRepository stockActivityRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Export inventory to CSV
     */
    public byte[] exportInventoryToCSV() {
        List<InventoryItem> items = inventoryItemRepository.findAll();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        // CSV Header
        writer.println("SKU,Name,Category,Brand,Quantity,Unit Price,Warehouse,Location,Reorder Level,Barcode,Expiration Date,Warranty End Date");

        // CSV Data
        for (InventoryItem item : items) {
            writer.printf("%s,%s,%s,%s,%d,%.2f,%s,%s,%d,%s,%s,%s%n",
                escapeCsv(item.getSku()),
                escapeCsv(item.getName()),
                escapeCsv(item.getCategory()),
                escapeCsv(item.getBrand()),
                item.getQuantity(),
                item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO,
                escapeCsv(item.getWarehouse().getName()),
                escapeCsv(item.getWarehouse().getLocation()),
                item.getReorderLevel() != null ? item.getReorderLevel() : 0,
                escapeCsv(item.getBarcode()),
                item.getExpirationDate() != null ? item.getExpirationDate().format(DATE_FORMATTER) : "",
                item.getWarrantyEndDate() != null ? item.getWarrantyEndDate().format(DATE_FORMATTER) : ""
            );
        }

        writer.flush();
        writer.close();
        return outputStream.toByteArray();
    }

    /**
     * Export warehouses to CSV
     */
    public byte[] exportWarehousesToCSV() {
        List<Warehouse> warehouses = warehouseRepository.findAll();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        // CSV Header
        writer.println("Name,Location,Max Capacity,Current Capacity,Utilization %,Available Capacity,Alert Threshold %,Status,Created At");

        // CSV Data
        for (Warehouse warehouse : warehouses) {
            writer.printf("%s,%s,%d,%d,%.2f,%d,%.2f,%s,%s%n",
                escapeCsv(warehouse.getName()),
                escapeCsv(warehouse.getLocation()),
                warehouse.getMaxCapacity(),
                warehouse.getCurrentCapacity(),
                warehouse.getUtilizationPercentage(),
                warehouse.getAvailableCapacity(),
                warehouse.getCapacityAlertThreshold(),
                warehouse.getIsActive() ? "Active" : "Inactive",
                warehouse.getCreatedAt().format(DATETIME_FORMATTER)
            );
        }

        writer.flush();
        writer.close();
        return outputStream.toByteArray();
    }

    /**
     * Export stock activities to CSV
     */
    public byte[] exportStockActivitiesToCSV() {
        List<StockActivity> activities = stockActivityRepository.findAll();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        // CSV Header
        writer.println("Timestamp,Item SKU,Item Name,Activity Type,Quantity Change,Previous Quantity,New Quantity,Source Warehouse,Destination Warehouse,Performed By,Notes");

        // CSV Data
        for (StockActivity activity : activities) {
            writer.printf("%s,%s,%s,%s,%d,%d,%d,%s,%s,%s,%s%n",
                activity.getTimestamp().format(DATETIME_FORMATTER),
                escapeCsv(activity.getItemSku()),
                escapeCsv(activity.getItem().getName()),
                activity.getActivityType().name(),
                activity.getQuantityChange(),
                activity.getPreviousQuantity() != null ? activity.getPreviousQuantity() : 0,
                activity.getNewQuantity() != null ? activity.getNewQuantity() : 0,
                activity.getSourceWarehouse() != null ? escapeCsv(activity.getSourceWarehouse().getName()) : "",
                activity.getDestinationWarehouse() != null ? escapeCsv(activity.getDestinationWarehouse().getName()) : "",
                escapeCsv(activity.getPerformedBy().getUsername()),
                escapeCsv(activity.getNotes())
            );
        }

        writer.flush();
        writer.close();
        return outputStream.toByteArray();
    }

    /**
     * Generate stock valuation report
     */
    public byte[] generateStockValuationReport() {
        List<InventoryItem> items = inventoryItemRepository.findAll();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        // CSV Header
        writer.println("SKU,Name,Category,Warehouse,Quantity,Unit Price,Total Value");

        BigDecimal grandTotal = BigDecimal.ZERO;

        // CSV Data
        for (InventoryItem item : items) {
            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal totalValue = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            grandTotal = grandTotal.add(totalValue);

            writer.printf("%s,%s,%s,%s,%d,%.2f,%.2f%n",
                escapeCsv(item.getSku()),
                escapeCsv(item.getName()),
                escapeCsv(item.getCategory()),
                escapeCsv(item.getWarehouse().getName()),
                item.getQuantity(),
                unitPrice,
                totalValue
            );
        }

        // Add summary
        writer.println();
        writer.printf("TOTAL VALUE:,,,,,%.2f%n", grandTotal);

        writer.flush();
        writer.close();
        return outputStream.toByteArray();
    }

    /**
     * Generate low stock report
     */
    public byte[] generateLowStockReport() {
        List<InventoryItem> items = inventoryItemRepository.findAll();

        // Filter items below reorder level
        List<InventoryItem> lowStockItems = items.stream()
            .filter(item -> item.getReorderLevel() != null && item.getQuantity() <= item.getReorderLevel())
            .collect(Collectors.toList());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        // CSV Header
        writer.println("SKU,Name,Category,Warehouse,Current Quantity,Reorder Level,Shortage,Status");

        // CSV Data
        for (InventoryItem item : lowStockItems) {
            int shortage = item.getReorderLevel() - item.getQuantity();
            String status = item.getQuantity() == 0 ? "OUT OF STOCK" : "LOW STOCK";

            writer.printf("%s,%s,%s,%s,%d,%d,%d,%s%n",
                escapeCsv(item.getSku()),
                escapeCsv(item.getName()),
                escapeCsv(item.getCategory()),
                escapeCsv(item.getWarehouse().getName()),
                item.getQuantity(),
                item.getReorderLevel(),
                shortage,
                status
            );
        }

        writer.flush();
        writer.close();
        return outputStream.toByteArray();
    }

    /**
     * Generate warehouse utilization report
     */
    public byte[] generateWarehouseUtilizationReport() {
        List<Warehouse> warehouses = warehouseRepository.findAll();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        // CSV Header
        writer.println("Warehouse,Location,Max Capacity,Current Capacity,Available Capacity,Utilization %,Status,Alert Status");

        // CSV Data
        for (Warehouse warehouse : warehouses) {
            String status = warehouse.getIsActive() ? "Active" : "Inactive";
            String alertStatus = warehouse.isAlertTriggered() ? "ALERT" : "OK";

            writer.printf("%s,%s,%d,%d,%d,%.2f,%s,%s%n",
                escapeCsv(warehouse.getName()),
                escapeCsv(warehouse.getLocation()),
                warehouse.getMaxCapacity(),
                warehouse.getCurrentCapacity(),
                warehouse.getAvailableCapacity(),
                warehouse.getUtilizationPercentage(),
                status,
                alertStatus
            );
        }

        writer.flush();
        writer.close();
        return outputStream.toByteArray();
    }

    /**
     * Generate stock movement report by warehouse
     */
    public byte[] generateStockMovementReport() {
        List<StockActivity> activities = stockActivityRepository.findAll();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        // CSV Header
        writer.println("Date,Time,Activity Type,Item SKU,Item Name,Quantity,Source Warehouse,Destination Warehouse,Performed By");

        // CSV Data - sorted by timestamp descending
        activities.stream()
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .forEach(activity -> {
                writer.printf("%s,%s,%s,%s,%s,%d,%s,%s,%s%n",
                    activity.getTimestamp().toLocalDate().format(DATE_FORMATTER),
                    activity.getTimestamp().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    activity.getActivityType().name(),
                    escapeCsv(activity.getItemSku()),
                    escapeCsv(activity.getItem().getName()),
                    activity.getQuantityChange(),
                    activity.getSourceWarehouse() != null ? escapeCsv(activity.getSourceWarehouse().getName()) : "N/A",
                    activity.getDestinationWarehouse() != null ? escapeCsv(activity.getDestinationWarehouse().getName()) : "N/A",
                    escapeCsv(activity.getPerformedBy().getUsername())
                );
            });

        writer.flush();
        writer.close();
        return outputStream.toByteArray();
    }

    /**
     * Generate inventory summary by category
     */
    public byte[] generateInventorySummaryByCategory() {
        List<InventoryItem> items = inventoryItemRepository.findAll();

        // Group by category
        Map<String, List<InventoryItem>> itemsByCategory = items.stream()
            .collect(Collectors.groupingBy(item -> item.getCategory() != null ? item.getCategory() : "Uncategorized"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        // CSV Header
        writer.println("Category,Total Items,Total Quantity,Total Value");

        // CSV Data
        for (Map.Entry<String, List<InventoryItem>> entry : itemsByCategory.entrySet()) {
            String category = entry.getKey();
            List<InventoryItem> categoryItems = entry.getValue();

            int totalItems = categoryItems.size();
            int totalQuantity = categoryItems.stream().mapToInt(InventoryItem::getQuantity).sum();
            BigDecimal totalValue = categoryItems.stream()
                .map(item -> {
                    BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            writer.printf("%s,%d,%d,%.2f%n",
                escapeCsv(category),
                totalItems,
                totalQuantity,
                totalValue
            );
        }

        writer.flush();
        writer.close();
        return outputStream.toByteArray();
    }

    /**
     * Generate filtered stock activity report
     */
    public byte[] generateFilteredStockActivityReport(
            Optional<LocalDate> startDate,
            Optional<LocalDate> endDate,
            Optional<String> category,
            Optional<String> brand,
            Optional<Long> warehouseId,
            Optional<String> activityType) {

        List<StockActivity> activities = stockActivityRepository.findAll();

        // Apply filters
        List<StockActivity> filteredActivities = activities.stream()
            .filter(activity -> {
                // Date range filter
                if (startDate.isPresent() && activity.getTimestamp().toLocalDate().isBefore(startDate.get())) {
                    return false;
                }
                if (endDate.isPresent() && activity.getTimestamp().toLocalDate().isAfter(endDate.get())) {
                    return false;
                }

                // Category filter
                if (category.isPresent() && activity.getItem() != null) {
                    String itemCategory = activity.getItem().getCategory();
                    if (itemCategory == null || !itemCategory.equals(category.get())) {
                        return false;
                    }
                }

                // Brand filter
                if (brand.isPresent() && activity.getItem() != null) {
                    String itemBrand = activity.getItem().getBrand();
                    if (itemBrand == null || !itemBrand.equals(brand.get())) {
                        return false;
                    }
                }

                // Warehouse filter (either source or destination)
                if (warehouseId.isPresent()) {
                    boolean matchesWarehouse = false;
                    if (activity.getSourceWarehouse() != null && activity.getSourceWarehouse().getId().equals(warehouseId.get())) {
                        matchesWarehouse = true;
                    }
                    if (activity.getDestinationWarehouse() != null && activity.getDestinationWarehouse().getId().equals(warehouseId.get())) {
                        matchesWarehouse = true;
                    }
                    if (!matchesWarehouse) {
                        return false;
                    }
                }

                // Activity type filter
                if (activityType.isPresent() && !activity.getActivityType().name().equals(activityType.get())) {
                    return false;
                }

                return true;
            })
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(Collectors.toList());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        // CSV Header
        writer.println("Date,Time,Activity Type,Item SKU,Item Name,Category,Brand,Quantity,Source Warehouse,Destination Warehouse,Performed By");

        // CSV Data
        for (StockActivity activity : filteredActivities) {
            writer.printf("%s,%s,%s,%s,%s,%s,%s,%d,%s,%s,%s%n",
                activity.getTimestamp().toLocalDate().format(DATE_FORMATTER),
                activity.getTimestamp().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                activity.getActivityType().name(),
                escapeCsv(activity.getItemSku()),
                escapeCsv(activity.getItem().getName()),
                escapeCsv(activity.getItem().getCategory()),
                escapeCsv(activity.getItem().getBrand()),
                activity.getQuantityChange(),
                activity.getSourceWarehouse() != null ? escapeCsv(activity.getSourceWarehouse().getName()) : "N/A",
                activity.getDestinationWarehouse() != null ? escapeCsv(activity.getDestinationWarehouse().getName()) : "N/A",
                escapeCsv(activity.getPerformedBy().getUsername())
            );
        }

        writer.flush();
        writer.close();
        return outputStream.toByteArray();
    }

    /**
     * Generate filtered inventory valuation report
     */
    public byte[] generateFilteredInventoryValuationReport(
            Optional<String> category,
            Optional<String> brand,
            Optional<Long> warehouseId) {

        List<InventoryItem> items = inventoryItemRepository.findAll();

        // Apply filters
        List<InventoryItem> filteredItems = items.stream()
            .filter(item -> {
                // Category filter
                if (category.isPresent()) {
                    String itemCategory = item.getCategory();
                    if (itemCategory == null || !itemCategory.equals(category.get())) {
                        return false;
                    }
                }

                // Brand filter
                if (brand.isPresent()) {
                    String itemBrand = item.getBrand();
                    if (itemBrand == null || !itemBrand.equals(brand.get())) {
                        return false;
                    }
                }

                // Warehouse filter
                if (warehouseId.isPresent() && !item.getWarehouse().getId().equals(warehouseId.get())) {
                    return false;
                }

                return true;
            })
            .collect(Collectors.toList());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        // CSV Header
        writer.println("SKU,Name,Category,Brand,Warehouse,Quantity,Unit Price,Total Value");

        BigDecimal grandTotal = BigDecimal.ZERO;

        // CSV Data
        for (InventoryItem item : filteredItems) {
            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal totalValue = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            grandTotal = grandTotal.add(totalValue);

            writer.printf("%s,%s,%s,%s,%s,%d,%.2f,%.2f%n",
                escapeCsv(item.getSku()),
                escapeCsv(item.getName()),
                escapeCsv(item.getCategory()),
                escapeCsv(item.getBrand()),
                escapeCsv(item.getWarehouse().getName()),
                item.getQuantity(),
                unitPrice,
                totalValue
            );
        }

        // Add summary
        writer.println();
        writer.printf("TOTAL VALUE:,,,,,,%.2f%n", grandTotal);

        writer.flush();
        writer.close();
        return outputStream.toByteArray();
    }

    /**
     * Escape CSV special characters
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
