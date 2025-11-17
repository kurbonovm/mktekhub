package com.mktekhub.inventory.controller;

import com.mktekhub.inventory.config.TestSecurityConfig;
import com.mktekhub.inventory.dto.DashboardSummary;
import com.mktekhub.inventory.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@DisplayName("DashboardController Tests")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    @DisplayName("GET /api/dashboard/summary - Success")
    @WithMockUser
    void getDashboardSummary() throws Exception {
        DashboardSummary summary = new DashboardSummary();
        when(dashboardService.getDashboardSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/dashboard/summary"))
            .andExpect(status().isOk());

        verify(dashboardService).getDashboardSummary();
    }

    @Test
    @DisplayName("GET /api/dashboard/warehouse-summary - Success")
    @WithMockUser
    void getWarehouseSummary() throws Exception {
        DashboardSummary.WarehouseSummary summary = new DashboardSummary.WarehouseSummary(
            5, 3, BigDecimal.valueOf(10000), BigDecimal.valueOf(7550), BigDecimal.valueOf(75.5), 2
        );
        when(dashboardService.getWarehouseSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/dashboard/warehouse-summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalWarehouses").value(5));

        verify(dashboardService).getWarehouseSummary();
    }

    @Test
    @DisplayName("GET /api/dashboard/inventory-summary - Success")
    @WithMockUser
    void getInventorySummary() throws Exception {
        DashboardSummary.InventorySummary summary = new DashboardSummary.InventorySummary(
            100, 5000, BigDecimal.valueOf(250000), 50, 10
        );
        when(dashboardService.getInventorySummary()).thenReturn(summary);

        mockMvc.perform(get("/api/dashboard/inventory-summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalItems").value(100));

        verify(dashboardService).getInventorySummary();
    }

    @Test
    @DisplayName("GET /api/dashboard/alerts-summary - Success")
    @WithMockUser
    void getAlertsSummary() throws Exception {
        DashboardSummary.AlertsSummary summary = new DashboardSummary.AlertsSummary(
            10, 5, 8, 2
        );
        when(dashboardService.getAlertsSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/dashboard/alerts-summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lowStockItems").value(10));

        verify(dashboardService).getAlertsSummary();
    }
}
