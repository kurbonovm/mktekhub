package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.WarehouseRequest;
import com.mktekhub.inventory.dto.WarehouseResponse;
import com.mktekhub.inventory.exception.DuplicateResourceException;
import com.mktekhub.inventory.exception.InvalidOperationException;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    private Warehouse warehouse;
    private WarehouseRequest warehouseRequest;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setName("Main Warehouse");
        warehouse.setLocation("New York");
        warehouse.setMaxCapacity(new BigDecimal("10000.00"));
        warehouse.setCurrentCapacity(new BigDecimal("5000.00"));
        warehouse.setCapacityAlertThreshold(new BigDecimal("80.00"));
        warehouse.setIsActive(true);

        warehouseRequest = new WarehouseRequest();
        warehouseRequest.setName("Main Warehouse");
        warehouseRequest.setLocation("New York");
        warehouseRequest.setMaxCapacity(new BigDecimal("10000.00"));
        warehouseRequest.setCapacityAlertThreshold(new BigDecimal("80.00"));
    }

    @Test
    void getAllWarehouses_ReturnsAllWarehouses() {
        // Arrange
        Warehouse warehouse2 = new Warehouse();
        warehouse2.setId(2L);
        warehouse2.setName("Secondary Warehouse");
        warehouse2.setLocation("Boston");
        warehouse2.setMaxCapacity(new BigDecimal("5000.00"));
        warehouse2.setCurrentCapacity(BigDecimal.ZERO);
        warehouse2.setIsActive(true);

        when(warehouseRepository.findAll()).thenReturn(Arrays.asList(warehouse, warehouse2));

        // Act
        List<WarehouseResponse> result = warehouseService.getAllWarehouses();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Main Warehouse", result.get(0).getName());
        assertEquals("Secondary Warehouse", result.get(1).getName());
        verify(warehouseRepository).findAll();
    }

    @Test
    void getActiveWarehouses_ReturnsOnlyActiveWarehouses() {
        // Arrange
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(List.of(warehouse));

        // Act
        List<WarehouseResponse> result = warehouseService.getActiveWarehouses();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsActive());
        verify(warehouseRepository).findByIsActiveTrue();
    }

    @Test
    void getWarehouseById_ReturnsWarehouse_WhenExists() {
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
    void getWarehouseById_ThrowsException_WhenNotFound() {
        // Arrange
        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> warehouseService.getWarehouseById(999L));
        verify(warehouseRepository).findById(999L);
    }

    @Test
    void getWarehousesWithAlerts_ReturnsWarehousesWithCapacityAlerts() {
        // Arrange
        when(warehouseRepository.findWarehousesWithCapacityAlert()).thenReturn(List.of(warehouse));

        // Act
        List<WarehouseResponse> result = warehouseService.getWarehousesWithAlerts();

        // Assert
        assertEquals(1, result.size());
        verify(warehouseRepository).findWarehousesWithCapacityAlert();
    }

    @Test
    void createWarehouse_Success() {
        // Arrange
        when(warehouseRepository.existsByName("Main Warehouse")).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        // Act
        WarehouseResponse result = warehouseService.createWarehouse(warehouseRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Main Warehouse", result.getName());
        assertEquals("New York", result.getLocation());
        verify(warehouseRepository).existsByName("Main Warehouse");
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void createWarehouse_ThrowsException_WhenNameExists() {
        // Arrange
        when(warehouseRepository.existsByName("Main Warehouse")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> warehouseService.createWarehouse(warehouseRequest));
        verify(warehouseRepository).existsByName("Main Warehouse");
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void createWarehouse_SetsDefaultThreshold_WhenNotProvided() {
        // Arrange
        warehouseRequest.setCapacityAlertThreshold(null);
        when(warehouseRepository.existsByName(anyString())).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> {
            Warehouse saved = invocation.getArgument(0);
            assertEquals(new BigDecimal("80.00"), saved.getCapacityAlertThreshold());
            return saved;
        });

        // Act
        warehouseService.createWarehouse(warehouseRequest);

        // Assert
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void createWarehouse_InitializesCurrentCapacityToZero() {
        // Arrange
        when(warehouseRepository.existsByName(anyString())).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> {
            Warehouse saved = invocation.getArgument(0);
            assertEquals(BigDecimal.ZERO, saved.getCurrentCapacity());
            assertTrue(saved.getIsActive());
            return saved;
        });

        // Act
        warehouseService.createWarehouse(warehouseRequest);

        // Assert
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void updateWarehouse_Success() {
        // Arrange
        WarehouseRequest updateRequest = new WarehouseRequest();
        updateRequest.setName("Updated Warehouse");
        updateRequest.setLocation("Boston");
        updateRequest.setMaxCapacity(new BigDecimal("15000.00"));
        updateRequest.setCapacityAlertThreshold(new BigDecimal("75.00"));

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.existsByName("Updated Warehouse")).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        // Act
        WarehouseResponse result = warehouseService.updateWarehouse(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(warehouseRepository).findById(1L);
        verify(warehouseRepository).save(warehouse);
    }

    @Test
    void updateWarehouse_ThrowsException_WhenNotFound() {
        // Arrange
        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> warehouseService.updateWarehouse(999L, warehouseRequest));
        verify(warehouseRepository).findById(999L);
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void updateWarehouse_ThrowsException_WhenNewNameExists() {
        // Arrange
        warehouseRequest.setName("Different Name");
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.existsByName("Different Name")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> warehouseService.updateWarehouse(1L, warehouseRequest));
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void updateWarehouse_AllowsSameName() {
        // Arrange
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        // Act
        WarehouseResponse result = warehouseService.updateWarehouse(1L, warehouseRequest);

        // Assert
        assertNotNull(result);
        verify(warehouseRepository, never()).existsByName(anyString());
        verify(warehouseRepository).save(warehouse);
    }

    @Test
    void deleteWarehouse_Success_WhenEmpty() {
        // Arrange
        warehouse.setCurrentCapacity(BigDecimal.ZERO);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        // Act
        warehouseService.deleteWarehouse(1L);

        // Assert
        verify(warehouseRepository).findById(1L);
        verify(warehouseRepository).save(warehouse);
    }

    @Test
    void deleteWarehouse_ThrowsException_WhenNotFound() {
        // Arrange
        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> warehouseService.deleteWarehouse(999L));
        verify(warehouseRepository).findById(999L);
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void deleteWarehouse_ThrowsException_WhenHasInventory() {
        // Arrange
        warehouse.setCurrentCapacity(new BigDecimal("100.00"));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> warehouseService.deleteWarehouse(1L));
        verify(warehouseRepository).findById(1L);
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void deleteWarehouse_SetsIsActiveToFalse() {
        // Arrange
        warehouse.setCurrentCapacity(BigDecimal.ZERO);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> {
            Warehouse saved = invocation.getArgument(0);
            assertFalse(saved.getIsActive());
            return saved;
        });

        // Act
        warehouseService.deleteWarehouse(1L);

        // Assert
        verify(warehouseRepository).save(warehouse);
    }

    @Test
    void hardDeleteWarehouse_Success_WhenEmpty() {
        // Arrange
        warehouse.setCurrentCapacity(BigDecimal.ZERO);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        // Act
        warehouseService.hardDeleteWarehouse(1L);

        // Assert
        verify(warehouseRepository).findById(1L);
        verify(warehouseRepository).delete(warehouse);
    }

    @Test
    void hardDeleteWarehouse_ThrowsException_WhenHasInventory() {
        // Arrange
        warehouse.setCurrentCapacity(new BigDecimal("100.00"));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> warehouseService.hardDeleteWarehouse(1L));
        verify(warehouseRepository).findById(1L);
        verify(warehouseRepository, never()).delete(any(Warehouse.class));
    }

    @Test
    void hardDeleteWarehouse_ThrowsException_WhenNotFound() {
        // Arrange
        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> warehouseService.hardDeleteWarehouse(999L));
        verify(warehouseRepository).findById(999L);
        verify(warehouseRepository, never()).delete(any(Warehouse.class));
    }
}
