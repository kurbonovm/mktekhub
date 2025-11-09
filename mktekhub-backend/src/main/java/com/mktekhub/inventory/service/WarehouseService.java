package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.WarehouseRequest;
import com.mktekhub.inventory.dto.WarehouseResponse;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for warehouse management operations.
 */
@Service
public class WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    /**
     * Get all warehouses.
     */
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(WarehouseResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all active warehouses.
     */
    public List<WarehouseResponse> getActiveWarehouses() {
        return warehouseRepository.findByIsActiveTrue().stream()
                .map(WarehouseResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get warehouse by ID.
     */
    public WarehouseResponse getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + id));
        return WarehouseResponse.fromEntity(warehouse);
    }

    /**
     * Get warehouses with capacity alerts.
     */
    public List<WarehouseResponse> getWarehousesWithAlerts() {
        return warehouseRepository.findWarehousesWithCapacityAlert().stream()
                .map(WarehouseResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Create a new warehouse.
     */
    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        // Check if warehouse name already exists
        if (warehouseRepository.existsByName(request.getName())) {
            throw new RuntimeException("Warehouse with name '" + request.getName() + "' already exists");
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setName(request.getName());
        warehouse.setLocation(request.getLocation());
        warehouse.setMaxCapacity(request.getMaxCapacity());
        warehouse.setCurrentCapacity(0);
        warehouse.setIsActive(true);

        // Set threshold or use default
        if (request.getCapacityAlertThreshold() != null) {
            warehouse.setCapacityAlertThreshold(request.getCapacityAlertThreshold());
        } else {
            warehouse.setCapacityAlertThreshold(new BigDecimal("80.00"));
        }

        Warehouse saved = warehouseRepository.save(warehouse);
        return WarehouseResponse.fromEntity(saved);
    }

    /**
     * Update an existing warehouse.
     */
    @Transactional
    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + id));

        // Check if new name conflicts with existing warehouse
        if (!warehouse.getName().equals(request.getName()) &&
                warehouseRepository.existsByName(request.getName())) {
            throw new RuntimeException("Warehouse with name '" + request.getName() + "' already exists");
        }

        warehouse.setName(request.getName());
        warehouse.setLocation(request.getLocation());
        warehouse.setMaxCapacity(request.getMaxCapacity());

        if (request.getCapacityAlertThreshold() != null) {
            warehouse.setCapacityAlertThreshold(request.getCapacityAlertThreshold());
        }

        Warehouse updated = warehouseRepository.save(warehouse);
        return WarehouseResponse.fromEntity(updated);
    }

    /**
     * Delete a warehouse (soft delete by setting isActive to false).
     */
    @Transactional
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + id));

        // Check if warehouse has inventory items
        if (warehouse.getCurrentCapacity() > 0) {
            throw new RuntimeException("Cannot delete warehouse with existing inventory. " +
                    "Please move or remove all items first.");
        }

        // Soft delete
        warehouse.setIsActive(false);
        warehouseRepository.save(warehouse);
    }

    /**
     * Permanently delete a warehouse.
     */
    @Transactional
    public void hardDeleteWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + id));

        if (warehouse.getCurrentCapacity() > 0) {
            throw new RuntimeException("Cannot delete warehouse with existing inventory.");
        }

        warehouseRepository.delete(warehouse);
    }
}
