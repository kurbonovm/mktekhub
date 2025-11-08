package com.mktekhub.inventory.model;

/**
 * Enum representing the types of audit actions in the mktekhub inventory management system.
 */
public enum AuditAction {
    /**
     * Entity was created
     */
    CREATE,

    /**
     * Entity was updated
     */
    UPDATE,

    /**
     * Entity was deleted
     */
    DELETE
}
