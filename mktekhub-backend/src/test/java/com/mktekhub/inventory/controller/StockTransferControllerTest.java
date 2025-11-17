package com.mktekhub.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mktekhub.inventory.config.TestSecurityConfig;
import com.mktekhub.inventory.dto.BulkStockTransferRequest;
import com.mktekhub.inventory.dto.BulkStockTransferResponse;
import com.mktekhub.inventory.dto.StockTransferRequest;
import com.mktekhub.inventory.dto.StockTransferResponse;
import com.mktekhub.inventory.exception.InsufficientStockException;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.exception.WarehouseCapacityExceededException;
import com.mktekhub.inventory.service.StockTransferService;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockTransferController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
@DisplayName("StockTransferController Tests")
class StockTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StockTransferService stockTransferService;

    private StockTransferRequest transferRequest;
    private StockTransferResponse transferResponse;

    @BeforeEach
    void setUp() {
        transferRequest = new StockTransferRequest();
        transferRequest.setItemSku("SKU-001");
        transferRequest.setSourceWarehouseId(1L);
        transferRequest.setDestinationWarehouseId(2L);
        transferRequest.setQuantity(50);
        transferRequest.setNotes("Transfer test");

        transferResponse = new StockTransferResponse();
        transferResponse.setActivityId(1L);
        transferResponse.setItemSku("SKU-001");
        transferResponse.setItemName("Test Item");
        transferResponse.setQuantityTransferred(50);
        transferResponse.setPreviousQuantity(100);
        transferResponse.setNewQuantity(150);
        transferResponse.setSourceWarehouseName("Warehouse A");
        transferResponse.setDestinationWarehouseName("Warehouse B");
        transferResponse.setTimestamp(LocalDateTime.now());
        transferResponse.setPerformedBy("testuser");
        transferResponse.setNotes("Transfer test");
    }

    @Test
    @DisplayName("POST /api/stock-transfer - Success")
    @WithMockUser
    void transferStock() throws Exception {
        when(stockTransferService.transferStock(any(StockTransferRequest.class))).thenReturn(transferResponse);

        mockMvc.perform(post("/api/stock-transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.itemSku").value("SKU-001"))
            .andExpect(jsonPath("$.quantityTransferred").value(50));

        verify(stockTransferService).transferStock(any(StockTransferRequest.class));
    }

    @Test
    @DisplayName("POST /api/stock-transfer/bulk - Success")
    @WithMockUser
    void bulkTransferStock_Success() throws Exception {
        BulkStockTransferRequest bulkRequest = new BulkStockTransferRequest();
        bulkRequest.setTransfers(Collections.singletonList(transferRequest));

        BulkStockTransferResponse bulkResponse = new BulkStockTransferResponse(
            1, 1, 0, Collections.singletonList(transferResponse), Collections.emptyList()
        );

        when(stockTransferService.bulkTransferStock(any(BulkStockTransferRequest.class))).thenReturn(bulkResponse);

        mockMvc.perform(post("/api/stock-transfer/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTransfers").value(1))
            .andExpect(jsonPath("$.successfulTransfers").value(1))
            .andExpect(jsonPath("$.failedTransfers").value(0));

        verify(stockTransferService).bulkTransferStock(any(BulkStockTransferRequest.class));
    }

    @Test
    @DisplayName("POST /api/stock-transfer - Item not found")
    @WithMockUser
    void transferStock_ItemNotFound() throws Exception {
        when(stockTransferService.transferStock(any(StockTransferRequest.class)))
            .thenThrow(new ResourceNotFoundException("InventoryItem", "sku", "SKU-001"));

        mockMvc.perform(post("/api/stock-transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
            .andExpect(status().isNotFound());

        verify(stockTransferService).transferStock(any(StockTransferRequest.class));
    }

    @Test
    @DisplayName("POST /api/stock-transfer - Source warehouse not found")
    @WithMockUser
    void transferStock_SourceWarehouseNotFound() throws Exception {
        when(stockTransferService.transferStock(any(StockTransferRequest.class)))
            .thenThrow(new ResourceNotFoundException("Warehouse", "id", 999L));

        mockMvc.perform(post("/api/stock-transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
            .andExpect(status().isNotFound());

        verify(stockTransferService).transferStock(any(StockTransferRequest.class));
    }

    @Test
    @DisplayName("POST /api/stock-transfer - Insufficient stock")
    @WithMockUser
    void transferStock_InsufficientStock() throws Exception {
        when(stockTransferService.transferStock(any(StockTransferRequest.class)))
            .thenThrow(new InsufficientStockException("Test Item", 10, 50));

        mockMvc.perform(post("/api/stock-transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
            .andExpect(status().isBadRequest());

        verify(stockTransferService).transferStock(any(StockTransferRequest.class));
    }

    @Test
    @DisplayName("POST /api/stock-transfer - Warehouse capacity exceeded")
    @WithMockUser
    void transferStock_CapacityExceeded() throws Exception {
        when(stockTransferService.transferStock(any(StockTransferRequest.class)))
            .thenThrow(new WarehouseCapacityExceededException(
                "Warehouse B", BigDecimal.valueOf(100), BigDecimal.valueOf(200)));

        mockMvc.perform(post("/api/stock-transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
            .andExpect(status().isBadRequest());

        verify(stockTransferService).transferStock(any(StockTransferRequest.class));
    }

    @Test
    @DisplayName("POST /api/stock-transfer/bulk - Partial success")
    @WithMockUser
    void bulkTransferStock_PartialSuccess() throws Exception {
        StockTransferRequest request2 = new StockTransferRequest();
        request2.setItemSku("SKU-002");
        request2.setSourceWarehouseId(1L);
        request2.setDestinationWarehouseId(2L);
        request2.setQuantity(30);

        BulkStockTransferRequest bulkRequest = new BulkStockTransferRequest();
        List<StockTransferRequest> transfers = new ArrayList<>();
        transfers.add(transferRequest);
        transfers.add(request2);
        bulkRequest.setTransfers(transfers);

        List<BulkStockTransferResponse.TransferError> errors = new ArrayList<>();
        errors.add(new BulkStockTransferResponse.TransferError(1, "SKU-002", "Insufficient stock"));

        BulkStockTransferResponse partialResponse = new BulkStockTransferResponse(
            2, 1, 1, Collections.singletonList(transferResponse), errors
        );

        when(stockTransferService.bulkTransferStock(any(BulkStockTransferRequest.class)))
            .thenReturn(partialResponse);

        mockMvc.perform(post("/api/stock-transfer/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTransfers").value(2))
            .andExpect(jsonPath("$.successfulTransfers").value(1))
            .andExpect(jsonPath("$.failedTransfers").value(1))
            .andExpect(jsonPath("$.errors[0].itemSku").value("SKU-002"))
            .andExpect(jsonPath("$.errors[0].errorMessage").value("Insufficient stock"));

        verify(stockTransferService).bulkTransferStock(any(BulkStockTransferRequest.class));
    }

    @Test
    @DisplayName("POST /api/stock-transfer/bulk - All failures")
    @WithMockUser
    void bulkTransferStock_AllFailures() throws Exception {
        BulkStockTransferRequest bulkRequest = new BulkStockTransferRequest();
        bulkRequest.setTransfers(Collections.singletonList(transferRequest));

        List<BulkStockTransferResponse.TransferError> errors = new ArrayList<>();
        errors.add(new BulkStockTransferResponse.TransferError(0, "SKU-001", "Item not found"));

        BulkStockTransferResponse failureResponse = new BulkStockTransferResponse(
            1, 0, 1, Collections.emptyList(), errors
        );

        when(stockTransferService.bulkTransferStock(any(BulkStockTransferRequest.class)))
            .thenReturn(failureResponse);

        mockMvc.perform(post("/api/stock-transfer/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTransfers").value(1))
            .andExpect(jsonPath("$.successfulTransfers").value(0))
            .andExpect(jsonPath("$.failedTransfers").value(1))
            .andExpect(jsonPath("$.successResults").isEmpty())
            .andExpect(jsonPath("$.errors[0].itemSku").value("SKU-001"));

        verify(stockTransferService).bulkTransferStock(any(BulkStockTransferRequest.class));
    }

    @Test
    @DisplayName("POST /api/stock-transfer/bulk - Empty transfer list")
    @WithMockUser
    void bulkTransferStock_EmptyList() throws Exception {
        BulkStockTransferRequest emptyRequest = new BulkStockTransferRequest();
        emptyRequest.setTransfers(Collections.emptyList());

        BulkStockTransferResponse emptyResponse = new BulkStockTransferResponse(
            0, 0, 0, Collections.emptyList(), Collections.emptyList()
        );

        when(stockTransferService.bulkTransferStock(any(BulkStockTransferRequest.class)))
            .thenReturn(emptyResponse);

        mockMvc.perform(post("/api/stock-transfer/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
            .andExpect(status().isBadRequest());

        verify(stockTransferService, never()).bulkTransferStock(any(BulkStockTransferRequest.class));
    }
}
