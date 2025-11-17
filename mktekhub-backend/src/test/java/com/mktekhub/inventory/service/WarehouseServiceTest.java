package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.WarehouseRequest;
import com.mktekhub.inventory.dto.WarehouseResponse;
import com.mktekhub.inventory.exception.DuplicateResourceException;
import com.mktekhub.inventory.exception.InvalidOperationException;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for WarehouseService.
 * Tests all CRUD operations and warehouse management functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WarehouseService Tests")
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    private Warehouse warehouse;
    private WarehouseRequest warehouseRequest;

    @BeforeEach
    void setUp() {
        // Setup warehouse entity
        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setName("Main Warehouse");
        warehouse.setLocation("New York");
        warehouse.setMaxCapacity(new BigDecimal("10000.00"));
        warehouse.setCurrentCapacity(new BigDecimal("5000.00"));
        warehouse.setCapacityAlertThreshold(new BigDecimal("80.00"));
        warehouse.setIsActive(true);

        // Setup warehouse request
        warehouseRequest = new WarehouseRequest();
        warehouseRequest.setName("Main Warehouse");
        warehouseRequest.setLocation("New York");
        warehouseRequest.setMaxCapacity(new BigDecimal("10000.00"));
        warehouseRequest.setCapacityAlertThreshold(new BigDecimal("80.00"));
    }

    // ==================== GET ALL WAREHOUSES TESTS ====================

    @Test
    @DisplayName("GetAllWarehouses - Should return all warehouses")
    void getAllWarehouses_Success() {
        // Arrange
        Warehouse warehouse2 = new Warehouse();
        warehouse2.setId(2L);
        warehouse2.setName("Secondary Warehouse");
        warehouse2.setLocation("Boston");
        warehouse2.setMaxCapacity(new BigDecimal("5000.00"));
        warehouse2.setCurrentCapacity(BigDecimal.ZERO);
        warehouse2.setCapacityAlertThreshold(new BigDecimal("75.00"));
        warehouse2.setIsActive(false);

        when(warehouseRepository.findAll()).thenReturn(Arrays.asList(warehouse, warehouse2));

        // Act
        List<WarehouseResponse> result = warehouseService.getAllWarehouses();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Main Warehouse", result.get(0).getName());
        assertEquals("Secondary Warehouse", result.get(1).getName());

        verify(warehouseRepository).findAll();
    }

    @Test
    @DisplayName("GetAllWarehouses - Should return empty list when no warehouses exist")
    void getAllWarehouses_Empty() {
        // Arrange
        when(warehouseRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<WarehouseResponse> result = warehouseService.getAllWarehouses();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(warehouseRepository).findAll();
    }

    // ==================== GET ACTIVE WAREHOUSES TESTS ====================

    @Test
    @DisplayName("GetActiveWarehouses - Should return only active warehouses")
    void getActiveWarehouses_Success() {
        // Arrange
        Warehouse inactiveWarehouse = new Warehouse();
        inactiveWarehouse.setId(2L);
        inactiveWarehouse.setName("Inactive Warehouse");
        inactiveWarehouse.setIsActive(false);

        when(warehouseRepository.findByIsActiveTrue()).thenReturn(Collections.singletonList(warehouse));

        // Act
        List<WarehouseResponse> result = warehouseService.getActiveWarehouses();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Main Warehouse", result.get(0).getName());
        assertTrue(result.get(0).getIsActive());

        verify(warehouseRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("GetActiveWarehouses - Should return empty list when no active warehouses")
    void getActiveWarehouses_Empty() {
        // Arrange
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());

        // Act
        List<WarehouseResponse> result = warehouseService.getActiveWarehouses();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(warehouseRepository).findByIsActiveTrue();
    }

    // ==================== GET WAREHOUSE BY ID TESTS ====================

    @Test
    @DisplayName("GetWarehouseById - Should return warehouse when found")
    void getWarehouseById_Success() {
        // Arrange
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        // Act
        WarehouseResponse result = warehouseService.getWarehouseById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Main Warehouse", result.getName());
        assertEquals("New York", result.getLocation());

        verify(warehouseRepository).findById(1L);
    }

    @Test
    @DisplayName("GetWarehouseById - Should throw ResourceNotFoundException when not found")
    void getWarehouseById_NotFound() {
        // Arrange
        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> warehouseService.getWarehouseById(999L)
        );

        assertTrue(exception.getMessage().contains("Warehouse"));
        assertTrue(exception.getMessage().contains("999"));

        verify(warehouseRepository).findById(999L);
    }

    // ==================== GET WAREHOUSES WITH ALERTS TESTS ====================

    @Test
    @DisplayName("GetWarehousesWithAlerts - Should return warehouses with capacity alerts")
    void getWarehousesWithAlerts_Success() {
        // Arrange
        Warehouse alertWarehouse = new Warehouse();
        alertWarehouse.setId(3L);
        alertWarehouse.setName("Alert Warehouse");
        alertWarehouse.setMaxCapacity(new BigDecimal("1000.00"));
        alertWarehouse.setCurrentCapacity(new BigDecimal("850.00")); // 85% utilization
        alertWarehouse.setCapacityAlertThreshold(new BigDecimal("80.00"));

        when(warehouseRepository.findWarehousesWithCapacityAlert())
            .thenReturn(Collections.singletonList(alertWarehouse));

        // Act
        List<WarehouseResponse> result = warehouseService.getWarehousesWithAlerts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Alert Warehouse", result.get(0).getName());

        verify(warehouseRepository).findWarehousesWithCapacityAlert();
    }

    // ==================== CREATE WAREHOUSE TESTS ====================

    @Test
    @DisplayName("CreateWarehouse - Should create warehouse successfully with custom threshold")
    void createWarehouse_Success() {
        // Arrange
        when(warehouseRepository.existsByName(warehouseRequest.getName())).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        // Act
        WarehouseResponse result = warehouseService.createWarehouse(warehouseRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Main Warehouse", result.getName());

        // Verify saved warehouse
        ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
        verify(warehouseRepository).save(warehouseCaptor.capture());
        Warehouse savedWarehouse = warehouseCaptor.getValue();

        assertEquals(warehouseRequest.getName(), savedWarehouse.getName());
        assertEquals(warehouseRequest.getLocation(), savedWarehouse.getLocation());
        assertEquals(warehouseRequest.getMaxCapacity(), savedWarehouse.getMaxCapacity());
        assertEquals(BigDecimal.ZERO, savedWarehouse.getCurrentCapacity());
        assertEquals(warehouseRequest.getCapacityAlertThreshold(), savedWarehouse.getCapacityAlertThreshold());
        assertTrue(savedWarehouse.getIsActive());

        verify(warehouseRepository).existsByName(warehouseRequest.getName());
    }

    @Test
    @DisplayName("CreateWarehouse - Should use default threshold when not provided")
    void createWarehouse_DefaultThreshold() {
        // Arrange
        warehouseRequest.setCapacityAlertThreshold(null);
        when(warehouseRepository.existsByName(warehouseRequest.getName())).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        // Act
        WarehouseResponse result = warehouseService.createWarehouse(warehouseRequest);

        // Assert
        assertNotNull(result);

        // Verify saved warehouse has default threshold
        ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
        verify(warehouseRepository).save(warehouseCaptor.capture());
        Warehouse savedWarehouse = warehouseCaptor.getValue();

        assertEquals(new BigDecimal("80.00"), savedWarehouse.getCapacityAlertThreshold());
    }

    @Test
    @DisplayName("CreateWarehouse - Should throw DuplicateResourceException when name exists")
    void createWarehouse_DuplicateName() {
        // Arrange
        when(warehouseRepository.existsByName(warehouseRequest.getName())).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> warehouseService.createWarehouse(warehouseRequest)
        );

        assertTrue(exception.getMessage().contains("Warehouse"));
        assertTrue(exception.getMessage().contains(warehouseRequest.getName()));

        verify(warehouseRepository).existsByName(warehouseRequest.getName());
        verify(warehouseRepository, never()).save(any());
    }

    // ==================== UPDATE WAREHOUSE TESTS ====================

    @Test
    @DisplayName("UpdateWarehouse - Should update warehouse successfully")
    void updateWarehouse_Success() {
        // Arrange
        warehouseRequest.setName("Updated Warehouse");
        warehouseRequest.setLocation("Boston");
        warehouseRequest.setMaxCapacity(new BigDecimal("15000.00"));

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.existsByName("Updated Warehouse")).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        // Act
        WarehouseResponse result = warehouseService.updateWarehouse(1L, warehouseRequest);

        // Assert
        assertNotNull(result);

        // Verify updated warehouse
        ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
        verify(warehouseRepository).save(warehouseCaptor.capture());
        Warehouse updatedWarehouse = warehouseCaptor.getValue();

        assertEquals("Updated Warehouse", updatedWarehouse.getName());
        assertEquals("Boston", updatedWarehouse.getLocation());
        assertEquals(new BigDecimal("15000.00"), updatedWarehouse.getMaxCapacity());

        verify(warehouseRepository).findById(1L);
    }

    @Test
    @DisplayName("UpdateWarehouse - Should allow keeping same name")
    void updateWarehouse_SameName() {
        // Arrange
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        // Act
        WarehouseResponse result = warehouseService.updateWarehouse(1L, warehouseRequest);

        // Assert
        assertNotNull(result);

        // Verify existsByName was not called since name didn't change
        verify(warehouseRepository, never()).existsByName(any());
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    @DisplayName("UpdateWarehouse - Should throw DuplicateResourceException when new name exists")
    void updateWarehouse_DuplicateName() {
        // Arrange
        warehouseRequest.setName("Existing Warehouse");

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.existsByName("Existing Warehouse")).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> warehouseService.updateWarehouse(1L, warehouseRequest)
        );

        assertTrue(exception.getMessage().contains("Warehouse"));
        assertTrue(exception.getMessage().contains("Existing Warehouse"));

        verify(warehouseRepository).findById(1L);
        verify(warehouseRepository).existsByName("Existing Warehouse");
        verify(warehouseRepository, never()).save(any());
    }

    @Test
    @DisplayName("UpdateWarehouse - Should throw ResourceNotFoundException when warehouse not found")
    void updateWarehouse_NotFound() {
        // Arrange
        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> warehouseService.updateWarehouse(999L, warehouseRequest)
        );

        assertTrue(exception.getMessage().contains("Warehouse"));
        assertTrue(exception.getMessage().contains("999"));

        verify(warehouseRepository).findById(999L);
        verify(warehouseRepository, never()).save(any());
    }

    // ==================== DELETE WAREHOUSE (SOFT DELETE) TESTS ====================

    @Test
    @DisplayName("DeleteWarehouse - Should soft delete warehouse when capacity is zero")
    void deleteWarehouse_Success() {
        // Arrange
        warehouse.setCurrentCapacity(BigDecimal.ZERO);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        // Act
        warehouseService.deleteWarehouse(1L);

        // Assert
        ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
        verify(warehouseRepository).save(warehouseCaptor.capture());
        Warehouse deletedWarehouse = warehouseCaptor.getValue();

        assertFalse(deletedWarehouse.getIsActive());

        verify(warehouseRepository).findById(1L);
    }

    @Test
    @DisplayName("DeleteWarehouse - Should throw InvalidOperationException when warehouse has inventory")
    void deleteWarehouse_HasInventory() {
        // Arrange
        warehouse.setCurrentCapacity(new BigDecimal("100.00"));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        // Act & Assert
        InvalidOperationException exception = assertThrows(
            InvalidOperationException.class,
            () -> warehouseService.deleteWarehouse(1L)
        );

        assertTrue(exception.getMessage().contains("Cannot delete warehouse with existing inventory"));

        verify(warehouseRepository).findById(1L);
        verify(warehouseRepository, never()).save(any());
        verify(warehouseRepository, never()).delete(any());
    }

    @Test
    @DisplayName("DeleteWarehouse - Should throw ResourceNotFoundException when not found")
    void deleteWarehouse_NotFound() {
        // Arrange
        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> warehouseService.deleteWarehouse(999L)
        );

        assertTrue(exception.getMessage().contains("Warehouse"));
        assertTrue(exception.getMessage().contains("999"));

        verify(warehouseRepository).findById(999L);
        verify(warehouseRepository, never()).save(any());
    }

    // ==================== HARD DELETE WAREHOUSE TESTS ====================

    @Test
    @DisplayName("HardDeleteWarehouse - Should permanently delete warehouse when capacity is zero")
    void hardDeleteWarehouse_Success() {
        // Arrange
        warehouse.setCurrentCapacity(BigDecimal.ZERO);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        doNothing().when(warehouseRepository).delete(warehouse);

        // Act
        warehouseService.hardDeleteWarehouse(1L);

        // Assert
        verify(warehouseRepository).findById(1L);
        verify(warehouseRepository).delete(warehouse);
    }

    @Test
    @DisplayName("HardDeleteWarehouse - Should throw InvalidOperationException when warehouse has inventory")
    void hardDeleteWarehouse_HasInventory() {
        // Arrange
        warehouse.setCurrentCapacity(new BigDecimal("500.00"));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        // Act & Assert
        InvalidOperationException exception = assertThrows(
            InvalidOperationException.class,
            () -> warehouseService.hardDeleteWarehouse(1L)
        );

        assertTrue(exception.getMessage().contains("Cannot delete warehouse with existing inventory"));

        verify(warehouseRepository).findById(1L);
        verify(warehouseRepository, never()).delete(any());
    }

    @Test
    @DisplayName("HardDeleteWarehouse - Should throw ResourceNotFoundException when not found")
    void hardDeleteWarehouse_NotFound() {
        // Arrange
        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> warehouseService.hardDeleteWarehouse(999L)
        );

        assertTrue(exception.getMessage().contains("Warehouse"));

        verify(warehouseRepository).findById(999L);
        verify(warehouseRepository, never()).delete(any());
    }
}
