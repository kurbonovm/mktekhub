/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.model;

/** Enum representing the types of stock activities in the mktekhub inventory management system. */
public enum ActivityType {
  /** New inventory received into warehouse */
  RECEIVE,

  /** Inventory transferred between warehouses */
  TRANSFER,

  /** Inventory sold or removed from system */
  SALE,

  /** Manual quantity adjustment or correction */
  ADJUSTMENT,

  /** Item details updated (non-quantity changes) */
  UPDATE,

  /** Item marked for deletion or removed */
  DELETE
}
