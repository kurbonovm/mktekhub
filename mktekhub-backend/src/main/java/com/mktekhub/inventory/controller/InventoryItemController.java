package com.mktekhub.inventory.controller;

import com.mktekhub.inventory.dto.InventoryItemRequest;
import com.mktekhub.inventory.dto.InventoryItemResponse;
import com.mktekhub.inventory.dto.MessageResponse;
import com.mktekhub.inventory.service.InventoryItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for inventory item management.
 */
@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InventoryItemController {

    @Autowired
    private InventoryItemService inventoryItemService;

    /**
     * Get all inventory items.
     * Accessible by all authenticated users.
     * GET /api/inventory
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<List<InventoryItemResponse>> getAllItems() {
        List<InventoryItemResponse> items = inventoryItemService.getAllItems();
        return ResponseEntity.ok(items);
    }

    /**
     * Get inventory item by ID.
     * Accessible by all authenticated users.
     * GET /api/inventory/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<?> getItemById(@PathVariable Long id) {
        try {
            InventoryItemResponse item = inventoryItemService.getItemById(id);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Get inventory item by SKU.
     * Accessible by all authenticated users.
     * GET /api/inventory/sku/{sku}
     */
    @GetMapping("/sku/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<?> getItemBySku(@PathVariable String sku) {
        try {
            InventoryItemResponse item = inventoryItemService.getItemBySku(sku);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Get all items in a specific warehouse.
     * Accessible by all authenticated users.
     * GET /api/inventory/warehouse/{warehouseId}
     */
    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<List<InventoryItemResponse>> getItemsByWarehouse(@PathVariable Long warehouseId) {
        List<InventoryItemResponse> items = inventoryItemService.getItemsByWarehouse(warehouseId);
        return ResponseEntity.ok(items);
    }

    /**
     * Get all items by category.
     * Accessible by all authenticated users.
     * GET /api/inventory/category/{category}
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public ResponseEntity<List<InventoryItemResponse>> getItemsByCategory(@PathVariable String category) {
        List<InventoryItemResponse> items = inventoryItemService.getItemsByCategory(category);
        return ResponseEntity.ok(items);
    }

    /**
     * Get all low stock items.
     * Accessible by ADMIN and MANAGER.
     * GET /api/inventory/alerts/low-stock
     */
    @GetMapping("/alerts/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<InventoryItemResponse>> getLowStockItems() {
        List<InventoryItemResponse> items = inventoryItemService.getLowStockItems();
        return ResponseEntity.ok(items);
    }

    /**
     * Get all expired items.
     * Accessible by ADMIN and MANAGER.
     * GET /api/inventory/alerts/expired
     */
    @GetMapping("/alerts/expired")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<InventoryItemResponse>> getExpiredItems() {
        List<InventoryItemResponse> items = inventoryItemService.getExpiredItems();
        return ResponseEntity.ok(items);
    }

    /**
     * Get items expiring within specified days.
     * Accessible by ADMIN and MANAGER.
     * GET /api/inventory/alerts/expiring?days=30
     */
    @GetMapping("/alerts/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<InventoryItemResponse>> getItemsExpiringSoon(
            @RequestParam(defaultValue = "30") int days) {
        List<InventoryItemResponse> items = inventoryItemService.getItemsExpiringSoon(days);
        return ResponseEntity.ok(items);
    }

    /**
     * Create a new inventory item.
     * Accessible by ADMIN and MANAGER only.
     * POST /api/inventory
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createItem(@Valid @RequestBody InventoryItemRequest request) {
        try {
            InventoryItemResponse item = inventoryItemService.createItem(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(item);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Update an existing inventory item.
     * Accessible by ADMIN and MANAGER only.
     * PUT /api/inventory/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateItem(@PathVariable Long id,
                                         @Valid @RequestBody InventoryItemRequest request) {
        try {
            InventoryItemResponse item = inventoryItemService.updateItem(id, request);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Delete an inventory item.
     * Accessible by ADMIN and MANAGER only.
     * DELETE /api/inventory/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        try {
            inventoryItemService.deleteItem(id);
            return ResponseEntity.ok(new MessageResponse("Inventory item deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Adjust inventory quantity.
     * Accessible by ADMIN and MANAGER only.
     * PATCH /api/inventory/{id}/adjust
     */
    @PatchMapping("/{id}/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> adjustQuantity(@PathVariable Long id,
                                             @RequestBody Map<String, Integer> request) {
        try {
            Integer quantityChange = request.get("quantityChange");
            if (quantityChange == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("quantityChange is required"));
            }

            InventoryItemResponse item = inventoryItemService.adjustQuantity(id, quantityChange);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
