package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.DashboardSummary;
import com.mktekhub.inventory.model.InventoryItem;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.InventoryItemRepository;
import com.mktekhub.inventory.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for DashboardService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Tests")
class DashboardServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private Warehouse warehouse1;
    private Warehouse warehouse2;
    private InventoryItem item1;
    private InventoryItem item2;

    @BeforeEach
    void setUp() {
        // Setup warehouses
        warehouse1 = new Warehouse();
        warehouse1.setId(1L);
        warehouse1.setName("Warehouse 1");
        warehouse1.setMaxCapacity(new BigDecimal("10000.00"));
        warehouse1.setCurrentCapacity(new BigDecimal("5000.00"));
        warehouse1.setCapacityAlertThreshold(new BigDecimal("80.00"));
        warehouse1.setIsActive(true);

        warehouse2 = new Warehouse();
        warehouse2.setId(2L);
        warehouse2.setName("Warehouse 2");
        warehouse2.setMaxCapacity(new BigDecimal("5000.00"));
        warehouse2.setCurrentCapacity(new BigDecimal("4500.00")); // 90% - triggers alert
        warehouse2.setCapacityAlertThreshold(new BigDecimal("80.00"));
        warehouse2.setIsActive(true);

        // Setup inventory items
        item1 = new InventoryItem();
        item1.setId(1L);
        item1.setSku("SKU-001");
        item1.setName("Item 1");
        item1.setCategory("Electronics");
        item1.setQuantity(100);
        item1.setUnitPrice(new BigDecimal("50.00"));
        item1.setReorderLevel(20);
        item1.setWarehouse(warehouse1);

        item2 = new InventoryItem();
        item2.setId(2L);
        item2.setSku("SKU-002");
        item2.setName("Item 2");
        item2.setCategory("Electronics");
        item2.setQuantity(5); // Low stock
        item2.setUnitPrice(new BigDecimal("100.00"));
        item2.setReorderLevel(10);
        item2.setExpirationDate(LocalDate.now().minusDays(1)); // Expired
        item2.setWarehouse(warehouse1);
    }

    // ==================== GET DASHBOARD SUMMARY TESTS ====================

    @Test
    @DisplayName("GetDashboardSummary - Should return complete dashboard summary")
    void getDashboardSummary_Success() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(Arrays.asList(warehouse1, warehouse2));
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(warehouse1, warehouse2));
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(Collections.singletonList(warehouse2));
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));
        when(inventoryItemRepository.findLowStockItems()).thenReturn(Collections.singletonList(item2));
        when(inventoryItemRepository.findByExpirationDateBefore(any())).thenReturn(Collections.singletonList(item2));
        when(inventoryItemRepository.findByExpirationDateBetween(any(), any())).thenReturn(Collections.emptyList());

        // Act
        DashboardSummary summary = dashboardService.getDashboardSummary();

        // Assert
        assertNotNull(summary);
        assertNotNull(summary.getWarehouseSummary());
        assertNotNull(summary.getInventorySummary());
        assertNotNull(summary.getAlertsSummary());

        // Verify warehouse summary
        assertEquals(2, summary.getWarehouseSummary().getTotalWarehouses());
        assertEquals(2, summary.getWarehouseSummary().getActiveWarehouses());

        // Verify inventory summary
        assertEquals(2, summary.getInventorySummary().getTotalItems());
        assertEquals(105, summary.getInventorySummary().getTotalQuantity());

        // Verify alerts summary
        assertEquals(1, summary.getAlertsSummary().getLowStockItems());
        assertEquals(1, summary.getAlertsSummary().getExpiredItems());
        assertEquals(1, summary.getAlertsSummary().getCapacityAlerts());
    }

    // ==================== GET WAREHOUSE SUMMARY TESTS ====================

    @Test
    @DisplayName("GetWarehouseSummary - Should calculate warehouse statistics correctly")
    void getWarehouseSummary_Success() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(Arrays.asList(warehouse1, warehouse2));
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(warehouse1, warehouse2));
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(Collections.singletonList(warehouse2));

        // Act
        DashboardSummary.WarehouseSummary summary = dashboardService.getWarehouseSummary();

        // Assert
        assertNotNull(summary);
        assertEquals(2, summary.getTotalWarehouses());
        assertEquals(2, summary.getActiveWarehouses());
        assertEquals(new BigDecimal("15000.00"), summary.getTotalCapacity());
        assertEquals(new BigDecimal("9500.00"), summary.getUsedCapacity());
        assertEquals(1, summary.getWarehousesWithAlerts());

        // Average utilization = (50% + 90%) / 2 = 70%
        assertEquals(new BigDecimal("70.00"), summary.getAverageUtilization());
    }

    @Test
    @DisplayName("GetWarehouseSummary - Should handle empty warehouse list")
    void getWarehouseSummary_EmptyList() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(Collections.emptyList());
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(Collections.emptyList());

        // Act
        DashboardSummary.WarehouseSummary summary = dashboardService.getWarehouseSummary();

        // Assert
        assertNotNull(summary);
        assertEquals(0, summary.getTotalWarehouses());
        assertEquals(0, summary.getActiveWarehouses());
        assertEquals(BigDecimal.ZERO, summary.getTotalCapacity());
        assertEquals(BigDecimal.ZERO, summary.getUsedCapacity());
        assertEquals(BigDecimal.ZERO, summary.getAverageUtilization());
        assertEquals(0, summary.getWarehousesWithAlerts());
    }

    @Test
    @DisplayName("GetWarehouseSummary - Should handle single warehouse")
    void getWarehouseSummary_SingleWarehouse() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(Collections.singletonList(warehouse1));
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(Collections.singletonList(warehouse1));
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(Collections.emptyList());

        // Act
        DashboardSummary.WarehouseSummary summary = dashboardService.getWarehouseSummary();

        // Assert
        assertEquals(1, summary.getTotalWarehouses());
        assertEquals(new BigDecimal("50.00"), summary.getAverageUtilization()); // 5000/10000 = 50%
    }

    // ==================== GET INVENTORY SUMMARY TESTS ====================

    @Test
    @DisplayName("GetInventorySummary - Should calculate inventory statistics correctly")
    void getInventorySummary_Success() {
        // Arrange
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        // Act
        DashboardSummary.InventorySummary summary = dashboardService.getInventorySummary();

        // Assert
        assertNotNull(summary);
        assertEquals(2, summary.getTotalItems());
        assertEquals(105, summary.getTotalQuantity()); // 100 + 5
        assertEquals(2, summary.getUniqueSkus());
        assertEquals(1, summary.getCategoriesCount()); // Both are "Electronics"

        // Total value = (100 * 50) + (5 * 100) = 5500
        assertEquals(new BigDecimal("5500.00"), summary.getTotalValue());
    }

    @Test
    @DisplayName("GetInventorySummary - Should handle empty inventory")
    void getInventorySummary_EmptyList() {
        // Arrange
        when(inventoryItemRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        DashboardSummary.InventorySummary summary = dashboardService.getInventorySummary();

        // Assert
        assertNotNull(summary);
        assertEquals(0, summary.getTotalItems());
        assertEquals(0, summary.getTotalQuantity());
        assertEquals(BigDecimal.ZERO, summary.getTotalValue());
        assertEquals(0, summary.getUniqueSkus());
        assertEquals(0, summary.getCategoriesCount());
    }

    @Test
    @DisplayName("GetInventorySummary - Should handle items with null prices")
    void getInventorySummary_NullPrices() {
        // Arrange
        item1.setUnitPrice(null);
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        // Act
        DashboardSummary.InventorySummary summary = dashboardService.getInventorySummary();

        // Assert
        // Total value = (0) + (5 * 100) = 500
        assertEquals(new BigDecimal("500.00"), summary.getTotalValue());
    }

    @Test
    @DisplayName("GetInventorySummary - Should count unique SKUs correctly")
    void getInventorySummary_UniqueSKUs() {
        // Arrange
        InventoryItem item3 = new InventoryItem();
        item3.setId(3L);
        item3.setSku("SKU-001"); // Duplicate SKU
        item3.setName("Item 3");
        item3.setCategory("Furniture");
        item3.setQuantity(10);
        item3.setUnitPrice(new BigDecimal("200.00"));

        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2, item3));

        // Act
        DashboardSummary.InventorySummary summary = dashboardService.getInventorySummary();

        // Assert
        assertEquals(3, summary.getTotalItems());
        assertEquals(2, summary.getUniqueSkus()); // SKU-001 and SKU-002
        assertEquals(2, summary.getCategoriesCount()); // Electronics and Furniture
    }

    @Test
    @DisplayName("GetInventorySummary - Should handle null/empty categories")
    void getInventorySummary_NullCategories() {
        // Arrange
        item1.setCategory(null);
        item2.setCategory("");

        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        // Act
        DashboardSummary.InventorySummary summary = dashboardService.getInventorySummary();

        // Assert
        assertEquals(0, summary.getCategoriesCount());
    }

    // ==================== GET ALERTS SUMMARY TESTS ====================

    @Test
    @DisplayName("GetAlertsSummary - Should calculate alert counts correctly")
    void getAlertsSummary_Success() {
        // Arrange
        InventoryItem expiringSoonItem = new InventoryItem();
        expiringSoonItem.setExpirationDate(LocalDate.now().plusDays(15));

        when(inventoryItemRepository.findLowStockItems()).thenReturn(Collections.singletonList(item2));
        when(inventoryItemRepository.findByExpirationDateBefore(any())).thenReturn(Collections.singletonList(item2));
        when(inventoryItemRepository.findByExpirationDateBetween(any(), any()))
            .thenReturn(Collections.singletonList(expiringSoonItem));
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(Collections.singletonList(warehouse2));

        // Act
        DashboardSummary.AlertsSummary summary = dashboardService.getAlertsSummary();

        // Assert
        assertNotNull(summary);
        assertEquals(1, summary.getLowStockItems());
        assertEquals(1, summary.getExpiredItems());
        assertEquals(1, summary.getExpiringSoonItems());
        assertEquals(1, summary.getCapacityAlerts());
    }

    @Test
    @DisplayName("GetAlertsSummary - Should handle no alerts")
    void getAlertsSummary_NoAlerts() {
        // Arrange
        when(inventoryItemRepository.findLowStockItems()).thenReturn(Collections.emptyList());
        when(inventoryItemRepository.findByExpirationDateBefore(any())).thenReturn(Collections.emptyList());
        when(inventoryItemRepository.findByExpirationDateBetween(any(), any())).thenReturn(Collections.emptyList());
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(Collections.emptyList());

        // Act
        DashboardSummary.AlertsSummary summary = dashboardService.getAlertsSummary();

        // Assert
        assertNotNull(summary);
        assertEquals(0, summary.getLowStockItems());
        assertEquals(0, summary.getExpiredItems());
        assertEquals(0, summary.getExpiringSoonItems());
        assertEquals(0, summary.getCapacityAlerts());
    }

    @Test
    @DisplayName("GetAlertsSummary - Should use 30 days for expiring soon")
    void getAlertsSummary_ThirtyDaysWindow() {
        // Arrange
        when(inventoryItemRepository.findLowStockItems()).thenReturn(Collections.emptyList());
        when(inventoryItemRepository.findByExpirationDateBefore(any())).thenReturn(Collections.emptyList());
        when(inventoryItemRepository.findByExpirationDateBetween(any(), any())).thenReturn(Collections.emptyList());
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(Collections.emptyList());

        // Act
        dashboardService.getAlertsSummary();

        // Assert
        verify(inventoryItemRepository).findByExpirationDateBetween(
            argThat(date -> date.equals(LocalDate.now())),
            argThat(date -> date.equals(LocalDate.now().plusDays(30)))
        );
    }
}
