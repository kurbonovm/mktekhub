package com.mktekhub.inventory.repository;

import com.mktekhub.inventory.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Warehouse entity.
 * Provides database operations for warehouse management.
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    /**
     * Find a warehouse by name.
     */
    Optional<Warehouse> findByName(String name);

    /**
     * Find all active warehouses.
     */
    List<Warehouse> findByIsActiveTrue();

    /**
     * Find warehouses where current capacity exceeds the alert threshold.
     */
    @Query("SELECT w FROM Warehouse w WHERE (w.currentCapacity * 100.0 / w.maxCapacity) >= w.capacityAlertThreshold")
    List<Warehouse> findWarehousesWithCapacityAlert();

    /**
     * Check if a warehouse exists by name.
     */
    boolean existsByName(String name);
}
