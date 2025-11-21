/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.controller;

import com.mktekhub.inventory.dto.StockActivityResponse;
import com.mktekhub.inventory.model.ActivityType;
import com.mktekhub.inventory.service.StockActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Controller for stock activity history and tracking. */
@RestController
@RequestMapping("/api/stock-activities")
@Tag(name = "Stock Activity", description = "APIs for viewing stock activity history and tracking")
public class StockActivityController {

  @Autowired private StockActivityService stockActivityService;

  /** Get all stock activities */
  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VIEWER')")
  @Operation(
      summary = "Get all stock activities",
      description = "Retrieve all stock activity records")
  public ResponseEntity<List<StockActivityResponse>> getAllActivities() {
    return ResponseEntity.ok(stockActivityService.getAllActivities());
  }

  /** Get stock activity by ID */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VIEWER')")
  @Operation(
      summary = "Get activity by ID",
      description = "Retrieve a specific stock activity by its ID")
  public ResponseEntity<StockActivityResponse> getActivityById(@PathVariable Long id) {
    return ResponseEntity.ok(stockActivityService.getActivityById(id));
  }

  /** Get activities by item ID */
  @GetMapping("/item/{itemId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VIEWER')")
  @Operation(
      summary = "Get activities by item ID",
      description =
          "Retrieve all stock activities for a specific inventory item (sorted by timestamp)")
  public ResponseEntity<List<StockActivityResponse>> getActivitiesByItemId(
      @PathVariable Long itemId) {
    return ResponseEntity.ok(stockActivityService.getActivitiesByItemIdSorted(itemId));
  }

  /** Get activities by item SKU */
  @GetMapping("/sku/{sku}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VIEWER')")
  @Operation(
      summary = "Get activities by SKU",
      description = "Retrieve all stock activities for an item by its SKU")
  public ResponseEntity<List<StockActivityResponse>> getActivitiesByItemSku(
      @PathVariable String sku) {
    return ResponseEntity.ok(stockActivityService.getActivitiesByItemSku(sku));
  }

  /** Get activities by activity type */
  @GetMapping("/type/{activityType}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VIEWER')")
  @Operation(
      summary = "Get activities by type",
      description =
          "Retrieve activities filtered by type (RECEIVE, TRANSFER, SALE, ADJUSTMENT, DELETE)")
  public ResponseEntity<List<StockActivityResponse>> getActivitiesByType(
      @PathVariable ActivityType activityType) {
    return ResponseEntity.ok(stockActivityService.getActivitiesByType(activityType));
  }

  /** Get activities by user ID */
  @GetMapping("/user/{userId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
  @Operation(
      summary = "Get activities by user ID",
      description = "Retrieve all activities performed by a specific user")
  public ResponseEntity<List<StockActivityResponse>> getActivitiesByUserId(
      @PathVariable Long userId) {
    return ResponseEntity.ok(stockActivityService.getActivitiesByUserId(userId));
  }

  /** Get activities by username */
  @GetMapping("/user/username/{username}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
  @Operation(
      summary = "Get activities by username",
      description = "Retrieve all activities performed by a specific user (by username)")
  public ResponseEntity<List<StockActivityResponse>> getActivitiesByUsername(
      @PathVariable String username) {
    return ResponseEntity.ok(stockActivityService.getActivitiesByUsername(username));
  }

  /** Get activities by warehouse (both source and destination) */
  @GetMapping("/warehouse/{warehouseId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VIEWER')")
  @Operation(
      summary = "Get activities by warehouse",
      description = "Retrieve all activities related to a warehouse (as source or destination)")
  public ResponseEntity<List<StockActivityResponse>> getActivitiesByWarehouse(
      @PathVariable Long warehouseId) {
    return ResponseEntity.ok(stockActivityService.getActivitiesByWarehouse(warehouseId));
  }

  /** Get activities by source warehouse */
  @GetMapping("/warehouse/source/{warehouseId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VIEWER')")
  @Operation(
      summary = "Get activities by source warehouse",
      description = "Retrieve activities where the warehouse was the source")
  public ResponseEntity<List<StockActivityResponse>> getActivitiesBySourceWarehouse(
      @PathVariable Long warehouseId) {
    return ResponseEntity.ok(stockActivityService.getActivitiesBySourceWarehouse(warehouseId));
  }

  /** Get activities by destination warehouse */
  @GetMapping("/warehouse/destination/{warehouseId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VIEWER')")
  @Operation(
      summary = "Get activities by destination warehouse",
      description = "Retrieve activities where the warehouse was the destination")
  public ResponseEntity<List<StockActivityResponse>> getActivitiesByDestinationWarehouse(
      @PathVariable Long warehouseId) {
    return ResponseEntity.ok(stockActivityService.getActivitiesByDestinationWarehouse(warehouseId));
  }

  /** Get activities by date range */
  @GetMapping("/date-range")
  @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VIEWER')")
  @Operation(
      summary = "Get activities by date range",
      description = "Retrieve activities within a specific date range")
  public ResponseEntity<List<StockActivityResponse>> getActivitiesByDateRange(
      @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          @Parameter(description = "Start date-time (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
          LocalDateTime startDate,
      @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          @Parameter(description = "End date-time (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
          LocalDateTime endDate) {
    return ResponseEntity.ok(stockActivityService.getActivitiesByDateRange(startDate, endDate));
  }

  /** Get activities with multiple filters All query parameters are optional */
  @GetMapping("/filter")
  @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('VIEWER')")
  @Operation(
      summary = "Get activities with filters",
      description =
          "Retrieve activities using multiple optional filters (item, type, user, warehouse, date range)")
  public ResponseEntity<List<StockActivityResponse>> getActivitiesWithFilters(
      @RequestParam(required = false) @Parameter(description = "Item ID") Long itemId,
      @RequestParam(required = false) @Parameter(description = "Item SKU") String sku,
      @RequestParam(required = false) @Parameter(description = "Activity type")
          ActivityType activityType,
      @RequestParam(required = false) @Parameter(description = "User ID") Long userId,
      @RequestParam(required = false) @Parameter(description = "Username") String username,
      @RequestParam(required = false) @Parameter(description = "Warehouse ID") Long warehouseId,
      @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          @Parameter(description = "Start date-time (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
          LocalDateTime startDate,
      @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          @Parameter(description = "End date-time (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
          LocalDateTime endDate) {

    return ResponseEntity.ok(
        stockActivityService.getActivitiesWithFilters(
            itemId, sku, activityType, userId, username, warehouseId, startDate, endDate));
  }
}
