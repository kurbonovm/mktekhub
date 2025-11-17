package com.mktekhub.inventory.controller;

import com.mktekhub.inventory.dto.CombinedAlertsResponse;
import com.mktekhub.inventory.dto.InventoryItemResponse;
import com.mktekhub.inventory.dto.WarehouseResponse;
import com.mktekhub.inventory.service.InventoryItemService;
import com.mktekhub.inventory.service.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AlertsControllerTest {

    @Mock
    private InventoryItemService inventoryItemService;

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private AlertsController alertsController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alertsController).build();
    }

    @Test
    void testGetAllAlerts() throws Exception {
        // Arrange
        InventoryItemResponse lowStockItem = new InventoryItemResponse();
        lowStockItem.setId(1L);
        lowStockItem.setSku("SKU001");
        lowStockItem.setQuantity(5);

        InventoryItemResponse expiredItem = new InventoryItemResponse();
        expiredItem.setId(2L);
        expiredItem.setSku("SKU002");

        InventoryItemResponse expiringSoonItem = new InventoryItemResponse();
        expiringSoonItem.setId(3L);
        expiringSoonItem.setSku("SKU003");

        WarehouseResponse warehouseAlert = new WarehouseResponse();
        warehouseAlert.setId(1L);
        warehouseAlert.setName("Warehouse 1");

        List<InventoryItemResponse> lowStockItems = Arrays.asList(lowStockItem);
        List<InventoryItemResponse> expiredItems = Arrays.asList(expiredItem);
        List<InventoryItemResponse> expiringSoonItems = Arrays.asList(expiringSoonItem);
        List<WarehouseResponse> capacityAlerts = Arrays.asList(warehouseAlert);

        when(inventoryItemService.getLowStockItems()).thenReturn(lowStockItems);
        when(inventoryItemService.getExpiredItems()).thenReturn(expiredItems);
        when(inventoryItemService.getItemsExpiringSoon(30)).thenReturn(expiringSoonItems);
        when(warehouseService.getWarehousesWithAlerts()).thenReturn(capacityAlerts);

        // Act & Assert
        mockMvc.perform(get("/api/alerts/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lowStockItems[0].id").value(1))
                .andExpect(jsonPath("$.lowStockItems[0].sku").value("SKU001"))
                .andExpect(jsonPath("$.expiredItems[0].id").value(2))
                .andExpect(jsonPath("$.expiringSoonItems[0].id").value(3))
                .andExpect(jsonPath("$.capacityAlerts[0].id").value(1));

        verify(inventoryItemService, times(1)).getLowStockItems();
        verify(inventoryItemService, times(1)).getExpiredItems();
        verify(inventoryItemService, times(1)).getItemsExpiringSoon(30);
        verify(warehouseService, times(1)).getWarehousesWithAlerts();
    }

    @Test
    void testGetAllAlerts_WithCustomExpiringDays() throws Exception {
        // Arrange
        List<InventoryItemResponse> lowStockItems = Arrays.asList(new InventoryItemResponse());
        List<InventoryItemResponse> expiredItems = Arrays.asList(new InventoryItemResponse());
        List<InventoryItemResponse> expiringSoonItems = Arrays.asList(new InventoryItemResponse());
        List<WarehouseResponse> capacityAlerts = Arrays.asList(new WarehouseResponse());

        when(inventoryItemService.getLowStockItems()).thenReturn(lowStockItems);
        when(inventoryItemService.getExpiredItems()).thenReturn(expiredItems);
        when(inventoryItemService.getItemsExpiringSoon(60)).thenReturn(expiringSoonItems);
        when(warehouseService.getWarehousesWithAlerts()).thenReturn(capacityAlerts);

        // Act & Assert
        mockMvc.perform(get("/api/alerts/all")
                .param("expiringDays", "60"))
                .andExpect(status().isOk());

        verify(inventoryItemService, times(1)).getLowStockItems();
        verify(inventoryItemService, times(1)).getExpiredItems();
        verify(inventoryItemService, times(1)).getItemsExpiringSoon(60);
        verify(warehouseService, times(1)).getWarehousesWithAlerts();
    }

    @Test
    void testGetAllAlerts_EmptyAlerts() throws Exception {
        // Arrange
        when(inventoryItemService.getLowStockItems()).thenReturn(Arrays.asList());
        when(inventoryItemService.getExpiredItems()).thenReturn(Arrays.asList());
        when(inventoryItemService.getItemsExpiringSoon(30)).thenReturn(Arrays.asList());
        when(warehouseService.getWarehousesWithAlerts()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/alerts/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lowStockItems").isEmpty())
                .andExpect(jsonPath("$.expiredItems").isEmpty())
                .andExpect(jsonPath("$.expiringSoonItems").isEmpty())
                .andExpect(jsonPath("$.capacityAlerts").isEmpty());

        verify(inventoryItemService, times(1)).getLowStockItems();
        verify(inventoryItemService, times(1)).getExpiredItems();
        verify(inventoryItemService, times(1)).getItemsExpiringSoon(30);
        verify(warehouseService, times(1)).getWarehousesWithAlerts();
    }
}
