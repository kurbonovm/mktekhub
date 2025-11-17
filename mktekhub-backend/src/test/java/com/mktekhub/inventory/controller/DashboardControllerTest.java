package com.mktekhub.inventory.controller;

import com.mktekhub.inventory.dto.DashboardSummary;
import com.mktekhub.inventory.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    @Test
    void testGetDashboardSummary() throws Exception {
        // Arrange
        DashboardSummary summary = new DashboardSummary();
        when(dashboardService.getDashboardSummary()).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk());

        verify(dashboardService, times(1)).getDashboardSummary();
    }

    @Test
    void testGetWarehouseSummary() throws Exception {
        // Arrange
        DashboardSummary.WarehouseSummary summary = new DashboardSummary.WarehouseSummary();
        summary.setTotalWarehouses(5);
        summary.setActiveWarehouses(4);
        summary.setWarehousesWithAlerts(1);

        when(dashboardService.getWarehouseSummary()).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(get("/api/dashboard/warehouse-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWarehouses").value(5))
                .andExpect(jsonPath("$.activeWarehouses").value(4))
                .andExpect(jsonPath("$.warehousesWithAlerts").value(1));

        verify(dashboardService, times(1)).getWarehouseSummary();
    }

    @Test
    void testGetInventorySummary() throws Exception {
        // Arrange
        DashboardSummary.InventorySummary summary = new DashboardSummary.InventorySummary();
        summary.setTotalItems(100);
        summary.setTotalQuantity(1000);
        summary.setUniqueSkus(50);

        when(dashboardService.getInventorySummary()).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(get("/api/dashboard/inventory-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(100))
                .andExpect(jsonPath("$.totalQuantity").value(1000))
                .andExpect(jsonPath("$.uniqueSkus").value(50));

        verify(dashboardService, times(1)).getInventorySummary();
    }

    @Test
    void testGetAlertsSummary() throws Exception {
        // Arrange
        DashboardSummary.AlertsSummary summary = new DashboardSummary.AlertsSummary();
        summary.setLowStockItems(5);
        summary.setExpiredItems(2);
        summary.setExpiringSoonItems(3);
        summary.setCapacityAlerts(1);

        when(dashboardService.getAlertsSummary()).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(get("/api/dashboard/alerts-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lowStockItems").value(5))
                .andExpect(jsonPath("$.expiredItems").value(2))
                .andExpect(jsonPath("$.expiringSoonItems").value(3))
                .andExpect(jsonPath("$.capacityAlerts").value(1));

        verify(dashboardService, times(1)).getAlertsSummary();
    }
}
