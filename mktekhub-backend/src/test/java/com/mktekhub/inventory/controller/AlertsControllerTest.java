package com.mktekhub.inventory.controller;

import com.mktekhub.inventory.config.TestSecurityConfig;
import com.mktekhub.inventory.dto.CombinedAlertsResponse;
import com.mktekhub.inventory.dto.InventoryItemResponse;
import com.mktekhub.inventory.dto.WarehouseResponse;
import com.mktekhub.inventory.service.InventoryItemService;
import com.mktekhub.inventory.service.WarehouseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@DisplayName("AlertsController Tests")
class AlertsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryItemService inventoryItemService;

    @MockBean
    private WarehouseService warehouseService;

    @Test
    @DisplayName("GET /api/alerts/all - Success with default days")
    @WithMockUser
    void getAllAlerts_DefaultDays() throws Exception {
        when(inventoryItemService.getLowStockItems()).thenReturn(Collections.emptyList());
        when(inventoryItemService.getExpiredItems()).thenReturn(Collections.emptyList());
        when(inventoryItemService.getItemsExpiringSoon(30)).thenReturn(Collections.emptyList());
        when(warehouseService.getWarehousesWithAlerts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/alerts/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lowStockItems").isArray())
            .andExpect(jsonPath("$.expiredItems").isArray())
            .andExpect(jsonPath("$.expiringSoonItems").isArray())
            .andExpect(jsonPath("$.capacityAlerts").isArray());

        verify(inventoryItemService).getLowStockItems();
        verify(inventoryItemService).getExpiredItems();
        verify(inventoryItemService).getItemsExpiringSoon(30);
        verify(warehouseService).getWarehousesWithAlerts();
    }

    @Test
    @DisplayName("GET /api/alerts/all - Success with custom days")
    @WithMockUser
    void getAllAlerts_CustomDays() throws Exception {
        when(inventoryItemService.getLowStockItems()).thenReturn(Collections.emptyList());
        when(inventoryItemService.getExpiredItems()).thenReturn(Collections.emptyList());
        when(inventoryItemService.getItemsExpiringSoon(60)).thenReturn(Collections.emptyList());
        when(warehouseService.getWarehousesWithAlerts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/alerts/all?expiringDays=60"))
            .andExpect(status().isOk());

        verify(inventoryItemService).getItemsExpiringSoon(60);
    }
}
