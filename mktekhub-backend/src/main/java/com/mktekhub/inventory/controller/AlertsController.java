package com.mktekhub.inventory.controller;

import com.mktekhub.inventory.dto.CombinedAlertsResponse;
import com.mktekhub.inventory.dto.InventoryItemResponse;
import com.mktekhub.inventory.dto.WarehouseResponse;
import com.mktekhub.inventory.service.InventoryItemService;
import com.mktekhub.inventory.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for combined alerts and notifications.
 */
@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Alerts", description = "APIs for combined alerts and notifications")
@SecurityRequirement(name = "Bearer Authentication")
public class AlertsController {

    @Autowired
    private InventoryItemService inventoryItemService;

    @Autowired
    private WarehouseService warehouseService;

    /**
     * Get all alerts in one response
     * Accessible by ADMIN and MANAGER
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all alerts",
               description = "Get all types of alerts including low stock, expired items, expiring soon items, and capacity alerts")
    public ResponseEntity<CombinedAlertsResponse> getAllAlerts(
            @RequestParam(defaultValue = "30") int expiringDays) {

        List<InventoryItemResponse> lowStockItems = inventoryItemService.getLowStockItems();
        List<InventoryItemResponse> expiredItems = inventoryItemService.getExpiredItems();
        List<InventoryItemResponse> expiringSoonItems = inventoryItemService.getItemsExpiringSoon(expiringDays);
        List<WarehouseResponse> capacityAlerts = warehouseService.getWarehousesWithAlerts();

        CombinedAlertsResponse response = new CombinedAlertsResponse(
            lowStockItems,
            expiredItems,
            expiringSoonItems,
            capacityAlerts
        );

        return ResponseEntity.ok(response);
    }
}
