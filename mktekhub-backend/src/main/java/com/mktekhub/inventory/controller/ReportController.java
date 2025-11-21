/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.controller;

import com.mktekhub.inventory.service.ReportService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for report generation and data export */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

  @Autowired private ReportService reportService;

  private static final DateTimeFormatter FILENAME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

  /** Export inventory to CSV */
  @GetMapping("/export/inventory")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  public ResponseEntity<byte[]> exportInventory() {
    byte[] csvData = reportService.exportInventoryToCSV();
    String filename = "inventory_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
  }

  /** Export warehouses to CSV */
  @GetMapping("/export/warehouses")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  public ResponseEntity<byte[]> exportWarehouses() {
    byte[] csvData = reportService.exportWarehousesToCSV();
    String filename = "warehouses_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
  }

  /** Export stock activities to CSV */
  @GetMapping("/export/activities")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  public ResponseEntity<byte[]> exportStockActivities() {
    byte[] csvData = reportService.exportStockActivitiesToCSV();
    String filename = "stock_activities_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
  }

  /** Generate stock valuation report */
  @GetMapping("/valuation")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  public ResponseEntity<byte[]> generateValuationReport() {
    byte[] csvData = reportService.generateStockValuationReport();
    String filename = "stock_valuation_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
  }

  /** Generate low stock report */
  @GetMapping("/low-stock")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  public ResponseEntity<byte[]> generateLowStockReport() {
    byte[] csvData = reportService.generateLowStockReport();
    String filename = "low_stock_report_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
  }

  /** Generate warehouse utilization report */
  @GetMapping("/warehouse-utilization")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  public ResponseEntity<byte[]> generateWarehouseUtilizationReport() {
    byte[] csvData = reportService.generateWarehouseUtilizationReport();
    String filename =
        "warehouse_utilization_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
  }

  /** Generate stock movement report */
  @GetMapping("/stock-movement")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  public ResponseEntity<byte[]> generateStockMovementReport() {
    byte[] csvData = reportService.generateStockMovementReport();
    String filename = "stock_movement_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
  }

  /** Generate inventory summary by category */
  @GetMapping("/inventory-by-category")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  public ResponseEntity<byte[]> generateInventorySummaryByCategory() {
    byte[] csvData = reportService.generateInventorySummaryByCategory();
    String filename =
        "inventory_by_category_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
  }

  /** Generate filtered stock activity report */
  @GetMapping("/custom/stock-activity")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  public ResponseEntity<byte[]> generateFilteredStockActivityReport(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String brand,
      @RequestParam(required = false) Long warehouseId,
      @RequestParam(required = false) String activityType) {

    byte[] csvData =
        reportService.generateFilteredStockActivityReport(
            Optional.ofNullable(startDate),
            Optional.ofNullable(endDate),
            Optional.ofNullable(category),
            Optional.ofNullable(brand),
            Optional.ofNullable(warehouseId),
            Optional.ofNullable(activityType));

    String filename =
        "filtered_stock_activity_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
  }

  /** Generate filtered inventory valuation report */
  @GetMapping("/custom/inventory-valuation")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
  public ResponseEntity<byte[]> generateFilteredInventoryValuationReport(
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String brand,
      @RequestParam(required = false) Long warehouseId) {

    byte[] csvData =
        reportService.generateFilteredInventoryValuationReport(
            Optional.ofNullable(category),
            Optional.ofNullable(brand),
            Optional.ofNullable(warehouseId));

    String filename =
        "filtered_inventory_valuation_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", filename);
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

    return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
  }
}
