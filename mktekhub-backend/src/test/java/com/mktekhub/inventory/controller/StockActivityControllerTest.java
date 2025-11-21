/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mktekhub.inventory.config.TestSecurityConfig;
import com.mktekhub.inventory.dto.StockActivityResponse;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.model.ActivityType;
import com.mktekhub.inventory.service.StockActivityService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

@WebMvcTest(StockActivityController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@DisplayName("StockActivityController Tests")
class StockActivityControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private StockActivityService stockActivityService;

  private StockActivityResponse activityResponse;
  private List<StockActivityResponse> activityList;

  @BeforeEach
  void setUp() {
    activityResponse = new StockActivityResponse();
    activityResponse.setId(1L);
    activityResponse.setItemSku("SKU-001");
    activityResponse.setItemName("Test Item");
    activityResponse.setActivityType(ActivityType.TRANSFER);
    activityResponse.setQuantityChange(50);
    activityResponse.setPreviousQuantity(100);
    activityResponse.setNewQuantity(150);
    activityResponse.setSourceWarehouseName("Warehouse A");
    activityResponse.setDestinationWarehouseName("Warehouse B");
    activityResponse.setTimestamp(LocalDateTime.now());
    activityResponse.setPerformedBy("testuser");
    activityResponse.setNotes("Test transfer");

    activityList = Arrays.asList(activityResponse);
  }

  @Test
  @DisplayName("GET /api/stock-activities - Success")
  @WithMockUser
  void getAllActivities_Success() throws Exception {
    when(stockActivityService.getAllActivities()).thenReturn(activityList);

    mockMvc
        .perform(get("/api/stock-activities"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].itemSku").value("SKU-001"))
        .andExpect(jsonPath("$[0].activityType").value("TRANSFER"));

    verify(stockActivityService).getAllActivities();
  }

  @Test
  @DisplayName("GET /api/stock-activities - Empty list")
  @WithMockUser
  void getAllActivities_EmptyList() throws Exception {
    when(stockActivityService.getAllActivities()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/stock-activities"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());

    verify(stockActivityService).getAllActivities();
  }

  @Test
  @DisplayName("GET /api/stock-activities/{id} - Success")
  @WithMockUser
  void getActivityById_Success() throws Exception {
    when(stockActivityService.getActivityById(1L)).thenReturn(activityResponse);

    mockMvc
        .perform(get("/api/stock-activities/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.itemSku").value("SKU-001"));

    verify(stockActivityService).getActivityById(1L);
  }

  @Test
  @DisplayName("GET /api/stock-activities/{id} - Not found")
  @WithMockUser
  void getActivityById_NotFound() throws Exception {
    when(stockActivityService.getActivityById(999L))
        .thenThrow(new ResourceNotFoundException("StockActivity", "id", 999L));

    mockMvc.perform(get("/api/stock-activities/999")).andExpect(status().isNotFound());

    verify(stockActivityService).getActivityById(999L);
  }

  @Test
  @DisplayName("GET /api/stock-activities/item/{itemId} - Success")
  @WithMockUser
  void getActivitiesByItemId_Success() throws Exception {
    when(stockActivityService.getActivitiesByItemIdSorted(1L)).thenReturn(activityList);

    mockMvc
        .perform(get("/api/stock-activities/item/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1));

    verify(stockActivityService).getActivitiesByItemIdSorted(1L);
  }

  @Test
  @DisplayName("GET /api/stock-activities/sku/{sku} - Success")
  @WithMockUser
  void getActivitiesByItemSku_Success() throws Exception {
    when(stockActivityService.getActivitiesByItemSku("SKU-001")).thenReturn(activityList);

    mockMvc
        .perform(get("/api/stock-activities/sku/SKU-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].itemSku").value("SKU-001"));

    verify(stockActivityService).getActivitiesByItemSku("SKU-001");
  }

  @Test
  @DisplayName("GET /api/stock-activities/type/{activityType} - Success")
  @WithMockUser
  void getActivitiesByType_Success() throws Exception {
    when(stockActivityService.getActivitiesByType(ActivityType.TRANSFER)).thenReturn(activityList);

    mockMvc
        .perform(get("/api/stock-activities/type/TRANSFER"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].activityType").value("TRANSFER"));

    verify(stockActivityService).getActivitiesByType(ActivityType.TRANSFER);
  }

  @Test
  @DisplayName("GET /api/stock-activities/user/{userId} - Success")
  @WithMockUser
  void getActivitiesByUserId_Success() throws Exception {
    when(stockActivityService.getActivitiesByUserId(1L)).thenReturn(activityList);

    mockMvc
        .perform(get("/api/stock-activities/user/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].performedBy").value("testuser"));

    verify(stockActivityService).getActivitiesByUserId(1L);
  }

  @Test
  @DisplayName("GET /api/stock-activities/user/username/{username} - Success")
  @WithMockUser
  void getActivitiesByUsername_Success() throws Exception {
    when(stockActivityService.getActivitiesByUsername("testuser")).thenReturn(activityList);

    mockMvc
        .perform(get("/api/stock-activities/user/username/testuser"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].performedBy").value("testuser"));

    verify(stockActivityService).getActivitiesByUsername("testuser");
  }

  @Test
  @DisplayName("GET /api/stock-activities/warehouse/{warehouseId} - Success")
  @WithMockUser
  void getActivitiesByWarehouse_Success() throws Exception {
    when(stockActivityService.getActivitiesByWarehouse(1L)).thenReturn(activityList);

    mockMvc
        .perform(get("/api/stock-activities/warehouse/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1));

    verify(stockActivityService).getActivitiesByWarehouse(1L);
  }

  @Test
  @DisplayName("GET /api/stock-activities/warehouse/source/{warehouseId} - Success")
  @WithMockUser
  void getActivitiesBySourceWarehouse_Success() throws Exception {
    when(stockActivityService.getActivitiesBySourceWarehouse(1L)).thenReturn(activityList);

    mockMvc
        .perform(get("/api/stock-activities/warehouse/source/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].sourceWarehouseName").value("Warehouse A"));

    verify(stockActivityService).getActivitiesBySourceWarehouse(1L);
  }

  @Test
  @DisplayName("GET /api/stock-activities/warehouse/destination/{warehouseId} - Success")
  @WithMockUser
  void getActivitiesByDestinationWarehouse_Success() throws Exception {
    when(stockActivityService.getActivitiesByDestinationWarehouse(2L)).thenReturn(activityList);

    mockMvc
        .perform(get("/api/stock-activities/warehouse/destination/2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].destinationWarehouseName").value("Warehouse B"));

    verify(stockActivityService).getActivitiesByDestinationWarehouse(2L);
  }

  @Test
  @DisplayName("GET /api/stock-activities/date-range - Success")
  @WithMockUser
  void getActivitiesByDateRange_Success() throws Exception {
    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);

    when(stockActivityService.getActivitiesByDateRange(start, end)).thenReturn(activityList);

    mockMvc
        .perform(
            get("/api/stock-activities/date-range")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:00"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1));

    verify(stockActivityService).getActivitiesByDateRange(start, end);
  }

  @Test
  @DisplayName("GET /api/stock-activities/filter - Success with all filters")
  @WithMockUser
  void getActivitiesWithFilters_AllFilters() throws Exception {
    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);

    when(stockActivityService.getActivitiesWithFilters(
            1L, "SKU-001", ActivityType.TRANSFER, 1L, "testuser", 1L, start, end))
        .thenReturn(activityList);

    mockMvc
        .perform(
            get("/api/stock-activities/filter")
                .param("itemId", "1")
                .param("sku", "SKU-001")
                .param("activityType", "TRANSFER")
                .param("userId", "1")
                .param("username", "testuser")
                .param("warehouseId", "1")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:00"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1));

    verify(stockActivityService)
        .getActivitiesWithFilters(
            1L, "SKU-001", ActivityType.TRANSFER, 1L, "testuser", 1L, start, end);
  }

  @Test
  @DisplayName("GET /api/stock-activities/filter - Success with no filters")
  @WithMockUser
  void getActivitiesWithFilters_NoFilters() throws Exception {
    when(stockActivityService.getActivitiesWithFilters(
            null, null, null, null, null, null, null, null))
        .thenReturn(activityList);

    mockMvc
        .perform(get("/api/stock-activities/filter"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1));

    verify(stockActivityService)
        .getActivitiesWithFilters(null, null, null, null, null, null, null, null);
  }
}
