package com.mktekhub.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mktekhub.inventory.dto.InventoryItemRequest;
import com.mktekhub.inventory.dto.InventoryItemResponse;
import com.mktekhub.inventory.service.InventoryItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InventoryItemControllerTest {

    @Mock
    private InventoryItemService inventoryItemService;

    @InjectMocks
    private InventoryItemController inventoryItemController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryItemController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetAllItems() throws Exception {
        // Arrange
        InventoryItemResponse item1 = new InventoryItemResponse();
        item1.setId(1L);
        item1.setSku("SKU001");
        item1.setName("Item 1");

        InventoryItemResponse item2 = new InventoryItemResponse();
        item2.setId(2L);
        item2.setSku("SKU002");
        item2.setName("Item 2");

        List<InventoryItemResponse> items = Arrays.asList(item1, item2);
        when(inventoryItemService.getAllItems()).thenReturn(items);

        // Act & Assert
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].sku").value("SKU001"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].sku").value("SKU002"));

        verify(inventoryItemService, times(1)).getAllItems();
    }

    @Test
    void testGetItemById() throws Exception {
        // Arrange
        InventoryItemResponse item = new InventoryItemResponse();
        item.setId(1L);
        item.setSku("SKU001");
        item.setName("Test Item");

        when(inventoryItemService.getItemById(1L)).thenReturn(item);

        // Act & Assert
        mockMvc.perform(get("/api/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SKU001"))
                .andExpect(jsonPath("$.name").value("Test Item"));

        verify(inventoryItemService, times(1)).getItemById(1L);
    }

    @Test
    void testGetItemBySku() throws Exception {
        // Arrange
        InventoryItemResponse item = new InventoryItemResponse();
        item.setId(1L);
        item.setSku("SKU001");
        item.setName("Test Item");

        when(inventoryItemService.getItemBySku("SKU001")).thenReturn(item);

        // Act & Assert
        mockMvc.perform(get("/api/inventory/sku/SKU001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SKU001"));

        verify(inventoryItemService, times(1)).getItemBySku("SKU001");
    }

    @Test
    void testGetItemsByWarehouse() throws Exception {
        // Arrange
        InventoryItemResponse item1 = new InventoryItemResponse();
        item1.setId(1L);
        item1.setSku("SKU001");

        List<InventoryItemResponse> items = Arrays.asList(item1);
        when(inventoryItemService.getItemsByWarehouse(1L)).thenReturn(items);

        // Act & Assert
        mockMvc.perform(get("/api/inventory/warehouse/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(inventoryItemService, times(1)).getItemsByWarehouse(1L);
    }

    @Test
    void testGetItemsByCategory() throws Exception {
        // Arrange
        InventoryItemResponse item1 = new InventoryItemResponse();
        item1.setId(1L);
        item1.setCategory("Electronics");

        List<InventoryItemResponse> items = Arrays.asList(item1);
        when(inventoryItemService.getItemsByCategory("Electronics")).thenReturn(items);

        // Act & Assert
        mockMvc.perform(get("/api/inventory/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(inventoryItemService, times(1)).getItemsByCategory("Electronics");
    }

    @Test
    void testGetLowStockItems() throws Exception {
        // Arrange
        InventoryItemResponse item1 = new InventoryItemResponse();
        item1.setId(1L);
        item1.setSku("SKU001");
        item1.setQuantity(5);

        List<InventoryItemResponse> items = Arrays.asList(item1);
        when(inventoryItemService.getLowStockItems()).thenReturn(items);

        // Act & Assert
        mockMvc.perform(get("/api/inventory/alerts/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(inventoryItemService, times(1)).getLowStockItems();
    }

    @Test
    void testGetExpiredItems() throws Exception {
        // Arrange
        List<InventoryItemResponse> items = Arrays.asList(new InventoryItemResponse());
        when(inventoryItemService.getExpiredItems()).thenReturn(items);

        // Act & Assert
        mockMvc.perform(get("/api/inventory/alerts/expired"))
                .andExpect(status().isOk());

        verify(inventoryItemService, times(1)).getExpiredItems();
    }

    @Test
    void testGetItemsExpiringSoon() throws Exception {
        // Arrange
        List<InventoryItemResponse> items = Arrays.asList(new InventoryItemResponse());
        when(inventoryItemService.getItemsExpiringSoon(30)).thenReturn(items);

        // Act & Assert
        mockMvc.perform(get("/api/inventory/alerts/expiring?days=30"))
                .andExpect(status().isOk());

        verify(inventoryItemService, times(1)).getItemsExpiringSoon(30);
    }

    @Test
    void testGetItemsExpiringSoon_DefaultDays() throws Exception {
        // Arrange
        List<InventoryItemResponse> items = Arrays.asList(new InventoryItemResponse());
        when(inventoryItemService.getItemsExpiringSoon(30)).thenReturn(items);

        // Act & Assert
        mockMvc.perform(get("/api/inventory/alerts/expiring"))
                .andExpect(status().isOk());

        verify(inventoryItemService, times(1)).getItemsExpiringSoon(30);
    }

    @Test
    void testCreateItem() throws Exception {
        // Arrange
        InventoryItemRequest request = new InventoryItemRequest();
        request.setSku("SKU001");
        request.setName("New Item");
        request.setQuantity(100);
        request.setWarehouseId(1L);

        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(1L);
        response.setSku("SKU001");
        response.setName("New Item");

        when(inventoryItemService.createItem(any(InventoryItemRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SKU001"));

        verify(inventoryItemService, times(1)).createItem(any(InventoryItemRequest.class));
    }

    @Test
    void testUpdateItem() throws Exception {
        // Arrange
        InventoryItemRequest request = new InventoryItemRequest();
        request.setSku("SKU001");
        request.setName("Updated Item");
        request.setQuantity(150);
        request.setWarehouseId(1L);

        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(1L);
        response.setSku("SKU001");
        response.setName("Updated Item");

        when(inventoryItemService.updateItem(eq(1L), any(InventoryItemRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/inventory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Item"));

        verify(inventoryItemService, times(1)).updateItem(eq(1L), any(InventoryItemRequest.class));
    }

    @Test
    void testDeleteItem() throws Exception {
        // Arrange
        doNothing().when(inventoryItemService).deleteItem(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inventory item deleted successfully"));

        verify(inventoryItemService, times(1)).deleteItem(1L);
    }

    @Test
    void testAdjustQuantity() throws Exception {
        // Arrange
        Map<String, Integer> request = new HashMap<>();
        request.put("quantityChange", 10);

        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(1L);
        response.setQuantity(110);

        when(inventoryItemService.adjustQuantity(1L, 10)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/inventory/1/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.quantity").value(110));

        verify(inventoryItemService, times(1)).adjustQuantity(1L, 10);
    }
}
