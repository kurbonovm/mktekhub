package com.mktekhub.inventory.service;

import com.mktekhub.inventory.model.*;
import com.mktekhub.inventory.repository.InventoryItemRepository;
import com.mktekhub.inventory.repository.StockActivityRepository;
import com.mktekhub.inventory.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockActivityRepository stockActivityRepository;

    @InjectMocks
    private ReportService reportService;

    private Warehouse warehouse;
    private InventoryItem item1;
    private InventoryItem item2;
    private StockActivity activity;
    private User user;

    @BeforeEach
    void setUp() {
        // Setup warehouse
        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setName("Test Warehouse");
        warehouse.setLocation("New York");
        warehouse.setMaxCapacity(new BigDecimal("10000"));
        warehouse.setCurrentCapacity(new BigDecimal("5000"));
        warehouse.setIsActive(true);
        warehouse.setCapacityAlertThreshold(new BigDecimal("80"));
        warehouse.setCreatedAt(LocalDateTime.now());

        // Setup items
        item1 = new InventoryItem();
        item1.setId(1L);
        item1.setSku("SKU001");
        item1.setName("Item 1");
        item1.setCategory("Electronics");
        item1.setBrand("Brand A");
        item1.setQuantity(100);
        item1.setUnitPrice(new BigDecimal("10.00"));
        item1.setReorderLevel(20);
        item1.setWarehouse(warehouse);

        item2 = new InventoryItem();
        item2.setId(2L);
        item2.setSku("SKU002");
        item2.setName("Item 2");
        item2.setCategory("Clothing");
        item2.setBrand("Brand B");
        item2.setQuantity(15);
        item2.setUnitPrice(new BigDecimal("25.00"));
        item2.setReorderLevel(20);
        item2.setWarehouse(warehouse);

        // Setup user
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        // Setup activity
        activity = new StockActivity();
        activity.setId(1L);
        activity.setItem(item1);
        activity.setItemSku("SKU001");
        activity.setActivityType(ActivityType.ADJUSTMENT);
        activity.setQuantityChange(10);
        activity.setPreviousQuantity(90);
        activity.setNewQuantity(100);
        activity.setTimestamp(LocalDateTime.now());
        activity.setPerformedBy(user);
        activity.setNotes("Test activity");
    }

    @Test
    void exportInventoryToCSV_GeneratesValidCSV() {
        // Arrange
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        // Act
        byte[] result = reportService.exportInventoryToCSV();
        String csv = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertNotNull(result);
        assertTrue(csv.contains("SKU,Name,Category"));
        assertTrue(csv.contains("SKU001"));
        assertTrue(csv.contains("SKU002"));
        assertTrue(csv.contains("Item 1"));
        assertTrue(csv.contains("Electronics"));
        verify(inventoryItemRepository).findAll();
    }

    @Test
    void exportWarehousesToCSV_GeneratesValidCSV() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(List.of(warehouse));

        // Act
        byte[] result = reportService.exportWarehousesToCSV();
        String csv = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertNotNull(result);
        assertTrue(csv.contains("Name,Location,Max Capacity"));
        assertTrue(csv.contains("Test Warehouse"));
        assertTrue(csv.contains("New York"));
        verify(warehouseRepository).findAll();
    }

    @Test
    void exportStockActivitiesToCSV_GeneratesValidCSV() {
        // Arrange
        when(stockActivityRepository.findAll()).thenReturn(List.of(activity));

        // Act
        byte[] result = reportService.exportStockActivitiesToCSV();
        String csv = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertNotNull(result);
        assertTrue(csv.contains("Timestamp,Item SKU,Item Name"));
        assertTrue(csv.contains("SKU001"));
        assertTrue(csv.contains("ADJUSTMENT"));
        verify(stockActivityRepository).findAll();
    }

    @Test
    void generateStockValuationReport_CalculatesTotalValue() {
        // Arrange
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        // Act
        byte[] result = reportService.generateStockValuationReport();
        String csv = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertNotNull(result);
        assertTrue(csv.contains("SKU,Name,Category,Warehouse,Quantity,Unit Price,Total Value"));
        assertTrue(csv.contains("TOTAL VALUE"));
        // item1: 100 * 10 = 1000, item2: 15 * 25 = 375, total = 1375
        assertTrue(csv.contains("1375"));
        verify(inventoryItemRepository).findAll();
    }

    @Test
    void generateLowStockReport_FilterslowStockItems() {
        // Arrange
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        // Act
        byte[] result = reportService.generateLowStockReport();
        String csv = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertNotNull(result);
        assertTrue(csv.contains("SKU,Name,Category,Warehouse,Current Quantity,Reorder Level"));
        // item2 has quantity 15, reorder level 20, so it should be in the report
        assertTrue(csv.contains("SKU002"));
        assertTrue(csv.contains("LOW STOCK"));
        // item1 has quantity 100, reorder level 20, so it should NOT be in the report
        assertFalse(csv.contains("SKU001,Item 1"));
        verify(inventoryItemRepository).findAll();
    }

    @Test
    void generateWarehouseUtilizationReport_ShowsUtilization() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(List.of(warehouse));

        // Act
        byte[] result = reportService.generateWarehouseUtilizationReport();
        String csv = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertNotNull(result);
        assertTrue(csv.contains("Warehouse,Location,Max Capacity,Current Capacity"));
        assertTrue(csv.contains("Test Warehouse"));
        assertTrue(csv.contains("Active"));
        verify(warehouseRepository).findAll();
    }

    @Test
    void generateStockMovementReport_SortsActivitiesByTimestamp() {
        // Arrange
        StockActivity activity2 = new StockActivity();
        activity2.setId(2L);
        activity2.setItem(item2);
        activity2.setItemSku("SKU002");
        activity2.setActivityType(ActivityType.TRANSFER);
        activity2.setQuantityChange(5);
        activity2.setTimestamp(LocalDateTime.now().plusHours(1));
        activity2.setPerformedBy(user);

        when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity, activity2));

        // Act
        byte[] result = reportService.generateStockMovementReport();
        String csv = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertNotNull(result);
        assertTrue(csv.contains("Date,Time,Activity Type,Item SKU"));
        assertTrue(csv.contains("ADJUSTMENT"));
        assertTrue(csv.contains("TRANSFER"));
        verify(stockActivityRepository).findAll();
    }

    @Test
    void generateInventorySummaryByCategory_GroupsByCategory() {
        // Arrange
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        // Act
        byte[] result = reportService.generateInventorySummaryByCategory();
        String csv = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertNotNull(result);
        assertTrue(csv.contains("Category,Total Items,Total Quantity,Total Value"));
        assertTrue(csv.contains("Electronics"));
        assertTrue(csv.contains("Clothing"));
        verify(inventoryItemRepository).findAll();
    }

    @Test
    void escapeCsv_HandlesSpecialCharacters() {
        // Arrange
        InventoryItem itemWithComma = new InventoryItem();
        itemWithComma.setSku("SKU003");
        itemWithComma.setName("Item, with comma");
        itemWithComma.setCategory("Test");
        itemWithComma.setQuantity(10);
        itemWithComma.setUnitPrice(new BigDecimal("5.00"));
        itemWithComma.setWarehouse(warehouse);

        when(inventoryItemRepository.findAll()).thenReturn(List.of(itemWithComma));

        // Act
        byte[] result = reportService.exportInventoryToCSV();
        String csv = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertNotNull(result);
        assertTrue(csv.contains("\"Item, with comma\""));
    }

    @Test
    void exportInventoryToCSV_HandlesNullValues() {
        // Arrange
        InventoryItem itemWithNulls = new InventoryItem();
        itemWithNulls.setSku("SKU003");
        itemWithNulls.setName("Item 3");
        itemWithNulls.setQuantity(10);
        itemWithNulls.setWarehouse(warehouse);
        // category, brand, unitPrice are null

        when(inventoryItemRepository.findAll()).thenReturn(List.of(itemWithNulls));

        // Act
        byte[] result = reportService.exportInventoryToCSV();

        // Assert
        assertNotNull(result);
        String csv = new String(result, StandardCharsets.UTF_8);
        assertTrue(csv.contains("SKU003"));
    }

    @Test
    void generateStockValuationReport_HandlesNullPrices() {
        // Arrange
        item1.setUnitPrice(null);
        when(inventoryItemRepository.findAll()).thenReturn(List.of(item1));

        // Act
        byte[] result = reportService.generateStockValuationReport();
        String csv = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertNotNull(result);
        assertTrue(csv.contains("0.00")); // Should use 0 for null price
    }
}
