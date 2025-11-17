package com.mktekhub.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mktekhub.inventory.dto.WarehouseRequest;
import com.mktekhub.inventory.dto.WarehouseResponse;
import com.mktekhub.inventory.service.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WarehouseControllerTest {

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private WarehouseController warehouseController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(warehouseController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetAllWarehouses() throws Exception {
        // Arrange
        WarehouseResponse warehouse1 = new WarehouseResponse();
        warehouse1.setId(1L);
        warehouse1.setName("Warehouse 1");

        WarehouseResponse warehouse2 = new WarehouseResponse();
        warehouse2.setId(2L);
        warehouse2.setName("Warehouse 2");

        List<WarehouseResponse> warehouses = Arrays.asList(warehouse1, warehouse2);
        when(warehouseService.getAllWarehouses()).thenReturn(warehouses);

        // Act & Assert
        mockMvc.perform(get("/api/warehouses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Warehouse 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Warehouse 2"));

        verify(warehouseService, times(1)).getAllWarehouses();
    }

    @Test
    void testGetActiveWarehouses() throws Exception {
        // Arrange
        WarehouseResponse warehouse1 = new WarehouseResponse();
        warehouse1.setId(1L);
        warehouse1.setName("Active Warehouse");
        warehouse1.setIsActive(true);

        List<WarehouseResponse> warehouses = Arrays.asList(warehouse1);
        when(warehouseService.getActiveWarehouses()).thenReturn(warehouses);

        // Act & Assert
        mockMvc.perform(get("/api/warehouses/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].isActive").value(true));

        verify(warehouseService, times(1)).getActiveWarehouses();
    }

    @Test
    void testGetWarehousesWithAlerts() throws Exception {
        // Arrange
        WarehouseResponse warehouse1 = new WarehouseResponse();
        warehouse1.setId(1L);
        warehouse1.setName("Warehouse with Alert");

        List<WarehouseResponse> warehouses = Arrays.asList(warehouse1);
        when(warehouseService.getWarehousesWithAlerts()).thenReturn(warehouses);

        // Act & Assert
        mockMvc.perform(get("/api/warehouses/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(warehouseService, times(1)).getWarehousesWithAlerts();
    }

    @Test
    void testGetWarehouseById() throws Exception {
        // Arrange
        WarehouseResponse warehouse = new WarehouseResponse();
        warehouse.setId(1L);
        warehouse.setName("Test Warehouse");
        warehouse.setLocation("Test Location");

        when(warehouseService.getWarehouseById(1L)).thenReturn(warehouse);

        // Act & Assert
        mockMvc.perform(get("/api/warehouses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Warehouse"))
                .andExpect(jsonPath("$.location").value("Test Location"));

        verify(warehouseService, times(1)).getWarehouseById(1L);
    }

    @Test
    void testCreateWarehouse() throws Exception {
        // Arrange
        WarehouseRequest request = new WarehouseRequest();
        request.setName("New Warehouse");
        request.setLocation("New Location");
        request.setMaxCapacity(new BigDecimal("1000.00"));

        WarehouseResponse response = new WarehouseResponse();
        response.setId(1L);
        response.setName("New Warehouse");
        response.setLocation("New Location");
        response.setMaxCapacity(new BigDecimal("1000.00"));

        when(warehouseService.createWarehouse(any(WarehouseRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Warehouse"))
                .andExpect(jsonPath("$.location").value("New Location"));

        verify(warehouseService, times(1)).createWarehouse(any(WarehouseRequest.class));
    }

    @Test
    void testUpdateWarehouse() throws Exception {
        // Arrange
        WarehouseRequest request = new WarehouseRequest();
        request.setName("Updated Warehouse");
        request.setLocation("Updated Location");
        request.setMaxCapacity(new BigDecimal("2000.00"));

        WarehouseResponse response = new WarehouseResponse();
        response.setId(1L);
        response.setName("Updated Warehouse");
        response.setLocation("Updated Location");
        response.setMaxCapacity(new BigDecimal("2000.00"));

        when(warehouseService.updateWarehouse(eq(1L), any(WarehouseRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/warehouses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Warehouse"));

        verify(warehouseService, times(1)).updateWarehouse(eq(1L), any(WarehouseRequest.class));
    }

    @Test
    void testDeleteWarehouse() throws Exception {
        // Arrange
        doNothing().when(warehouseService).deleteWarehouse(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/warehouses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Warehouse deleted successfully"));

        verify(warehouseService, times(1)).deleteWarehouse(1L);
    }

    @Test
    void testHardDeleteWarehouse() throws Exception {
        // Arrange
        doNothing().when(warehouseService).hardDeleteWarehouse(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/warehouses/1/permanent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Warehouse permanently deleted"));

        verify(warehouseService, times(1)).hardDeleteWarehouse(1L);
    }
}
