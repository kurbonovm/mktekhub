package com.mktekhub.inventory.controller;

import com.mktekhub.inventory.service.ReportService;
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
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
    }

    @Test
    void testExportInventory() throws Exception {
        // Arrange
        byte[] csvData = "id,sku,name\n1,SKU001,Item1".getBytes();
        when(reportService.exportInventoryToCSV()).thenReturn(csvData);

        // Act & Assert
        mockMvc.perform(get("/api/reports/export/inventory"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().exists("Content-Disposition"));

        verify(reportService, times(1)).exportInventoryToCSV();
    }

    @Test
    void testExportWarehouses() throws Exception {
        // Arrange
        byte[] csvData = "id,name,location\n1,Warehouse1,Location1".getBytes();
        when(reportService.exportWarehousesToCSV()).thenReturn(csvData);

        // Act & Assert
        mockMvc.perform(get("/api/reports/export/warehouses"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().exists("Content-Disposition"));

        verify(reportService, times(1)).exportWarehousesToCSV();
    }

    @Test
    void testExportStockActivities() throws Exception {
        // Arrange
        byte[] csvData = "id,activity_type,item_sku\n1,RECEIVE,SKU001".getBytes();
        when(reportService.exportStockActivitiesToCSV()).thenReturn(csvData);

        // Act & Assert
        mockMvc.perform(get("/api/reports/export/activities"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().exists("Content-Disposition"));

        verify(reportService, times(1)).exportStockActivitiesToCSV();
    }

    @Test
    void testGenerateValuationReport() throws Exception {
        // Arrange
        byte[] csvData = "sku,quantity,value\nSKU001,100,1000.00".getBytes();
        when(reportService.generateStockValuationReport()).thenReturn(csvData);

        // Act & Assert
        mockMvc.perform(get("/api/reports/valuation"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().exists("Content-Disposition"));

        verify(reportService, times(1)).generateStockValuationReport();
    }

    @Test
    void testGenerateLowStockReport() throws Exception {
        // Arrange
        byte[] csvData = "sku,quantity,threshold\nSKU001,5,10".getBytes();
        when(reportService.generateLowStockReport()).thenReturn(csvData);

        // Act & Assert
        mockMvc.perform(get("/api/reports/low-stock"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().exists("Content-Disposition"));

        verify(reportService, times(1)).generateLowStockReport();
    }

    @Test
    void testGenerateWarehouseUtilizationReport() throws Exception {
        // Arrange
        byte[] csvData = "warehouse,capacity,utilization\nWarehouse1,1000,75.5".getBytes();
        when(reportService.generateWarehouseUtilizationReport()).thenReturn(csvData);

        // Act & Assert
        mockMvc.perform(get("/api/reports/warehouse-utilization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().exists("Content-Disposition"));

        verify(reportService, times(1)).generateWarehouseUtilizationReport();
    }

    @Test
    void testGenerateStockMovementReport() throws Exception {
        // Arrange
        byte[] csvData = "date,item,movement\n2024-01-01,SKU001,10".getBytes();
        when(reportService.generateStockMovementReport()).thenReturn(csvData);

        // Act & Assert
        mockMvc.perform(get("/api/reports/stock-movement"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().exists("Content-Disposition"));

        verify(reportService, times(1)).generateStockMovementReport();
    }

    @Test
    void testGenerateInventorySummaryByCategory() throws Exception {
        // Arrange
        byte[] csvData = "category,items,value\nElectronics,50,5000.00".getBytes();
        when(reportService.generateInventorySummaryByCategory()).thenReturn(csvData);

        // Act & Assert
        mockMvc.perform(get("/api/reports/inventory-by-category"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().exists("Content-Disposition"));

        verify(reportService, times(1)).generateInventorySummaryByCategory();
    }

    @Test
    void testGenerateFilteredStockActivityReport() throws Exception {
        // Arrange
        byte[] csvData = "date,activity,item\n2024-01-01,RECEIVE,SKU001".getBytes();
        when(reportService.generateFilteredStockActivityReport(any(), any(), any(), any(), any(), any()))
                .thenReturn(csvData);

        // Act & Assert
        mockMvc.perform(get("/api/reports/custom/stock-activity")
                .param("category", "Electronics")
                .param("brand", "BrandX"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().exists("Content-Disposition"));

        verify(reportService, times(1))
                .generateFilteredStockActivityReport(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testGenerateFilteredInventoryValuationReport() throws Exception {
        // Arrange
        byte[] csvData = "sku,value\nSKU001,1000.00".getBytes();
        when(reportService.generateFilteredInventoryValuationReport(any(), any(), any()))
                .thenReturn(csvData);

        // Act & Assert
        mockMvc.perform(get("/api/reports/custom/inventory-valuation")
                .param("category", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().exists("Content-Disposition"));

        verify(reportService, times(1))
                .generateFilteredInventoryValuationReport(any(), any(), any());
    }
}
