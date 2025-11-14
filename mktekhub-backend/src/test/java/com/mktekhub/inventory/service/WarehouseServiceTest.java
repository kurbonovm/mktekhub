package com.mktekhub.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mktekhub.inventory.dto.WarehouseRequest;
import com.mktekhub.inventory.dto.WarehouseResponse;
import com.mktekhub.inventory.exception.DuplicateResourceException;
import com.mktekhub.inventory.exception.InvalidOperationException;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.WarehouseRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    private Warehouse testWarehouse;
    private WarehouseRequest testRequest;

    @BeforeEach
    void setUp() {
        // Setup test warehouse
        testWarehouse = new Warehouse();
        testWarehouse.setId(1L);
        testWarehouse.setName("Test Warehouse");
        testWarehouse.setLocation("Test Location");
        testWarehouse.setMaxCapacity(new BigDecimal("1000.00"));
        testWarehouse.setCurrentCapacity(new BigDecimal("200.00"));
        testWarehouse.setCapacityAlertThreshold(new BigDecimal("80.00"));
        testWarehouse.setIsActive(true);

        // Setup test request
        testRequest = new WarehouseRequest();
        testRequest.setName("New Warehouse");
        testRequest.setLocation("New Location");
        testRequest.setMaxCapacity(new BigDecimal("1500.00"));
        testRequest.setCapacityAlertThreshold(new BigDecimal("85.00"));
    }

    @Test
    void testGetAllWarehouses_ReturnsAllWarehouses() {
        // Arrange
        List<Warehouse> warehouses = Arrays.asList(testWarehouse);
        when(warehouseRepository.findAll()).thenReturn(warehouses);

        // Act
        List<WarehouseResponse> result = warehouseService.getAllWarehouses();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Warehouse", result.get(0).getName());
        verify(warehouseRepository, times(1)).findAll();
    }

    @Test
    void testGetActiveWarehouses_ReturnsActiveWarehouses() {
        // Arrange
        List<Warehouse> activeWarehouses = Arrays.asList(testWarehouse);
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(activeWarehouses);

        // Act
        List<WarehouseResponse> result = warehouseService.getActiveWarehouses();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Warehouse", result.get(0).getName());
        verify(warehouseRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testGetWarehouseById_WarehouseExists_ReturnsWarehouse() {
        // Arrange
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));

        // Act
        WarehouseResponse result = warehouseService.getWarehouseById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Warehouse", result.getName());
        assertEquals("Test Location", result.getLocation());
        verify(warehouseRepository, times(1)).findById(1L);
    }

    @Test
    void testGetWarehouseById_WarehouseNotFound_ThrowsException() {
        // Arrange
        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> warehouseService.getWarehouseById(999L));
        verify(warehouseRepository, times(1)).findById(999L);
    }

    @Test
    void testGetWarehousesWithAlerts_ReturnsWarehousesWithAlerts() {
        // Arrange
        List<Warehouse> alertWarehouses = Arrays.asList(testWarehouse);
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(alertWarehouses);

        // Act
        List<WarehouseResponse> result = warehouseService.getWarehousesWithAlerts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(warehouseRepository, times(1)).findWarehousesWithCapacityAlert();
    }

    @Test
    void testCreateWarehouse_ValidRequest_CreatesWarehouse() {
        // Arrange
        when(warehouseRepository.existsByName("New Warehouse")).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);

        // Act
        WarehouseResponse result = warehouseService.createWarehouse(testRequest);

        // Assert
        assertNotNull(result);
        verify(warehouseRepository, times(1)).existsByName("New Warehouse");
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    void testCreateWarehouse_DuplicateName_ThrowsException() {
        // Arrange
        when(warehouseRepository.existsByName("New Warehouse")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> warehouseService.createWarehouse(testRequest));
        verify(warehouseRepository, times(1)).existsByName("New Warehouse");
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void testUpdateWarehouse_ValidRequest_UpdatesWarehouse() {
        // Arrange
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(warehouseRepository.existsByName("New Warehouse")).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);

        // Act
        WarehouseResponse result = warehouseService.updateWarehouse(1L, testRequest);

        // Assert
        assertNotNull(result);
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).existsByName("New Warehouse");
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    void testUpdateWarehouse_WarehouseNotFound_ThrowsException() {
        // Arrange
        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> warehouseService.updateWarehouse(999L, testRequest));
        verify(warehouseRepository, times(1)).findById(999L);
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void testUpdateWarehouse_DuplicateName_ThrowsException() {
        // Arrange
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(warehouseRepository.existsByName("New Warehouse")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> warehouseService.updateWarehouse(1L, testRequest));
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).existsByName("New Warehouse");
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void testDeleteWarehouse_ValidRequest_DeletesWarehouse() {
        // Arrange
        testWarehouse.setCurrentCapacity(BigDecimal.ZERO);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);

        // Act
        warehouseService.deleteWarehouse(1L);

        // Assert
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).save(testWarehouse);
        assertFalse(testWarehouse.getIsActive());
    }

    @Test
    void testDeleteWarehouse_WarehouseNotFound_ThrowsException() {
        // Arrange
        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> warehouseService.deleteWarehouse(999L));
        verify(warehouseRepository, times(1)).findById(999L);
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void testDeleteWarehouse_HasInventory_ThrowsException() {
        // Arrange
        testWarehouse.setCurrentCapacity(new BigDecimal("100.00"));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> warehouseService.deleteWarehouse(1L));
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void testHardDeleteWarehouse_ValidRequest_DeletesWarehouse() {
        // Arrange
        testWarehouse.setCurrentCapacity(BigDecimal.ZERO);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        doNothing().when(warehouseRepository).delete(any(Warehouse.class));

        // Act
        warehouseService.hardDeleteWarehouse(1L);

        // Assert
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).delete(testWarehouse);
    }

    @Test
    void testHardDeleteWarehouse_HasInventory_ThrowsException() {
        // Arrange
        testWarehouse.setCurrentCapacity(new BigDecimal("100.00"));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> warehouseService.hardDeleteWarehouse(1L));
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, never()).delete(any(Warehouse.class));
    }
}