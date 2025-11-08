package com.mktekhub.inventory.repository;

import com.mktekhub.inventory.model.Audit;
import com.mktekhub.inventory.model.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Audit entity.
 * Provides database operations for audit log management.
 */
@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {

    /**
     * Find all audit records for a specific entity type and ID.
     */
    List<Audit> findByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Find all audit records by action type.
     */
    List<Audit> findByAction(AuditAction action);

    /**
     * Find all audit records created by a specific user.
     */
    List<Audit> findByCreatedById(Long userId);

    /**
     * Find all audit records updated by a specific user.
     */
    List<Audit> findByUpdatedById(Long userId);

    /**
     * Find all audit records within a date range.
     */
    List<Audit> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all audit records for a specific entity type.
     */
    List<Audit> findByEntityType(String entityType);
}
