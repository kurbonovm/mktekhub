/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.exception;

public class InsufficientStockException extends RuntimeException {
  public InsufficientStockException(String message) {
    super(message);
  }

  public InsufficientStockException(String sku, int available, int requested) {
    super(
        String.format(
            "Insufficient stock for SKU '%s'. Available: %d, Requested: %d",
            sku, available, requested));
  }
}
