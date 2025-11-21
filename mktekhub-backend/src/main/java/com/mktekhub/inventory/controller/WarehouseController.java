/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.controller;

import com.mktekhub.inventory.dto.MessageResponse;
import com.mktekhub.inventory.dto.WarehouseRequest;
import com.mktekhub.inventory.dto.WarehouseResponse;
import com.mktekhub.inventory.service.WarehouseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for warehouse management. */
@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

  @Autowired private WarehouseService warehouseService;

  /** Get all warehouses. Accessible by all authenticated users. GET /api/warehouses */
  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  public ResponseEntity<List<WarehouseResponse>> getAllWarehouses() {
    List<WarehouseResponse> warehouses = warehouseService.getAllWarehouses();
    return ResponseEntity.ok(warehouses);
  }

  /**
   * Get all active warehouses. Accessible by all authenticated users. GET /api/warehouses/active
   */
  @GetMapping("/active")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  public ResponseEntity<List<WarehouseResponse>> getActiveWarehouses() {
    List<WarehouseResponse> warehouses = warehouseService.getActiveWarehouses();
    return ResponseEntity.ok(warehouses);
  }

  /**
   * Get warehouses with capacity alerts. Accessible by ADMIN and MANAGER. GET
   * /api/warehouses/alerts
   */
  @GetMapping("/alerts")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<List<WarehouseResponse>> getWarehousesWithAlerts() {
    List<WarehouseResponse> warehouses = warehouseService.getWarehousesWithAlerts();
    return ResponseEntity.ok(warehouses);
  }

  /** Get warehouse by ID. Accessible by all authenticated users. GET /api/warehouses/{id} */
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  public ResponseEntity<WarehouseResponse> getWarehouseById(@PathVariable Long id) {
    WarehouseResponse warehouse = warehouseService.getWarehouseById(id);
    return ResponseEntity.ok(warehouse);
  }

  /** Create a new warehouse. Accessible by ADMIN and MANAGER only. POST /api/warehouses */
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<WarehouseResponse> createWarehouse(
      @Valid @RequestBody WarehouseRequest request) {
    WarehouseResponse warehouse = warehouseService.createWarehouse(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(warehouse);
  }

  /**
   * Update an existing warehouse. Accessible by ADMIN and MANAGER only. PUT /api/warehouses/{id}
   */
  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<WarehouseResponse> updateWarehouse(
      @PathVariable Long id, @Valid @RequestBody WarehouseRequest request) {
    WarehouseResponse warehouse = warehouseService.updateWarehouse(id, request);
    return ResponseEntity.ok(warehouse);
  }

  /**
   * Delete a warehouse (soft delete). Accessible by ADMIN and MANAGER only. DELETE
   * /api/warehouses/{id}
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<MessageResponse> deleteWarehouse(@PathVariable Long id) {
    warehouseService.deleteWarehouse(id);
    return ResponseEntity.ok(new MessageResponse("Warehouse deleted successfully"));
  }

  /**
   * Permanently delete a warehouse. Accessible by ADMIN only. DELETE /api/warehouses/{id}/permanent
   */
  @DeleteMapping("/{id}/permanent")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MessageResponse> hardDeleteWarehouse(@PathVariable Long id) {
    warehouseService.hardDeleteWarehouse(id);
    return ResponseEntity.ok(new MessageResponse("Warehouse permanently deleted"));
  }
}
