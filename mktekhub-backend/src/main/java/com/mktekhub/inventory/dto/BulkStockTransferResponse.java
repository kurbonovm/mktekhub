/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.dto;

import java.util.List;

/** DTO for bulk stock transfer response. */
public class BulkStockTransferResponse {
  private int totalTransfers;
  private int successfulTransfers;
  private int failedTransfers;
  private List<StockTransferResponse> successResults;
  private List<TransferError> errors;

  // Constructors
  public BulkStockTransferResponse() {}

  public BulkStockTransferResponse(
      int totalTransfers,
      int successfulTransfers,
      int failedTransfers,
      List<StockTransferResponse> successResults,
      List<TransferError> errors) {
    this.totalTransfers = totalTransfers;
    this.successfulTransfers = successfulTransfers;
    this.failedTransfers = failedTransfers;
    this.successResults = successResults;
    this.errors = errors;
  }

  // Getters and Setters
  public int getTotalTransfers() {
    return totalTransfers;
  }

  public void setTotalTransfers(int totalTransfers) {
    this.totalTransfers = totalTransfers;
  }

  public int getSuccessfulTransfers() {
    return successfulTransfers;
  }

  public void setSuccessfulTransfers(int successfulTransfers) {
    this.successfulTransfers = successfulTransfers;
  }

  public int getFailedTransfers() {
    return failedTransfers;
  }

  public void setFailedTransfers(int failedTransfers) {
    this.failedTransfers = failedTransfers;
  }

  public List<StockTransferResponse> getSuccessResults() {
    return successResults;
  }

  public void setSuccessResults(List<StockTransferResponse> successResults) {
    this.successResults = successResults;
  }

  public List<TransferError> getErrors() {
    return errors;
  }

  public void setErrors(List<TransferError> errors) {
    this.errors = errors;
  }

  /** Inner class for transfer error details. */
  public static class TransferError {
    private int transferIndex;
    private String itemSku;
    private String errorMessage;

    public TransferError() {}

    public TransferError(int transferIndex, String itemSku, String errorMessage) {
      this.transferIndex = transferIndex;
      this.itemSku = itemSku;
      this.errorMessage = errorMessage;
    }

    public int getTransferIndex() {
      return transferIndex;
    }

    public void setTransferIndex(int transferIndex) {
      this.transferIndex = transferIndex;
    }

    public String getItemSku() {
      return itemSku;
    }

    public void setItemSku(String itemSku) {
      this.itemSku = itemSku;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
    }
  }
}
