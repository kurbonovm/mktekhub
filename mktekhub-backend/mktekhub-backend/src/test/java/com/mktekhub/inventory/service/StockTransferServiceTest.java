package com.mktekhub.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mktekhub.inventory.dto.BulkStockTransferRequest;
import com.mktekhub.inventory.dto.BulkStockTransferResponse;
import com.mktekhub.inventory.dto.StockTransferRequest;
import com.mktekhub.inventory.dto.StockTransferResponse;
import com.mktekhub.inventory.exception.InsufficientStockException;
import com.mktekhub.inventory.exception.InvalidOperationException;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.model.*;
import com.mktekhub.inventory.repository.*;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class StockTransferServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockActivityRepository stockActivityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetailsImpl userDetails;

    @InjectMocks
    private StockTransferService stockTransferService;

    private Warehouse sourceWarehouse;
    private Warehouse destinationWarehouse;
    private InventoryItem sourceItem;
    private InventoryItem destinationItem;
    private User currentUser;
    private StockTransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        // Setup warehouses
        sourceWarehouse = new Warehouse();
        sourceWarehouse.setId(1L);
        sourceWarehouse.setName("Source Warehouse");
        sourceWarehouse.setIsActive(true);
        sourceWarehouse.setMaxCapacity(new BigDecimal("1000.00"));
        sourceWarehouse.setCurrentCapacity(new BigDecimal("500.00"));

        destinationWarehouse = new Warehouse();
        destinationWarehouse.setId(2L);
        destinationWarehouse.setName("Destination Warehouse");
        destinationWarehouse.setIsActive(true);
        destinationWarehouse.setMaxCapacity(new BigDecimal("1000.00"));
        destinationWarehouse.setCurrentCapacity(new BigDecimal("200.00"));

        // Setup items
        sourceItem = new InventoryItem();
        sourceItem.setId(1L);
        sourceItem.setSku("TEST-001");
        sourceItem.setName("Test Item");
        sourceItem.setQuantity(100);
        sourceItem.setVolumePerUnit(new BigDecimal("5.00"));
        sourceItem.setWarehouse(sourceWarehouse);

        destinationItem = new InventoryItem();
        destinationItem.setId(2L);
        destinationItem.setSku("TEST-001");
        destinationItem.setQuantity(50);
        destinationItem.setWarehouse(destinationWarehouse);

        // Setup user
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");

        // Setup request
        transferRequest = new StockTransferRequest();
        transferRequest.setItemSku("TEST-001");
        transferRequest.setSourceWarehouseId(1L);
        transferRequest.setDestinationWarehouseId(2L);
        transferRequest.setQuantity(20);
    }

    @Test
    void testTransferStock_ValidTransfer_Success() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destinationWarehouse));
        when(inventoryItemRepository.findBySkuAndWarehouseId("TEST-001", 1L)).thenReturn(sourceItem);
        when(inventoryItemRepository.findBySkuAndWarehouseId("TEST-001", 2L)).thenReturn(destinationItem);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(sourceWarehouse, destinationWarehouse);
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(sourceItem, destinationItem);
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StockTransferResponse result = stockTransferService.transferStock(transferRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getItem());
        verify(warehouseRepository, times(2)).save(any(Warehouse.class));
        verify(inventoryItemRepository, times(2)).save(any(InventoryItem.class));
        verify(stockActivityRepository, times(1)).save(any(StockActivity.class));
        assertEquals(80, sourceItem.getQuantity()); // 100 - 20
        assertEquals(70, destinationItem.getQuantity()); // 50 + 20
    }

    @Test
    void testTransferStock_SameWarehouse_ThrowsException() {
        // Arrange
        transferRequest.setDestinationWarehouseId(1L);

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> stockTransferService.transferStock(transferRequest));
        verify(warehouseRepository, never()).findById(anyLong());
    }

    @Test
    void testTransferStock_SourceWarehouseInactive_ThrowsException() {
        // Arrange
        sourceWarehouse.setIsActive(false);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> stockTransferService.transferStock(transferRequest));
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, never()).findById(2L);
    }

    @Test
    void testTransferStock_ItemNotFound_ThrowsException() {
        // Arrange
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destinationWarehouse));
        when(inventoryItemRepository.findBySkuAndWarehouseId("TEST-001", 1L)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> stockTransferService.transferStock(transferRequest));
        verify(inventoryItemRepository, times(1)).findBySkuAndWarehouseId("TEST-001", 1L);
    }

    @Test
    void testTransferStock_InsufficientStock_ThrowsException() {
        // Arrange
        transferRequest.setQuantity(150);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destinationWarehouse));
        when(inventoryItemRepository.findBySkuAndWarehouseId("TEST-001", 1L)).thenReturn(sourceItem);

        // Act & Assert
        assertThrows(InsufficientStockException.class, () -> stockTransferService.transferStock(transferRequest));
        verify(inventoryItemRepository, times(1)).findBySkuAndWarehouseId("TEST-001", 1L);
    }

    @Test
    void testBulkTransferStock_MixedResults() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));

        BulkStockTransferRequest bulkRequest = new BulkStockTransferRequest();
        bulkRequest.setTransfers(Arrays.asList(transferRequest, transferRequest)); // Two transfers

        // Mock for successful transfers
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sourceWarehouse));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(destinationWarehouse));
        when(inventoryItemRepository.findBySkuAndWarehouseId("TEST-001", 1L)).thenReturn(sourceItem);
        when(inventoryItemRepository.findBySkuAndWarehouseId("TEST-001", 2L)).thenReturn(destinationItem);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(sourceWarehouse, destinationWarehouse);
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(sourceItem, destinationItem);
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BulkStockTransferResponse result = stockTransferService.bulkTransferStock(bulkRequest);

        // Assert
        assertEquals(2, result.getTotalTransfers());
        assertEquals(2, result.getSuccessfulTransfers());
        assertEquals(0, result.getFailedTransfers());
    }
}