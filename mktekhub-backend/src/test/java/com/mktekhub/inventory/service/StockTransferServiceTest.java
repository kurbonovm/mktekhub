package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.BulkStockTransferRequest;
import com.mktekhub.inventory.dto.BulkStockTransferResponse;
import com.mktekhub.inventory.dto.StockTransferRequest;
import com.mktekhub.inventory.dto.StockTransferResponse;
import com.mktekhub.inventory.exception.InsufficientStockException;
import com.mktekhub.inventory.exception.InvalidOperationException;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.model.*;
import com.mktekhub.inventory.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for StockTransferService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StockTransferService Tests")
class StockTransferServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockActivityRepository stockActivityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StockTransferService stockTransferService;

    private Warehouse sourceWarehouse;
    private Warehouse destinationWarehouse;
    private InventoryItem sourceItem;
    private InventoryItem destinationItem;
    private StockTransferRequest transferRequest;
    private User user;

    @BeforeEach
    void setUp() {
        sourceWarehouse = new Warehouse();
        sourceWarehouse.setId(1L);
        sourceWarehouse.setName("Source Warehouse");
        sourceWarehouse.setMaxCapacity(new BigDecimal("10000.00"));
        sourceWarehouse.setCurrentCapacity(new BigDecimal("5000.00"));
        sourceWarehouse.setIsActive(true);

        destinationWarehouse = new Warehouse();
        destinationWarehouse.setId(2L);
        destinationWarehouse.setName("Destination Warehouse");
        destinationWarehouse.setMaxCapacity(new BigDecimal("10000.00"));
        destinationWarehouse.setCurrentCapacity(new BigDecimal("3000.00"));
        destinationWarehouse.setIsActive(true);

        sourceItem = new InventoryItem();
        sourceItem.setId(1L);
        sourceItem.setSku("SKU-001");
        sourceItem.setName("Test Item");
        sourceItem.setQuantity(100);
        sourceItem.setVolumePerUnit(new BigDecimal("10.00"));
        sourceItem.setWarehouse(sourceWarehouse);

        transferRequest = new StockTransferRequest();
        transferRequest.setItemSku("SKU-001");
        transferRequest.setSourceWarehouseId(1L);
        transferRequest.setDestinationWarehouseId(2L);
        transferRequest.setQuantity(50);
        transferRequest.setNotes("Test transfer");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    // ==================== TRANSFER STOCK TESTS ====================

    @Test
    @DisplayName("TransferStock - Should transfer stock successfully when destination item doesn't exist")
    void transferStock_NewDestinationItem() {
        // Arrange
        mockSecurityContext();
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destinationWarehouse));
        when(inventoryItemRepository.findBySkuAndWarehouseId("SKU-001", 1L)).thenReturn(sourceItem);
        when(inventoryItemRepository.findBySkuAndWarehouseId("SKU-001", 2L)).thenReturn(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(sourceItem);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(sourceWarehouse);
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StockTransferResponse result = stockTransferService.transferStock(transferRequest);

        // Assert
        assertNotNull(result);
        verify(inventoryItemRepository, times(2)).save(any(InventoryItem.class));
        verify(stockActivityRepository).save(any(StockActivity.class));
    }

    @Test
    @DisplayName("TransferStock - Should transfer stock successfully when destination item exists")
    void transferStock_ExistingDestinationItem() {
        // Arrange
        mockSecurityContext();
        destinationItem = new InventoryItem();
        destinationItem.setId(2L);
        destinationItem.setSku("SKU-001");
        destinationItem.setQuantity(30);
        destinationItem.setWarehouse(destinationWarehouse);

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destinationWarehouse));
        when(inventoryItemRepository.findBySkuAndWarehouseId("SKU-001", 1L)).thenReturn(sourceItem);
        when(inventoryItemRepository.findBySkuAndWarehouseId("SKU-001", 2L)).thenReturn(destinationItem);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(sourceItem);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(sourceWarehouse);
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StockTransferResponse result = stockTransferService.transferStock(transferRequest);

        // Assert
        assertNotNull(result);
        verify(inventoryItemRepository, times(2)).save(any(InventoryItem.class));
    }

    @Test
    @DisplayName("TransferStock - Should throw exception when source and destination are same")
    void transferStock_SameWarehouse() {
        // Arrange
        transferRequest.setDestinationWarehouseId(1L);

        // Act & Assert
        InvalidOperationException exception = assertThrows(
            InvalidOperationException.class,
            () -> stockTransferService.transferStock(transferRequest)
        );
        assertTrue(exception.getMessage().contains("Source and destination warehouses must be different"));
    }

    @Test
    @DisplayName("TransferStock - Should throw exception when source warehouse not found")
    void transferStock_SourceWarehouseNotFound() {
        // Arrange
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> stockTransferService.transferStock(transferRequest));
    }

    @Test
    @DisplayName("TransferStock - Should throw exception when source warehouse is inactive")
    void transferStock_SourceWarehouseInactive() {
        // Arrange
        sourceWarehouse.setIsActive(false);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));

        // Act & Assert
        InvalidOperationException exception = assertThrows(
            InvalidOperationException.class,
            () -> stockTransferService.transferStock(transferRequest)
        );
        assertTrue(exception.getMessage().contains("not active"));
    }

    @Test
    @DisplayName("TransferStock - Should throw exception when destination warehouse is inactive")
    void transferStock_DestinationWarehouseInactive() {
        // Arrange
        destinationWarehouse.setIsActive(false);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destinationWarehouse));

        // Act & Assert
        InvalidOperationException exception = assertThrows(
            InvalidOperationException.class,
            () -> stockTransferService.transferStock(transferRequest)
        );
        assertTrue(exception.getMessage().contains("not active"));
    }

    @Test
    @DisplayName("TransferStock - Should throw exception when item not found in source warehouse")
    void transferStock_ItemNotFoundInSource() {
        // Arrange
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destinationWarehouse));
        when(inventoryItemRepository.findBySkuAndWarehouseId("SKU-001", 1L)).thenReturn(null);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> stockTransferService.transferStock(transferRequest)
        );
        assertTrue(exception.getMessage().contains("not found in source warehouse"));
    }

    @Test
    @DisplayName("TransferStock - Should throw exception for insufficient stock")
    void transferStock_InsufficientStock() {
        // Arrange
        sourceItem.setQuantity(20);
        transferRequest.setQuantity(50);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destinationWarehouse));
        when(inventoryItemRepository.findBySkuAndWarehouseId("SKU-001", 1L)).thenReturn(sourceItem);

        // Act & Assert
        assertThrows(InsufficientStockException.class,
            () -> stockTransferService.transferStock(transferRequest));
    }

    // ==================== BULK TRANSFER TESTS ====================

    @Test
    @DisplayName("BulkTransferStock - Should process multiple transfers successfully")
    void bulkTransferStock_AllSuccess() {
        // Arrange
        mockSecurityContext();
        BulkStockTransferRequest bulkRequest = new BulkStockTransferRequest();
        bulkRequest.setTransfers(Arrays.asList(transferRequest));

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destinationWarehouse));
        when(inventoryItemRepository.findBySkuAndWarehouseId("SKU-001", 1L)).thenReturn(sourceItem);
        when(inventoryItemRepository.findBySkuAndWarehouseId("SKU-001", 2L)).thenReturn(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(sourceItem);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(sourceWarehouse);
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BulkStockTransferResponse result = stockTransferService.bulkTransferStock(bulkRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalTransfers());
        assertEquals(1, result.getSuccessfulTransfers());
        assertEquals(0, result.getFailedTransfers());
    }

    @Test
    @DisplayName("BulkTransferStock - Should handle mixed success and failure")
    void bulkTransferStock_MixedResults() {
        // Arrange
        StockTransferRequest failingRequest = new StockTransferRequest();
        failingRequest.setItemSku("SKU-002");
        failingRequest.setSourceWarehouseId(1L);
        failingRequest.setDestinationWarehouseId(2L);
        failingRequest.setQuantity(100);

        BulkStockTransferRequest bulkRequest = new BulkStockTransferRequest();
        bulkRequest.setTransfers(Arrays.asList(transferRequest, failingRequest));

        mockSecurityContext();
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destinationWarehouse));
        when(inventoryItemRepository.findBySkuAndWarehouseId("SKU-001", 1L)).thenReturn(sourceItem);
        when(inventoryItemRepository.findBySkuAndWarehouseId("SKU-001", 2L)).thenReturn(null);
        when(inventoryItemRepository.findBySkuAndWarehouseId("SKU-002", 1L)).thenReturn(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(sourceItem);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(sourceWarehouse);
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BulkStockTransferResponse result = stockTransferService.bulkTransferStock(bulkRequest);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalTransfers());
        assertEquals(1, result.getSuccessfulTransfers());
        assertEquals(1, result.getFailedTransfers());
        assertEquals(1, result.getErrors().size());
    }

    // ==================== HELPER METHODS ====================

    private void mockSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
    }
}
