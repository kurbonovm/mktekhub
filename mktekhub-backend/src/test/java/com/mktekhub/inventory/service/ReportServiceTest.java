/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mktekhub.inventory.model.*;
import com.mktekhub.inventory.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Comprehensive unit tests for ReportService. */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService Tests")
class ReportServiceTest {

  @Mock private InventoryItemRepository inventoryItemRepository;

  @Mock private WarehouseRepository warehouseRepository;

  @Mock private StockActivityRepository stockActivityRepository;

  @InjectMocks private ReportService reportService;

  private Warehouse warehouse;
  private InventoryItem item1;
  private InventoryItem item2;
  private StockActivity activity;
  private User user;

  @BeforeEach
  void setUp() {
    warehouse = new Warehouse();
    warehouse.setId(1L);
    warehouse.setName("Main Warehouse");
    warehouse.setLocation("New York");
    warehouse.setMaxCapacity(new BigDecimal("10000.00"));
    warehouse.setCurrentCapacity(new BigDecimal("5000.00"));
    warehouse.setCapacityAlertThreshold(new BigDecimal("80.00"));
    warehouse.setIsActive(true);
    warehouse.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));

    item1 = new InventoryItem();
    item1.setId(1L);
    item1.setSku("SKU-001");
    item1.setName("Test Item 1");
    item1.setCategory("Electronics");
    item1.setBrand("Brand A");
    item1.setQuantity(100);
    item1.setUnitPrice(new BigDecimal("50.00"));
    item1.setVolumePerUnit(new BigDecimal("10.00"));
    item1.setReorderLevel(20);
    item1.setWarehouse(warehouse);
    item1.setBarcode("123456");
    item1.setExpirationDate(LocalDate.of(2025, 12, 31));
    item1.setWarrantyEndDate(LocalDate.of(2025, 6, 30));

    item2 = new InventoryItem();
    item2.setId(2L);
    item2.setSku("SKU-002");
    item2.setName("Test, Item \"2\""); // Test CSV escaping
    item2.setCategory("Furniture");
    item2.setBrand(null);
    item2.setQuantity(50);
    item2.setUnitPrice(new BigDecimal("150.00"));
    item2.setVolumePerUnit(new BigDecimal("50.00"));
    item2.setReorderLevel(10);
    item2.setWarehouse(warehouse);
    item2.setBarcode(null);
    item2.setExpirationDate(null);
    item2.setWarrantyEndDate(null);

    user = new User();
    user.setId(1L);
    user.setUsername("testuser");

    activity = new StockActivity();
    activity.setId(1L);
    activity.setItem(item1);
    activity.setItemSku("SKU-001");
    activity.setActivityType(ActivityType.RECEIVE);
    activity.setQuantityChange(100);
    activity.setPreviousQuantity(0);
    activity.setNewQuantity(100);
    activity.setPerformedBy(user);
    activity.setTimestamp(LocalDateTime.of(2024, 1, 15, 14, 30));
    activity.setNotes("Initial stock receipt");
  }

  // ==================== EXPORT INVENTORY TO CSV ====================

  @Test
  @DisplayName("ExportInventoryToCSV - Should generate CSV with all items")
  void exportInventoryToCSV_Success() {
    // Arrange
    when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

    // Act
    byte[] result = reportService.exportInventoryToCSV();

    // Assert
    assertNotNull(result);
    assertTrue(result.length > 0);

    String csv = new String(result);
    assertTrue(csv.contains("SKU,Name,Category"));
    assertTrue(csv.contains("SKU-001"));
    assertTrue(csv.contains("SKU-002"));
    assertTrue(csv.contains("Test Item 1"));
    assertTrue(csv.contains("Electronics"));
    assertTrue(csv.contains("Brand A"));

    verify(inventoryItemRepository).findAll();
  }

  @Test
  @DisplayName("ExportInventoryToCSV - Should handle CSV special characters")
  void exportInventoryToCSV_CSVEscaping() {
    // Arrange
    when(inventoryItemRepository.findAll()).thenReturn(Collections.singletonList(item2));

    // Act
    byte[] result = reportService.exportInventoryToCSV();

    // Assert
    assertNotNull(result);
    String csv = new String(result);

    // Should escape quotes and commas properly
    assertTrue(csv.contains("Test"));
    verify(inventoryItemRepository).findAll();
  }

  @Test
  @DisplayName("ExportInventoryToCSV - Should handle empty list")
  void exportInventoryToCSV_EmptyList() {
    // Arrange
    when(inventoryItemRepository.findAll()).thenReturn(Collections.emptyList());

    // Act
    byte[] result = reportService.exportInventoryToCSV();

    // Assert
    assertNotNull(result);
    String csv = new String(result);
    assertTrue(csv.contains("SKU,Name,Category")); // Header should still be present

    verify(inventoryItemRepository).findAll();
  }

  @Test
  @DisplayName("ExportInventoryToCSV - Should handle null fields")
  void exportInventoryToCSV_NullFields() {
    // Arrange
    when(inventoryItemRepository.findAll()).thenReturn(Collections.singletonList(item2));

    // Act
    byte[] result = reportService.exportInventoryToCSV();

    // Assert
    assertNotNull(result);
    String csv = new String(result);

    // Should handle null brand, barcode, and dates gracefully
    assertTrue(csv.contains("SKU-002"));
    verify(inventoryItemRepository).findAll();
  }

  // ==================== EXPORT WAREHOUSES TO CSV ====================

  @Test
  @DisplayName("ExportWarehousesToCSV - Should generate CSV with all warehouses")
  void exportWarehousesToCSV_Success() {
    // Arrange
    Warehouse warehouse2 = new Warehouse();
    warehouse2.setId(2L);
    warehouse2.setName("Secondary Warehouse");
    warehouse2.setLocation("Boston");
    warehouse2.setMaxCapacity(new BigDecimal("5000.00"));
    warehouse2.setCurrentCapacity(new BigDecimal("2000.00"));
    warehouse2.setCapacityAlertThreshold(new BigDecimal("75.00"));
    warehouse2.setIsActive(false);
    warehouse2.setCreatedAt(LocalDateTime.of(2024, 2, 1, 9, 0));

    when(warehouseRepository.findAll()).thenReturn(Arrays.asList(warehouse, warehouse2));

    // Act
    byte[] result = reportService.exportWarehousesToCSV();

    // Assert
    assertNotNull(result);
    assertTrue(result.length > 0);

    String csv = new String(result);
    assertTrue(csv.contains("Name,Location,Max Capacity"));
    assertTrue(csv.contains("Main Warehouse"));
    assertTrue(csv.contains("Secondary Warehouse"));
    assertTrue(csv.contains("New York"));
    assertTrue(csv.contains("Boston"));
    assertTrue(csv.contains("Active"));
    assertTrue(csv.contains("Inactive"));

    verify(warehouseRepository).findAll();
  }

  @Test
  @DisplayName("ExportWarehousesToCSV - Should include warehouse metrics")
  void exportWarehousesToCSV_Metrics() {
    // Arrange
    when(warehouseRepository.findAll()).thenReturn(Collections.singletonList(warehouse));

    // Act
    byte[] result = reportService.exportWarehousesToCSV();

    // Assert
    assertNotNull(result);
    String csv = new String(result);

    // Should include utilization percentage and available capacity
    assertTrue(csv.contains("50.00")); // Utilization percentage (5000/10000 * 100)
    assertTrue(csv.contains("80.00")); // Alert threshold

    verify(warehouseRepository).findAll();
  }

  @Test
  @DisplayName("ExportWarehousesToCSV - Should handle empty list")
  void exportWarehousesToCSV_EmptyList() {
    // Arrange
    when(warehouseRepository.findAll()).thenReturn(Collections.emptyList());

    // Act
    byte[] result = reportService.exportWarehousesToCSV();

    // Assert
    assertNotNull(result);
    String csv = new String(result);
    assertTrue(csv.contains("Name,Location,Max Capacity")); // Header should be present

    verify(warehouseRepository).findAll();
  }

  // ==================== EXPORT STOCK ACTIVITIES TO CSV ====================

  @Test
  @DisplayName("ExportStockActivitiesToCSV - Should generate CSV with all activities")
  void exportStockActivitiesToCSV_Success() {
    // Arrange
    when(stockActivityRepository.findAll()).thenReturn(Collections.singletonList(activity));

    // Act
    byte[] result = reportService.exportStockActivitiesToCSV();

    // Assert
    assertNotNull(result);
    assertTrue(result.length > 0);

    String csv = new String(result);
    assertTrue(csv.contains("Timestamp,Item SKU,Item Name,Activity Type"));
    assertTrue(csv.contains("SKU-001"));
    assertTrue(csv.contains("RECEIVE"));
    assertTrue(csv.contains("testuser"));
    assertTrue(csv.contains("Initial stock receipt"));

    verify(stockActivityRepository).findAll();
  }

  @Test
  @DisplayName("ExportStockActivitiesToCSV - Should handle empty list")
  void exportStockActivitiesToCSV_EmptyList() {
    // Arrange
    when(stockActivityRepository.findAll()).thenReturn(Collections.emptyList());

    // Act
    byte[] result = reportService.exportStockActivitiesToCSV();

    // Assert
    assertNotNull(result);
    String csv = new String(result);
    assertTrue(
        csv.contains("Timestamp,Item SKU,Item Name,Activity Type")); // Header should be present

    verify(stockActivityRepository).findAll();
  }

  // ==================== STOCK VALUATION REPORT ====================

  @Test
  @DisplayName("GenerateStockValuationReport - Should calculate total value correctly")
  void generateStockValuationReport_Success() {
    // Arrange
    when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

    // Act
    byte[] result = reportService.generateStockValuationReport();

    // Assert
    assertNotNull(result);
    String csv = new String(result);

    assertTrue(csv.contains("SKU,Name,Category,Warehouse,Quantity,Unit Price,Total Value"));
    assertTrue(csv.contains("SKU-001"));
    assertTrue(csv.contains("SKU-002"));

    // Total value should be: (100 * 50) + (50 * 150) = 5000 + 7500 = 12500
    assertTrue(csv.contains("12500"));

    verify(inventoryItemRepository).findAll();
  }

  @Test
  @DisplayName("GenerateStockValuationReport - Should handle items with null prices")
  void generateStockValuationReport_NullPrices() {
    // Arrange
    item1.setUnitPrice(null);
    when(inventoryItemRepository.findAll()).thenReturn(Collections.singletonList(item1));

    // Act
    byte[] result = reportService.generateStockValuationReport();

    // Assert
    assertNotNull(result);
    String csv = new String(result);
    assertTrue(csv.contains("SKU-001"));

    verify(inventoryItemRepository).findAll();
  }

  // ==================== LOW STOCK REPORT ====================

  @Test
  @DisplayName("GenerateLowStockReport - Should include only low stock items")
  void generateLowStockReport_Success() {
    // Arrange
    item1.setQuantity(10); // Below reorder level of 20
    when(inventoryItemRepository.findAll()).thenReturn(Collections.singletonList(item1));

    // Act
    byte[] result = reportService.generateLowStockReport();

    // Assert
    assertNotNull(result);
    String csv = new String(result);

    assertTrue(csv.contains("SKU,Name,Category,Warehouse,Current Quantity,Reorder Level"));
    assertTrue(csv.contains("SKU-001"));

    verify(inventoryItemRepository).findAll();
  }

  @Test
  @DisplayName("GenerateLowStockReport - Should handle empty result")
  void generateLowStockReport_NoLowStockItems() {
    // Arrange
    item1.setQuantity(100); // Above reorder level
    when(inventoryItemRepository.findAll()).thenReturn(Collections.singletonList(item1));

    // Act
    byte[] result = reportService.generateLowStockReport();

    // Assert
    assertNotNull(result);
    String csv = new String(result);
    assertTrue(csv.contains("SKU,Name,Category,Warehouse,Current Quantity,Reorder Level"));

    verify(inventoryItemRepository).findAll();
  }

  // ==================== WAREHOUSE UTILIZATION REPORT ====================

  @Test
  @DisplayName("GenerateWarehouseUtilizationReport - Should show warehouse utilization")
  void generateWarehouseUtilizationReport_Success() {
    // Arrange
    when(warehouseRepository.findAll()).thenReturn(Collections.singletonList(warehouse));

    // Act
    byte[] result = reportService.generateWarehouseUtilizationReport();

    // Assert
    assertNotNull(result);
    String csv = new String(result);

    assertTrue(csv.contains("Warehouse,Location,Max Capacity"));
    assertTrue(csv.contains("Main Warehouse"));
    assertTrue(csv.contains("50.00")); // Utilization percentage

    verify(warehouseRepository).findAll();
  }

  // ==================== STOCK MOVEMENT REPORT ====================

  @Test
  @DisplayName("GenerateStockMovementReport - Should list all stock activities")
  void generateStockMovementReport_Success() {
    // Arrange
    when(stockActivityRepository.findAll()).thenReturn(Collections.singletonList(activity));

    // Act
    byte[] result = reportService.generateStockMovementReport();

    // Assert
    assertNotNull(result);
    String csv = new String(result);

    assertTrue(csv.contains("Date,Time,Activity Type"));
    assertTrue(csv.contains("SKU-001"));
    assertTrue(csv.contains("RECEIVE"));

    verify(stockActivityRepository).findAll();
  }

  // ==================== INVENTORY SUMMARY BY CATEGORY ====================

  @Test
  @DisplayName("GenerateInventorySummaryByCategory - Should group by category")
  void generateInventorySummaryByCategory_Success() {
    // Arrange
    when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

    // Act
    byte[] result = reportService.generateInventorySummaryByCategory();

    // Assert
    assertNotNull(result);
    String csv = new String(result);

    assertTrue(csv.contains("Category,Total Items,Total Quantity,Total Value"));
    assertTrue(csv.contains("Electronics"));
    assertTrue(csv.contains("Furniture"));

    verify(inventoryItemRepository).findAll();
  }

  @Test
  @DisplayName("GenerateInventorySummaryByCategory - Should handle null categories")
  void generateInventorySummaryByCategory_NullCategory() {
    // Arrange
    item1.setCategory(null);
    when(inventoryItemRepository.findAll()).thenReturn(Collections.singletonList(item1));

    // Act
    byte[] result = reportService.generateInventorySummaryByCategory();

    // Assert
    assertNotNull(result);
    String csv = new String(result);
    assertTrue(csv.contains("Category,Total Items,Total Quantity,Total Value"));

    verify(inventoryItemRepository).findAll();
  }

  // ==================== FILTERED REPORTS ====================

  @Test
  @DisplayName("GenerateFilteredStockActivityReport - Should filter by date range")
  void generateFilteredStockActivityReport_WithFilters() {
    // Arrange
    LocalDate startDate = LocalDate.of(2024, 1, 1);
    LocalDate endDate = LocalDate.of(2024, 12, 31);

    when(stockActivityRepository.findAll()).thenReturn(Collections.singletonList(activity));

    // Act
    byte[] result =
        reportService.generateFilteredStockActivityReport(
            Optional.of(startDate),
            Optional.of(endDate),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

    // Assert
    assertNotNull(result);
    String csv = new String(result);
    assertTrue(csv.contains("Date,Time,Activity Type"));

    verify(stockActivityRepository).findAll();
  }

  @Test
  @DisplayName("GenerateFilteredInventoryValuationReport - Should filter by category")
  void generateFilteredInventoryValuationReport_WithCategory() {
    // Arrange
    when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

    // Act
    byte[] result =
        reportService.generateFilteredInventoryValuationReport(
            Optional.of("Electronics"), Optional.empty(), Optional.empty());

    // Assert
    assertNotNull(result);
    String csv = new String(result);
    assertTrue(csv.contains("SKU,Name,Category"));
    assertTrue(csv.contains("SKU-001"));

    verify(inventoryItemRepository).findAll();
  }

  @Test
  @DisplayName("GenerateFilteredInventoryValuationReport - Should filter by warehouse")
  void generateFilteredInventoryValuationReport_WithWarehouse() {
    // Arrange
    when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

    // Act
    byte[] result =
        reportService.generateFilteredInventoryValuationReport(
            Optional.empty(), Optional.empty(), Optional.of(1L));

    // Assert
    assertNotNull(result);
    String csv = new String(result);
    assertTrue(csv.contains("SKU,Name,Category"));

    verify(inventoryItemRepository).findAll();
  }
}
