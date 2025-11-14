package com.mktekhub.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mktekhub.inventory.dto.DashboardSummary;
import com.mktekhub.inventory.model.InventoryItem;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.InventoryItemRepository;
import com.mktekhub.inventory.repository.WarehouseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        warehouse1 = new Warehouse();
        warehouse1.setId(1L);
        warehouse1.setName("Warehouse 1");
        warehouse1.setMaxCapacity(new BigDecimal("1000.00"));
        warehouse1.setCurrentCapacity(new BigDecimal("500.00"));
        warehouse1.setIsActive(true);

        warehouse2 = new Warehouse();
        warehouse2.setId(2L);
        warehouse2.setName("Warehouse 2");
        warehouse2.setMaxCapacity(new BigDecimal("2000.00"));
        warehouse2.setCurrentCapacity(new BigDecimal("1500.00"));
        warehouse2.setIsActive(true);

        item1 = new InventoryItem();
        item1.setId(1L);
        item1.setQuantity(100);
        item1.setUnitPrice(new BigDecimal("10.00"));

        item2 = new InventoryItem();
        item2.setId(2L);
        item2.setQuantity(200);
        item2.setUnitPrice(new BigDecimal("20.00"));
    }

    @Test
    void testGetDashboardSummary_ReturnsSummary() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(Arrays.asList(warehouse1, warehouse2));
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(warehouse1, warehouse2));
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(Arrays.asList(warehouse2));
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));
        when(inventoryItemRepository.findLowStockItems()).thenReturn(Arrays.asList(item1));

        // Act
        DashboardSummary result = dashboardService.getDashboardSummary();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getWarehouseSummary());
        assertNotNull(result.getInventorySummary());
        assertNotNull(result.getAlertsSummary());
        verify(warehouseRepository, times(1)).findAll();
        verify(inventoryItemRepository, times(1)).findAll();
    }

    @Test
    void testGetWarehouseSummary_ReturnsSummary() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(Arrays.asList(warehouse1, warehouse2));
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(warehouse1, warehouse2));
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(Arrays.asList(warehouse2));

        // Act
        DashboardSummary.WarehouseSummary result = dashboardService.getWarehouseSummary();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalWarehouses());
        assertEquals(2, result.getActiveWarehouses());
        assertEquals(1, result.getWarehousesWithAlerts());
        assertEquals(new BigDecimal("3000.00"), result.getTotalCapacity());
    }

    @Test
    void testGetInventorySummary_ReturnsSummary() {
        // Arrange
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        // Act
        DashboardSummary.InventorySummary result = dashboardService.getInventorySummary();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalItems());
        verify(inventoryItemRepository, times(1)).findAll();
    }

    @Test
    void testGetAlertsSummary_ReturnsSummary() {
        // Arrange
        when(inventoryItemRepository.findLowStockItems()).thenReturn(Arrays.asList(item1));
        when(inventoryItemRepository.findByExpirationDateBefore(any(LocalDate.class))).thenReturn(Arrays.asList());
        when(inventoryItemRepository.findByExpirationDateBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(Arrays.asList());
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(Arrays.asList());

        // Act
        DashboardSummary.AlertsSummary result = dashboardService.getAlertsSummary();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getLowStockItems());
        assertEquals(0, result.getExpiredItems());
        assertEquals(0, result.getExpiringSoonItems());
        assertEquals(0, result.getCapacityAlerts());
        verify(inventoryItemRepository, times(1)).findLowStockItems();
    }
}
