package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.DashboardSummary;
import com.mktekhub.inventory.model.InventoryItem;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.InventoryItemRepository;
import com.mktekhub.inventory.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        warehouse1.setIsActive(true);

        warehouse2 = new Warehouse();
        warehouse2.setId(2L);
        warehouse2.setName("Warehouse 2");
        warehouse2.setMaxCapacity(new BigDecimal("8000.00"));
        warehouse2.setCurrentCapacity(new BigDecimal("6000.00"));
        warehouse2.setIsActive(true);

        // Setup inventory items
        item1 = new InventoryItem();
        item1.setId(1L);
        item1.setSku("SKU001");
        item1.setName("Item 1");
        item1.setCategory("Electronics");
        item1.setQuantity(100);
        item1.setUnitPrice(new BigDecimal("10.00"));
        item1.setReorderLevel(20);
        item1.setWarehouse(warehouse1);

        item2 = new InventoryItem();
        item2.setId(2L);
        item2.setSku("SKU002");
        item2.setName("Item 2");
        item2.setCategory("Clothing");
        item2.setQuantity(50);
        item2.setUnitPrice(new BigDecimal("25.00"));
        item2.setReorderLevel(60);
        item2.setWarehouse(warehouse2);
    }

    @Test
    void getDashboardSummary_ReturnsCompleteSummary() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(Arrays.asList(warehouse1, warehouse2));
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(warehouse1, warehouse2));
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(List.of(warehouse2));
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));
        when(inventoryItemRepository.findLowStockItems()).thenReturn(List.of(item2));
        when(inventoryItemRepository.findByExpirationDateBefore(any(LocalDate.class))).thenReturn(List.of());
        when(inventoryItemRepository.findByExpirationDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        // Act
        DashboardSummary result = dashboardService.getDashboardSummary();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getWarehouseSummary());
        assertNotNull(result.getInventorySummary());
        assertNotNull(result.getAlertsSummary());
    }

    @Test
    void getWarehouseSummary_CalculatesCorrectly() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(Arrays.asList(warehouse1, warehouse2));
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(warehouse1, warehouse2));
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(List.of(warehouse2));

        // Act
        DashboardSummary.WarehouseSummary result = dashboardService.getWarehouseSummary();

        // Assert
        assertEquals(2, result.getTotalWarehouses());
        assertEquals(2, result.getActiveWarehouses());
        assertEquals(new BigDecimal("18000.00"), result.getTotalCapacity());
        assertEquals(new BigDecimal("11000.00"), result.getUsedCapacity());
        assertEquals(1, result.getWarehousesWithAlerts());
        verify(warehouseRepository).findAll();
        verify(warehouseRepository).findByIsActiveTrue();
        verify(warehouseRepository).findWarehousesWithCapacityAlert();
    }

    @Test
    void getWarehouseSummary_CalculatesAverageUtilization() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(Arrays.asList(warehouse1, warehouse2));
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(warehouse1, warehouse2));
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(List.of());

        // Act
        DashboardSummary.WarehouseSummary result = dashboardService.getWarehouseSummary();

        // Assert
        assertNotNull(result.getAverageUtilization());
        assertTrue(result.getAverageUtilization().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void getWarehouseSummary_HandlesEmptyList() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(List.of());
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(List.of());
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(List.of());

        // Act
        DashboardSummary.WarehouseSummary result = dashboardService.getWarehouseSummary();

        // Assert
        assertEquals(0, result.getTotalWarehouses());
        assertEquals(0, result.getActiveWarehouses());
        assertEquals(BigDecimal.ZERO, result.getTotalCapacity());
        assertEquals(BigDecimal.ZERO, result.getUsedCapacity());
        assertEquals(BigDecimal.ZERO, result.getAverageUtilization());
    }

    @Test
    void getInventorySummary_CalculatesCorrectly() {
        // Arrange
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        // Act
        DashboardSummary.InventorySummary result = dashboardService.getInventorySummary();

        // Assert
        assertEquals(2, result.getTotalItems());
        assertEquals(150, result.getTotalQuantity()); // 100 + 50
        assertEquals(new BigDecimal("2250.00"), result.getTotalValue()); // (100*10) + (50*25)
        assertEquals(2, result.getUniqueSkus());
        assertEquals(2, result.getCategoriesCount());
        verify(inventoryItemRepository).findAll();
    }

    @Test
    void getInventorySummary_HandlesNullPrices() {
        // Arrange
        item1.setUnitPrice(null);
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        // Act
        DashboardSummary.InventorySummary result = dashboardService.getInventorySummary();

        // Assert
        assertEquals(new BigDecimal("1250.00"), result.getTotalValue()); // Only item2: 50*25
    }

    @Test
    void getInventorySummary_CountsUniqueSkus() {
        // Arrange
        InventoryItem item3 = new InventoryItem();
        item3.setSku("SKU001"); // Same SKU as item1
        item3.setQuantity(10);
        item3.setWarehouse(warehouse1);

        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2, item3));

        // Act
        DashboardSummary.InventorySummary result = dashboardService.getInventorySummary();

        // Assert
        assertEquals(3, result.getTotalItems());
        assertEquals(2, result.getUniqueSkus()); // SKU001 and SKU002
    }

    @Test
    void getInventorySummary_HandlesNullCategories() {
        // Arrange
        item1.setCategory(null);
        item2.setCategory("");
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        // Act
        DashboardSummary.InventorySummary result = dashboardService.getInventorySummary();

        // Assert
        assertEquals(0, result.getCategoriesCount());
    }

    @Test
    void getAlertsSummary_CalculatesCorrectly() {
        // Arrange
        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysFromNow = now.plusDays(30);

        when(inventoryItemRepository.findLowStockItems()).thenReturn(List.of(item2));
        when(inventoryItemRepository.findByExpirationDateBefore(now)).thenReturn(List.of(item1));
        when(inventoryItemRepository.findByExpirationDateBetween(now, thirtyDaysFromNow))
                .thenReturn(List.of(item2));
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(List.of(warehouse2));

        // Act
        DashboardSummary.AlertsSummary result = dashboardService.getAlertsSummary();

        // Assert
        assertEquals(1, result.getLowStockItems());
        assertEquals(1, result.getExpiredItems());
        assertEquals(1, result.getExpiringSoonItems());
        assertEquals(1, result.getCapacityAlerts());
    }

    @Test
    void getAlertsSummary_HandlesNoAlerts() {
        // Arrange
        when(inventoryItemRepository.findLowStockItems()).thenReturn(List.of());
        when(inventoryItemRepository.findByExpirationDateBefore(any(LocalDate.class))).thenReturn(List.of());
        when(inventoryItemRepository.findByExpirationDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(List.of());

        // Act
        DashboardSummary.AlertsSummary result = dashboardService.getAlertsSummary();

        // Assert
        assertEquals(0, result.getLowStockItems());
        assertEquals(0, result.getExpiredItems());
        assertEquals(0, result.getExpiringSoonItems());
        assertEquals(0, result.getCapacityAlerts());
    }

    @Test
    void getAlertsSummary_Uses30DayWindow() {
        // Arrange
        LocalDate now = LocalDate.now();
        LocalDate expectedEndDate = now.plusDays(30);

        when(inventoryItemRepository.findLowStockItems()).thenReturn(List.of());
        when(inventoryItemRepository.findByExpirationDateBefore(any(LocalDate.class))).thenReturn(List.of());
        when(inventoryItemRepository.findByExpirationDateBetween(eq(now), eq(expectedEndDate)))
                .thenReturn(List.of());
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(List.of());

        // Act
        dashboardService.getAlertsSummary();

        // Assert
        verify(inventoryItemRepository).findByExpirationDateBetween(eq(now), eq(expectedEndDate));
    }
}
