package com.mktekhub.inventory.exception;

public class WarehouseCapacityExceededException extends RuntimeException {
    public WarehouseCapacityExceededException(String message) {
        super(message);
    }

    public WarehouseCapacityExceededException(String warehouseName, int available, int requested) {
        super(String.format("Warehouse '%s' capacity exceeded. Available capacity: %d, Requested: %d",
            warehouseName, available, requested));
    }
}
