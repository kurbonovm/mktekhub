package com.mktekhub.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mktekhub.inventory.config.TestSecurityConfig;
import com.mktekhub.inventory.dto.WarehouseRequest;
import com.mktekhub.inventory.dto.WarehouseResponse;
import com.mktekhub.inventory.service.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WarehouseController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@DisplayName("WarehouseController Tests")
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WarehouseService warehouseService;

    private WarehouseResponse warehouseResponse;
    private WarehouseRequest warehouseRequest;

    @BeforeEach
    void setUp() {
        warehouseResponse = new WarehouseResponse();
        warehouseResponse.setId(1L);
        warehouseResponse.setName("Main Warehouse");
        warehouseResponse.setLocation("New York");
        warehouseResponse.setMaxCapacity(BigDecimal.valueOf(10000));
        warehouseResponse.setCurrentCapacity(BigDecimal.valueOf(5000));
        warehouseResponse.setIsActive(true);

        warehouseRequest = new WarehouseRequest();
        warehouseRequest.setName("Main Warehouse");
        warehouseRequest.setLocation("New York");
        warehouseRequest.setMaxCapacity(BigDecimal.valueOf(10000));
    }

    @Test
    @DisplayName("GET /api/warehouses - Success")
    @WithMockUser
    void getAllWarehouses() throws Exception {
        when(warehouseService.getAllWarehouses()).thenReturn(Collections.singletonList(warehouseResponse));

        mockMvc.perform(get("/api/warehouses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Main Warehouse"));

        verify(warehouseService).getAllWarehouses();
    }

    @Test
    @DisplayName("GET /api/warehouses/active - Success")
    @WithMockUser
    void getActiveWarehouses() throws Exception {
        when(warehouseService.getActiveWarehouses()).thenReturn(Collections.singletonList(warehouseResponse));

        mockMvc.perform(get("/api/warehouses/active"))
            .andExpect(status().isOk());

        verify(warehouseService).getActiveWarehouses();
    }

    @Test
    @DisplayName("GET /api/warehouses/alerts - Success")
    @WithMockUser
    void getWarehousesWithAlerts() throws Exception {
        when(warehouseService.getWarehousesWithAlerts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/warehouses/alerts"))
            .andExpect(status().isOk());

        verify(warehouseService).getWarehousesWithAlerts();
    }

    @Test
    @DisplayName("GET /api/warehouses/{id} - Success")
    @WithMockUser
    void getWarehouseById() throws Exception {
        when(warehouseService.getWarehouseById(1L)).thenReturn(warehouseResponse);

        mockMvc.perform(get("/api/warehouses/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Main Warehouse"));

        verify(warehouseService).getWarehouseById(1L);
    }

    @Test
    @DisplayName("POST /api/warehouses - Success")
    @WithMockUser
    void createWarehouse() throws Exception {
        when(warehouseService.createWarehouse(any(WarehouseRequest.class))).thenReturn(warehouseResponse);

        mockMvc.perform(post("/api/warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(warehouseRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Main Warehouse"));

        verify(warehouseService).createWarehouse(any(WarehouseRequest.class));
    }

    @Test
    @DisplayName("PUT /api/warehouses/{id} - Success")
    @WithMockUser
    void updateWarehouse() throws Exception {
        when(warehouseService.updateWarehouse(eq(1L), any(WarehouseRequest.class))).thenReturn(warehouseResponse);

        mockMvc.perform(put("/api/warehouses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(warehouseRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Main Warehouse"));

        verify(warehouseService).updateWarehouse(eq(1L), any(WarehouseRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/warehouses/{id} - Success")
    @WithMockUser
    void deleteWarehouse() throws Exception {
        doNothing().when(warehouseService).deleteWarehouse(1L);

        mockMvc.perform(delete("/api/warehouses/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Warehouse deleted successfully"));

        verify(warehouseService).deleteWarehouse(1L);
    }

    @Test
    @DisplayName("DELETE /api/warehouses/{id}/permanent - Success")
    @WithMockUser
    void hardDeleteWarehouse() throws Exception {
        doNothing().when(warehouseService).hardDeleteWarehouse(1L);

        mockMvc.perform(delete("/api/warehouses/1/permanent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Warehouse permanently deleted"));

        verify(warehouseService).hardDeleteWarehouse(1L);
    }
}
