package com.mktekhub.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mktekhub.inventory.model.InventoryItem;
import com.mktekhub.inventory.model.StockActivity;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.InventoryItemRepository;
import com.mktekhub.inventory.repository.StockActivityRepository;
import com.mktekhub.inventory.repository.WarehouseRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private InventoryItem item;
    private Warehouse warehouse;
    private StockActivity activity;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setName("Test Warehouse");
        warehouse.setLocation("Test Location");
        warehouse.setMaxCapacity(new BigDecimal("1000.00"));
        warehouse.setCurrentCapacity(new BigDecimal("500.00"));
        warehouse.setCapacityAlertThreshold(new BigDecimal("80.00"));
        warehouse.setIsActive(true);
        warehouse.setCreatedAt(LocalDateTime.now());

        item = new InventoryItem();
        item.setId(1L);
        item.setSku("TEST-001");
        item.setName("Test Item");
        item.setCategory("Test Category");
        item.setQuantity(100);
        item.setReorderLevel(10);
        item.setWarehouse(warehouse);

        activity = new StockActivity();
        activity.setId(1L);
        activity.setItemSku("TEST-001");
        activity.setItem(item);
        activity.setActivityType(com.mktekhub.inventory.model.ActivityType.ADJUSTMENT);
        activity.setTimestamp(LocalDateTime.now());
        activity.setPerformedBy(new com.mktekhub.inventory.model.User());
    }

    @Test
    void testExportInventoryToCSV_ReturnsByteArray() {
        // Arrange
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item));

        // Act
        byte[] result = reportService.exportInventoryToCSV();

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(inventoryItemRepository, times(1)).findAll();
    }

    @Test
    void testExportWarehousesToCSV_ReturnsByteArray() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(Arrays.asList(warehouse));

        // Act
        byte[] result = reportService.exportWarehousesToCSV();

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(warehouseRepository, times(1)).findAll();
    }

    @Test
    void testExportStockActivitiesToCSV_ReturnsByteArray() {
        // Arrange
        when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity));

        // Act
        byte[] result = reportService.exportStockActivitiesToCSV();

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(stockActivityRepository, times(1)).findAll();
    }

    @Test
    void testGenerateLowStockReport_ReturnsByteArray() {
        // Arrange
        item.setQuantity(5); // Below reorder level
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item));

        // Act
        byte[] result = reportService.generateLowStockReport();

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(inventoryItemRepository, times(1)).findAll();
    }

    @Test
    void testGenerateStockValuationReport_ReturnsByteArray() {
        // Arrange
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item));

        // Act
        byte[] result = reportService.generateStockValuationReport();

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(inventoryItemRepository, times(1)).findAll();
    }

    @Test
    void testGenerateWarehouseUtilizationReport_ReturnsByteArray() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(Arrays.asList(warehouse));

        // Act
        byte[] result = reportService.generateWarehouseUtilizationReport();

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(warehouseRepository, times(1)).findAll();
    }

    @Test
    void testGenerateStockMovementReport_ReturnsByteArray() {
        // Arrange
        when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity));

        // Act
        byte[] result = reportService.generateStockMovementReport();

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(stockActivityRepository, times(1)).findAll();
    }

    @Test
    void testGenerateInventorySummaryByCategory_ReturnsByteArray() {
        // Arrange
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item));

        // Act
        byte[] result = reportService.generateInventorySummaryByCategory();

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(inventoryItemRepository, times(1)).findAll();
    }
}
