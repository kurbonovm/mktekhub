package com.mktekhub.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mktekhub.inventory.dto.BulkStockTransferRequest;
import com.mktekhub.inventory.dto.BulkStockTransferResponse;
import com.mktekhub.inventory.dto.StockTransferRequest;
import com.mktekhub.inventory.dto.StockTransferResponse;
import com.mktekhub.inventory.service.StockTransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StockTransferControllerTest {

    @Mock
    private StockTransferService stockTransferService;

    @InjectMocks
    private StockTransferController stockTransferController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(stockTransferController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testTransferStock() throws Exception {
        // Arrange
        StockTransferRequest request = new StockTransferRequest();
        request.setItemSku("SKU001");
        request.setSourceWarehouseId(1L);
        request.setDestinationWarehouseId(2L);
        request.setQuantity(10);

        StockTransferResponse response = new StockTransferResponse();
        response.setActivityId(1L);
        response.setItemSku("SKU001");
        response.setQuantityTransferred(10);

        when(stockTransferService.transferStock(any(StockTransferRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/stock-transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activityId").value(1))
                .andExpect(jsonPath("$.itemSku").value("SKU001"))
                .andExpect(jsonPath("$.quantityTransferred").value(10));

        verify(stockTransferService, times(1)).transferStock(any(StockTransferRequest.class));
    }

    @Test
    void testTransferStock_WithNotes() throws Exception {
        // Arrange
        StockTransferRequest request = new StockTransferRequest();
        request.setItemSku("SKU002");
        request.setSourceWarehouseId(1L);
        request.setDestinationWarehouseId(2L);
        request.setQuantity(5);
        request.setNotes("Transfer for restock");

        StockTransferResponse response = new StockTransferResponse();
        response.setActivityId(2L);
        response.setItemSku("SKU002");
        response.setQuantityTransferred(5);
        response.setNotes("Transfer for restock");

        when(stockTransferService.transferStock(any(StockTransferRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/stock-transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activityId").value(2))
                .andExpect(jsonPath("$.notes").value("Transfer for restock"));

        verify(stockTransferService, times(1)).transferStock(any(StockTransferRequest.class));
    }

    @Test
    void testBulkTransferStock() throws Exception {
        // Arrange
        BulkStockTransferRequest request = new BulkStockTransferRequest();
        StockTransferRequest transfer1 = new StockTransferRequest();
        transfer1.setItemSku("SKU001");
        transfer1.setSourceWarehouseId(1L);
        transfer1.setDestinationWarehouseId(2L);
        transfer1.setQuantity(10);
        request.setTransfers(Arrays.asList(transfer1));

        BulkStockTransferResponse response = new BulkStockTransferResponse();
        response.setTotalTransfers(1);
        response.setSuccessfulTransfers(1);
        response.setFailedTransfers(0);
        response.setSuccessResults(Arrays.asList(new StockTransferResponse()));
        response.setErrors(new ArrayList<>());

        when(stockTransferService.bulkTransferStock(any(BulkStockTransferRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/stock-transfer/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTransfers").value(1))
                .andExpect(jsonPath("$.successfulTransfers").value(1))
                .andExpect(jsonPath("$.failedTransfers").value(0));

        verify(stockTransferService, times(1)).bulkTransferStock(any(BulkStockTransferRequest.class));
    }

    @Test
    void testBulkTransferStock_PartialSuccess() throws Exception {
        // Arrange
        BulkStockTransferRequest request = new BulkStockTransferRequest();
        StockTransferRequest transfer1 = new StockTransferRequest();
        transfer1.setItemSku("SKU001");
        transfer1.setSourceWarehouseId(1L);
        transfer1.setDestinationWarehouseId(2L);
        transfer1.setQuantity(10);

        StockTransferRequest transfer2 = new StockTransferRequest();
        transfer2.setItemSku("SKU002");
        transfer2.setSourceWarehouseId(1L);
        transfer2.setDestinationWarehouseId(2L);
        transfer2.setQuantity(1000);

        request.setTransfers(Arrays.asList(transfer1, transfer2));

        BulkStockTransferResponse response = new BulkStockTransferResponse();
        response.setTotalTransfers(2);
        response.setSuccessfulTransfers(1);
        response.setFailedTransfers(1);

        StockTransferResponse successResponse = new StockTransferResponse();
        successResponse.setItemSku("SKU001");
        response.setSuccessResults(Arrays.asList(successResponse));

        BulkStockTransferResponse.TransferError error = new BulkStockTransferResponse.TransferError();
        error.setItemSku("SKU002");
        error.setErrorMessage("Insufficient stock");
        response.setErrors(Arrays.asList(error));

        when(stockTransferService.bulkTransferStock(any(BulkStockTransferRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/stock-transfer/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTransfers").value(2))
                .andExpect(jsonPath("$.successfulTransfers").value(1))
                .andExpect(jsonPath("$.failedTransfers").value(1));

        verify(stockTransferService, times(1)).bulkTransferStock(any(BulkStockTransferRequest.class));
    }
}
