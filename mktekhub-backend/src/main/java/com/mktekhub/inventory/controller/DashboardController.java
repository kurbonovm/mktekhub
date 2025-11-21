/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.controller;

import com.mktekhub.inventory.dto.DashboardSummary;
import com.mktekhub.inventory.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for dashboard and reporting. */
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "APIs for dashboard statistics and reports")
public class DashboardController {

  @Autowired private DashboardService dashboardService;

  /** Get comprehensive dashboard summary Accessible by all authenticated users */
  @GetMapping("/summary")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  @Operation(
      summary = "Get dashboard summary",
      description =
          "Get comprehensive dashboard summary including warehouse, inventory, and alerts statistics")
  public ResponseEntity<DashboardSummary> getDashboardSummary() {
    DashboardSummary summary = dashboardService.getDashboardSummary();
    return ResponseEntity.ok(summary);
  }

  /** Get warehouse summary statistics Accessible by all authenticated users */
  @GetMapping("/warehouse-summary")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  @Operation(
      summary = "Get warehouse summary",
      description = "Get warehouse statistics including capacity and utilization")
  public ResponseEntity<DashboardSummary.WarehouseSummary> getWarehouseSummary() {
    DashboardSummary.WarehouseSummary summary = dashboardService.getWarehouseSummary();
    return ResponseEntity.ok(summary);
  }

  /** Get inventory summary statistics Accessible by all authenticated users */
  @GetMapping("/inventory-summary")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  @Operation(
      summary = "Get inventory summary",
      description = "Get inventory statistics including total items, value, and SKUs")
  public ResponseEntity<DashboardSummary.InventorySummary> getInventorySummary() {
    DashboardSummary.InventorySummary summary = dashboardService.getInventorySummary();
    return ResponseEntity.ok(summary);
  }

  /** Get alerts summary statistics Accessible by ADMIN and MANAGER */
  @GetMapping("/alerts-summary")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @Operation(
      summary = "Get alerts summary",
      description = "Get alerts statistics including low stock, expired, and capacity alerts")
  public ResponseEntity<DashboardSummary.AlertsSummary> getAlertsSummary() {
    DashboardSummary.AlertsSummary summary = dashboardService.getAlertsSummary();
    return ResponseEntity.ok(summary);
  }
}
