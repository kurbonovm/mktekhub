package com.mktekhub.inventory.controller;

import com.mktekhub.inventory.config.TestSecurityConfig;
import com.mktekhub.inventory.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@DisplayName("ReportController Tests")
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    private byte[] csvData;

    @BeforeEach
    void setUp() {
        csvData = "Header1,Header2\nValue1,Value2".getBytes();
    }

    @Test
    @DisplayName("GET /api/reports/export/inventory - Success")
    @WithMockUser
    void exportInventory_Success() throws Exception {
        when(reportService.exportInventoryToCSV()).thenReturn(csvData);

        mockMvc.perform(get("/api/reports/export/inventory"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(content().bytes(csvData));

        verify(reportService).exportInventoryToCSV();
    }

    @Test
    @DisplayName("GET /api/reports/export/warehouses - Success")
    @WithMockUser
    void exportWarehouses_Success() throws Exception {
        when(reportService.exportWarehousesToCSV()).thenReturn(csvData);

        mockMvc.perform(get("/api/reports/export/warehouses"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(content().bytes(csvData));

        verify(reportService).exportWarehousesToCSV();
    }

    @Test
    @DisplayName("GET /api/reports/export/activities - Success")
    @WithMockUser
    void exportStockActivities_Success() throws Exception {
        when(reportService.exportStockActivitiesToCSV()).thenReturn(csvData);

        mockMvc.perform(get("/api/reports/export/activities"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(content().bytes(csvData));

        verify(reportService).exportStockActivitiesToCSV();
    }

    @Test
    @DisplayName("GET /api/reports/valuation - Success")
    @WithMockUser
    void generateValuationReport_Success() throws Exception {
        when(reportService.generateStockValuationReport()).thenReturn(csvData);

        mockMvc.perform(get("/api/reports/valuation"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(content().bytes(csvData));

        verify(reportService).generateStockValuationReport();
    }

    @Test
    @DisplayName("GET /api/reports/low-stock - Success")
    @WithMockUser
    void generateLowStockReport_Success() throws Exception {
        when(reportService.generateLowStockReport()).thenReturn(csvData);

        mockMvc.perform(get("/api/reports/low-stock"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(content().bytes(csvData));

        verify(reportService).generateLowStockReport();
    }

    @Test
    @DisplayName("GET /api/reports/warehouse-utilization - Success")
    @WithMockUser
    void generateWarehouseUtilizationReport_Success() throws Exception {
        when(reportService.generateWarehouseUtilizationReport()).thenReturn(csvData);

        mockMvc.perform(get("/api/reports/warehouse-utilization"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(content().bytes(csvData));

        verify(reportService).generateWarehouseUtilizationReport();
    }

    @Test
    @DisplayName("GET /api/reports/stock-movement - Success")
    @WithMockUser
    void generateStockMovementReport_Success() throws Exception {
        when(reportService.generateStockMovementReport()).thenReturn(csvData);

        mockMvc.perform(get("/api/reports/stock-movement"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(content().bytes(csvData));

        verify(reportService).generateStockMovementReport();
    }

    @Test
    @DisplayName("GET /api/reports/inventory-by-category - Success")
    @WithMockUser
    void generateInventorySummaryByCategory_Success() throws Exception {
        when(reportService.generateInventorySummaryByCategory()).thenReturn(csvData);

        mockMvc.perform(get("/api/reports/inventory-by-category"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(content().bytes(csvData));

        verify(reportService).generateInventorySummaryByCategory();
    }

    @Test
    @DisplayName("GET /api/reports/custom/stock-activity - Success with all params")
    @WithMockUser
    void generateFilteredStockActivityReport_AllParams() throws Exception {
        when(reportService.generateFilteredStockActivityReport(
            any(Optional.class), any(Optional.class), any(Optional.class),
            any(Optional.class), any(Optional.class), any(Optional.class)))
            .thenReturn(csvData);

        mockMvc.perform(get("/api/reports/custom/stock-activity")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .param("category", "Electronics")
                .param("brand", "TestBrand")
                .param("warehouseId", "1")
                .param("activityType", "TRANSFER"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(content().bytes(csvData));

        verify(reportService).generateFilteredStockActivityReport(
            any(Optional.class), any(Optional.class), any(Optional.class),
            any(Optional.class), any(Optional.class), any(Optional.class));
    }

    @Test
    @DisplayName("GET /api/reports/custom/stock-activity - Success with no params")
    @WithMockUser
    void generateFilteredStockActivityReport_NoParams() throws Exception {
        when(reportService.generateFilteredStockActivityReport(
            any(Optional.class), any(Optional.class), any(Optional.class),
            any(Optional.class), any(Optional.class), any(Optional.class)))
            .thenReturn(csvData);

        mockMvc.perform(get("/api/reports/custom/stock-activity"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(content().bytes(csvData));

        verify(reportService).generateFilteredStockActivityReport(
            eq(Optional.empty()), eq(Optional.empty()), eq(Optional.empty()),
            eq(Optional.empty()), eq(Optional.empty()), eq(Optional.empty()));
    }

    @Test
    @DisplayName("GET /api/reports/custom/stock-activity - Success with partial params")
    @WithMockUser
    void generateFilteredStockActivityReport_PartialParams() throws Exception {
        when(reportService.generateFilteredStockActivityReport(
            any(Optional.class), any(Optional.class), any(Optional.class),
            any(Optional.class), any(Optional.class), any(Optional.class)))
            .thenReturn(csvData);

        mockMvc.perform(get("/api/reports/custom/stock-activity")
                .param("category", "Electronics")
                .param("warehouseId", "1"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(content().bytes(csvData));

        verify(reportService).generateFilteredStockActivityReport(
            any(Optional.class), any(Optional.class), any(Optional.class),
            any(Optional.class), any(Optional.class), any(Optional.class));
    }

    @Test
    @DisplayName("GET /api/reports/custom/inventory-valuation - Success with all params")
    @WithMockUser
    void generateFilteredInventoryValuationReport_AllParams() throws Exception {
        when(reportService.generateFilteredInventoryValuationReport(
            any(Optional.class), any(Optional.class), any(Optional.class)))
            .thenReturn(csvData);

        mockMvc.perform(get("/api/reports/custom/inventory-valuation")
                .param("category", "Electronics")
                .param("brand", "TestBrand")
                .param("warehouseId", "1"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(content().bytes(csvData));

        verify(reportService).generateFilteredInventoryValuationReport(
            any(Optional.class), any(Optional.class), any(Optional.class));
    }

    @Test
    @DisplayName("GET /api/reports/custom/inventory-valuation - Success with no params")
    @WithMockUser
    void generateFilteredInventoryValuationReport_NoParams() throws Exception {
        when(reportService.generateFilteredInventoryValuationReport(
            any(Optional.class), any(Optional.class), any(Optional.class)))
            .thenReturn(csvData);

        mockMvc.perform(get("/api/reports/custom/inventory-valuation"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(content().bytes(csvData));

        verify(reportService).generateFilteredInventoryValuationReport(
            eq(Optional.empty()), eq(Optional.empty()), eq(Optional.empty()));
    }

    @Test
    @DisplayName("GET /api/reports/custom/inventory-valuation - Success with partial params")
    @WithMockUser
    void generateFilteredInventoryValuationReport_PartialParams() throws Exception {
        when(reportService.generateFilteredInventoryValuationReport(
            any(Optional.class), any(Optional.class), any(Optional.class)))
            .thenReturn(csvData);

        mockMvc.perform(get("/api/reports/custom/inventory-valuation")
                .param("category", "Electronics"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(content().bytes(csvData));

        verify(reportService).generateFilteredInventoryValuationReport(
            any(Optional.class), any(Optional.class), any(Optional.class));
    }

    @Test
    @DisplayName("GET /api/reports/export/inventory - Empty data")
    @WithMockUser
    void exportInventory_EmptyData() throws Exception {
        when(reportService.exportInventoryToCSV()).thenReturn(new byte[0]);

        mockMvc.perform(get("/api/reports/export/inventory"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/csv"))
            .andExpect(content().bytes(new byte[0]));

        verify(reportService).exportInventoryToCSV();
    }

    @Test
    @DisplayName("GET /api/reports/export/warehouses - Empty data")
    @WithMockUser
    void exportWarehouses_EmptyData() throws Exception {
        when(reportService.exportWarehousesToCSV()).thenReturn(new byte[0]);

        mockMvc.perform(get("/api/reports/export/warehouses"))
            .andExpect(status().isOk())
            .andExpect(content().bytes(new byte[0]));

        verify(reportService).exportWarehousesToCSV();
    }

    @Test
    @DisplayName("GET /api/reports/low-stock - Empty data")
    @WithMockUser
    void generateLowStockReport_EmptyData() throws Exception {
        when(reportService.generateLowStockReport()).thenReturn(new byte[0]);

        mockMvc.perform(get("/api/reports/low-stock"))
            .andExpect(status().isOk())
            .andExpect(content().bytes(new byte[0]));

        verify(reportService).generateLowStockReport();
    }
}
