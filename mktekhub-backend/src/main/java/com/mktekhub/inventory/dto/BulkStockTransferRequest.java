package com.mktekhub.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO for bulk stock transfer request.
 */
public class BulkStockTransferRequest {

    @NotEmpty(message = "Transfer list cannot be empty")
    @Valid
    private List<StockTransferRequest> transfers;

    // Constructors
    public BulkStockTransferRequest() {
    }

    public BulkStockTransferRequest(List<StockTransferRequest> transfers) {
        this.transfers = transfers;
    }

    // Getters and Setters
    public List<StockTransferRequest> getTransfers() {
        return transfers;
    }

    public void setTransfers(List<StockTransferRequest> transfers) {
        this.transfers = transfers;
    }
}
