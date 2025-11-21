/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mktekhub.inventory.config.TestSecurityConfig;
import com.mktekhub.inventory.dto.InventoryItemRequest;
import com.mktekhub.inventory.dto.InventoryItemResponse;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.service.InventoryItemService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

@WebMvcTest(InventoryItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@DisplayName("InventoryItemController Tests")
class InventoryItemControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private InventoryItemService inventoryItemService;

  private InventoryItemResponse itemResponse;
  private InventoryItemRequest itemRequest;

  @BeforeEach
  void setUp() {
    itemResponse = new InventoryItemResponse();
    itemResponse.setId(1L);
    itemResponse.setSku("SKU-001");
    itemResponse.setName("Test Item");
    itemResponse.setDescription("Test Description");
    itemResponse.setCategory("Electronics");
    itemResponse.setBrand("TestBrand");
    itemResponse.setQuantity(100);
    itemResponse.setReorderLevel(10);
    itemResponse.setUnitPrice(BigDecimal.valueOf(50.00));
    itemResponse.setWarehouseId(1L);
    itemResponse.setWarehouseName("Main Warehouse");
    itemResponse.setExpirationDate(LocalDate.now().plusMonths(6));

    itemRequest = new InventoryItemRequest();
    itemRequest.setSku("SKU-001");
    itemRequest.setName("Test Item");
    itemRequest.setDescription("Test Description");
    itemRequest.setCategory("Electronics");
    itemRequest.setBrand("TestBrand");
    itemRequest.setQuantity(100);
    itemRequest.setReorderLevel(10);
    itemRequest.setUnitPrice(BigDecimal.valueOf(50.00));
    itemRequest.setWarehouseId(1L);
    itemRequest.setExpirationDate(LocalDate.now().plusMonths(6));
  }

  @Test
  @DisplayName("GET /api/inventory - Success")
  @WithMockUser
  void getAllItems() throws Exception {
    when(inventoryItemService.getAllItems()).thenReturn(Collections.singletonList(itemResponse));

    mockMvc
        .perform(get("/api/inventory"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].sku").value("SKU-001"));

    verify(inventoryItemService).getAllItems();
  }

  @Test
  @DisplayName("GET /api/inventory/{id} - Success")
  @WithMockUser
  void getItemById() throws Exception {
    when(inventoryItemService.getItemById(1L)).thenReturn(itemResponse);

    mockMvc
        .perform(get("/api/inventory/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1));

    verify(inventoryItemService).getItemById(1L);
  }

  @Test
  @DisplayName("GET /api/inventory/sku/{sku} - Success")
  @WithMockUser
  void getItemBySku() throws Exception {
    when(inventoryItemService.getItemBySku("SKU-001")).thenReturn(itemResponse);

    mockMvc
        .perform(get("/api/inventory/sku/SKU-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sku").value("SKU-001"));

    verify(inventoryItemService).getItemBySku("SKU-001");
  }

  @Test
  @DisplayName("GET /api/inventory/warehouse/{warehouseId} - Success")
  @WithMockUser
  void getItemsByWarehouse() throws Exception {
    when(inventoryItemService.getItemsByWarehouse(1L))
        .thenReturn(Collections.singletonList(itemResponse));

    mockMvc.perform(get("/api/inventory/warehouse/1")).andExpect(status().isOk());

    verify(inventoryItemService).getItemsByWarehouse(1L);
  }

  @Test
  @DisplayName("GET /api/inventory/category/{category} - Success")
  @WithMockUser
  void getItemsByCategory() throws Exception {
    when(inventoryItemService.getItemsByCategory("Electronics"))
        .thenReturn(Collections.singletonList(itemResponse));

    mockMvc.perform(get("/api/inventory/category/Electronics")).andExpect(status().isOk());

    verify(inventoryItemService).getItemsByCategory("Electronics");
  }

  @Test
  @DisplayName("GET /api/inventory/alerts/low-stock - Success")
  @WithMockUser
  void getLowStockItems() throws Exception {
    when(inventoryItemService.getLowStockItems())
        .thenReturn(Collections.singletonList(itemResponse));

    mockMvc.perform(get("/api/inventory/alerts/low-stock")).andExpect(status().isOk());

    verify(inventoryItemService).getLowStockItems();
  }

  @Test
  @DisplayName("GET /api/inventory/alerts/expired - Success")
  @WithMockUser
  void getExpiredItems() throws Exception {
    when(inventoryItemService.getExpiredItems()).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/api/inventory/alerts/expired")).andExpect(status().isOk());

    verify(inventoryItemService).getExpiredItems();
  }

  @Test
  @DisplayName("GET /api/inventory/alerts/expiring - Success with default days")
  @WithMockUser
  void getItemsExpiringSoon_DefaultDays() throws Exception {
    when(inventoryItemService.getItemsExpiringSoon(30)).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/api/inventory/alerts/expiring")).andExpect(status().isOk());

    verify(inventoryItemService).getItemsExpiringSoon(30);
  }

  @Test
  @DisplayName("GET /api/inventory/alerts/expiring - Success with custom days")
  @WithMockUser
  void getItemsExpiringSoon_CustomDays() throws Exception {
    when(inventoryItemService.getItemsExpiringSoon(60)).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/api/inventory/alerts/expiring?days=60")).andExpect(status().isOk());

    verify(inventoryItemService).getItemsExpiringSoon(60);
  }

  @Test
  @DisplayName("POST /api/inventory - Success")
  @WithMockUser
  void createItem() throws Exception {
    when(inventoryItemService.createItem(any(InventoryItemRequest.class))).thenReturn(itemResponse);

    mockMvc
        .perform(
            post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.sku").value("SKU-001"));

    verify(inventoryItemService).createItem(any(InventoryItemRequest.class));
  }

  @Test
  @DisplayName("PUT /api/inventory/{id} - Success")
  @WithMockUser
  void updateItem() throws Exception {
    when(inventoryItemService.updateItem(eq(1L), any(InventoryItemRequest.class)))
        .thenReturn(itemResponse);

    mockMvc
        .perform(
            put("/api/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemRequest)))
        .andExpect(status().isOk());

    verify(inventoryItemService).updateItem(eq(1L), any(InventoryItemRequest.class));
  }

  @Test
  @DisplayName("DELETE /api/inventory/{id} - Success")
  @WithMockUser
  void deleteItem() throws Exception {
    doNothing().when(inventoryItemService).deleteItem(1L);

    mockMvc
        .perform(delete("/api/inventory/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Inventory item deleted successfully"));

    verify(inventoryItemService).deleteItem(1L);
  }

  @Test
  @DisplayName("PATCH /api/inventory/{id}/adjust - Success")
  @WithMockUser
  void adjustQuantity() throws Exception {
    when(inventoryItemService.adjustQuantity(1L, 50)).thenReturn(itemResponse);

    Map<String, Integer> request = new HashMap<>();
    request.put("quantityChange", 50);

    mockMvc
        .perform(
            patch("/api/inventory/1/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    verify(inventoryItemService).adjustQuantity(1L, 50);
  }

  @Test
  @DisplayName("PATCH /api/inventory/{id}/adjust - Missing quantityChange")
  @WithMockUser
  void adjustQuantity_MissingQuantityChange() throws Exception {
    Map<String, Integer> request = new HashMap<>();

    mockMvc
        .perform(
            patch("/api/inventory/1/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError());

    verify(inventoryItemService, never()).adjustQuantity(anyLong(), anyInt());
  }

  @Test
  @DisplayName("GET /api/inventory - Empty list")
  @WithMockUser
  void getAllItems_EmptyList() throws Exception {
    when(inventoryItemService.getAllItems()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/inventory"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());

    verify(inventoryItemService).getAllItems();
  }

  @Test
  @DisplayName("GET /api/inventory/{id} - Not found")
  @WithMockUser
  void getItemById_NotFound() throws Exception {
    when(inventoryItemService.getItemById(999L))
        .thenThrow(new ResourceNotFoundException("InventoryItem", "id", 999L));

    mockMvc.perform(get("/api/inventory/999")).andExpect(status().isNotFound());

    verify(inventoryItemService).getItemById(999L);
  }

  @Test
  @DisplayName("GET /api/inventory/sku/{sku} - Not found")
  @WithMockUser
  void getItemBySku_NotFound() throws Exception {
    when(inventoryItemService.getItemBySku("INVALID"))
        .thenThrow(new ResourceNotFoundException("InventoryItem", "sku", "INVALID"));

    mockMvc.perform(get("/api/inventory/sku/INVALID")).andExpect(status().isNotFound());

    verify(inventoryItemService).getItemBySku("INVALID");
  }

  @Test
  @DisplayName("GET /api/inventory/warehouse/{warehouseId} - Empty list")
  @WithMockUser
  void getItemsByWarehouse_EmptyList() throws Exception {
    when(inventoryItemService.getItemsByWarehouse(1L)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/inventory/warehouse/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());

    verify(inventoryItemService).getItemsByWarehouse(1L);
  }

  @Test
  @DisplayName("GET /api/inventory/category/{category} - Empty list")
  @WithMockUser
  void getItemsByCategory_EmptyList() throws Exception {
    when(inventoryItemService.getItemsByCategory("NonExistent"))
        .thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/inventory/category/NonExistent"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());

    verify(inventoryItemService).getItemsByCategory("NonExistent");
  }

  @Test
  @DisplayName("GET /api/inventory/alerts/low-stock - Empty list")
  @WithMockUser
  void getLowStockItems_EmptyList() throws Exception {
    when(inventoryItemService.getLowStockItems()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/inventory/alerts/low-stock"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());

    verify(inventoryItemService).getLowStockItems();
  }

  @Test
  @DisplayName("PUT /api/inventory/{id} - Not found")
  @WithMockUser
  void updateItem_NotFound() throws Exception {
    when(inventoryItemService.updateItem(eq(999L), any(InventoryItemRequest.class)))
        .thenThrow(new ResourceNotFoundException("InventoryItem", "id", 999L));

    mockMvc
        .perform(
            put("/api/inventory/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemRequest)))
        .andExpect(status().isNotFound());

    verify(inventoryItemService).updateItem(eq(999L), any(InventoryItemRequest.class));
  }

  @Test
  @DisplayName("DELETE /api/inventory/{id} - Not found")
  @WithMockUser
  void deleteItem_NotFound() throws Exception {
    doThrow(new ResourceNotFoundException("InventoryItem", "id", 999L))
        .when(inventoryItemService)
        .deleteItem(999L);

    mockMvc.perform(delete("/api/inventory/999")).andExpect(status().isNotFound());

    verify(inventoryItemService).deleteItem(999L);
  }

  @Test
  @DisplayName("PATCH /api/inventory/{id}/adjust - Negative quantity change")
  @WithMockUser
  void adjustQuantity_NegativeChange() throws Exception {
    itemResponse.setQuantity(80);
    when(inventoryItemService.adjustQuantity(1L, -20)).thenReturn(itemResponse);

    Map<String, Integer> request = new HashMap<>();
    request.put("quantityChange", -20);

    mockMvc
        .perform(
            patch("/api/inventory/1/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.quantity").value(80));

    verify(inventoryItemService).adjustQuantity(1L, -20);
  }

  @Test
  @DisplayName("PATCH /api/inventory/{id}/adjust - Not found")
  @WithMockUser
  void adjustQuantity_NotFound() throws Exception {
    when(inventoryItemService.adjustQuantity(999L, 20))
        .thenThrow(new ResourceNotFoundException("InventoryItem", "id", 999L));

    Map<String, Integer> request = new HashMap<>();
    request.put("quantityChange", 20);

    mockMvc
        .perform(
            patch("/api/inventory/999/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());

    verify(inventoryItemService).adjustQuantity(999L, 20);
  }
}
