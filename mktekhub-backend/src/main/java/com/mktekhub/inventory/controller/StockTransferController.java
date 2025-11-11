package com.mktekhub.inventory.controller;

import com.mktekhub.inventory.dto.StockTransferRequest;
import com.mktekhub.inventory.dto.StockTransferResponse;
import com.mktekhub.inventory.service.StockTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for stock transfer operations between warehouses.
 */
@RestController
@RequestMapping("/api/stock-transfer")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Stock Transfer", description = "APIs for transferring inventory between warehouses")
@SecurityRequirement(name = "Bearer Authentication")
public class StockTransferController {

    @Autowired
    private StockTransferService stockTransferService;

    /**
     * Transfer inventory between warehouses
     * Requires ADMIN or MANAGER role
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Transfer stock between warehouses",
               description = "Transfer inventory items from one warehouse to another with automatic capacity checking and activity logging")
    public ResponseEntity<StockTransferResponse> transferStock(
            @Valid @RequestBody StockTransferRequest request) {

        StockTransferResponse response = stockTransferService.transferStock(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
