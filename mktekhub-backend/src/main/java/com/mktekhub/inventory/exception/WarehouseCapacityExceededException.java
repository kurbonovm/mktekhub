/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.exception;

import java.math.BigDecimal;

public class WarehouseCapacityExceededException extends RuntimeException {
  public WarehouseCapacityExceededException(String message) {
    super(message);
  }

  public WarehouseCapacityExceededException(
      String warehouseName, BigDecimal available, BigDecimal requested) {
    super(
        String.format(
            "Warehouse '%s' capacity exceeded. Available capacity: %.2f ft³, Requested: %.2f ft³",
            warehouseName, available, requested));
  }
}
