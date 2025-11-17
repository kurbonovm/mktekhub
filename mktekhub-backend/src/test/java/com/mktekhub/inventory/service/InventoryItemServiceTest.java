package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.InventoryItemRequest;
import com.mktekhub.inventory.dto.InventoryItemResponse;
import com.mktekhub.inventory.exception.DuplicateResourceException;
import com.mktekhub.inventory.exception.InvalidOperationException;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.exception.WarehouseCapacityExceededException;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for InventoryItemService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryItemService Tests")
class InventoryItemServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockActivityRepository stockActivityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityLoggerService activityLoggerService;

    @InjectMocks
    private InventoryItemService inventoryItemService;

    private Warehouse warehouse;
    private InventoryItem item;
    private InventoryItemRequest itemRequest;
    private User user;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setName("Main Warehouse");
        warehouse.setMaxCapacity(new BigDecimal("10000.00"));
        warehouse.setCurrentCapacity(new BigDecimal("5000.00"));
        warehouse.setIsActive(true);

        item = new InventoryItem();
        item.setId(1L);
        item.setSku("SKU-001");
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setCategory("Electronics");
        item.setQuantity(100);
        item.setUnitPrice(new BigDecimal("50.00"));
        item.setVolumePerUnit(new BigDecimal("10.00"));
        item.setReorderLevel(20);
        item.setWarehouse(warehouse);

        itemRequest = new InventoryItemRequest();
        itemRequest.setSku("SKU-001");
        itemRequest.setName("Test Item");
        itemRequest.setDescription("Test Description");
        itemRequest.setCategory("Electronics");
        itemRequest.setQuantity(100);
        itemRequest.setUnitPrice(new BigDecimal("50.00"));
        itemRequest.setVolumePerUnit(new BigDecimal("10.00"));
        itemRequest.setReorderLevel(20);
        itemRequest.setWarehouseId(1L);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    // ==================== GET ALL ITEMS TESTS ====================

    @Test
    @DisplayName("GetAllItems - Should return all inventory items")
    void getAllItems_Success() {
        // Arrange
        when(inventoryItemRepository.findAll()).thenReturn(Arrays.asList(item));

        // Act
        List<InventoryItemResponse> result = inventoryItemService.getAllItems();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventoryItemRepository).findAll();
    }

    // ==================== GET ITEM BY ID TESTS ====================

    @Test
    @DisplayName("GetItemById - Should return item when found")
    void getItemById_Success() {
        // Arrange
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act
        InventoryItemResponse result = inventoryItemService.getItemById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("SKU-001", result.getSku());
        verify(inventoryItemRepository).findById(1L);
    }

    @Test
    @DisplayName("GetItemById - Should throw exception when not found")
    void getItemById_NotFound() {
        // Arrange
        when(inventoryItemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> inventoryItemService.getItemById(999L));
    }

    // ==================== GET ITEM BY SKU TESTS ====================

    @Test
    @DisplayName("GetItemBySku - Should return item when found")
    void getItemBySku_Success() {
        // Arrange
        when(inventoryItemRepository.findBySku("SKU-001")).thenReturn(Optional.of(item));

        // Act
        InventoryItemResponse result = inventoryItemService.getItemBySku("SKU-001");

        // Assert
        assertNotNull(result);
        assertEquals("SKU-001", result.getSku());
    }

    // ==================== GET ITEMS BY WAREHOUSE TESTS ====================

    @Test
    @DisplayName("GetItemsByWarehouse - Should return items for warehouse")
    void getItemsByWarehouse_Success() {
        // Arrange
        when(inventoryItemRepository.findByWarehouseId(1L)).thenReturn(Arrays.asList(item));

        // Act
        List<InventoryItemResponse> result = inventoryItemService.getItemsByWarehouse(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== GET LOW STOCK ITEMS TESTS ====================

    @Test
    @DisplayName("GetLowStockItems - Should return items below reorder level")
    void getLowStockItems_Success() {
        // Arrange
        when(inventoryItemRepository.findLowStockItems()).thenReturn(Arrays.asList(item));

        // Act
        List<InventoryItemResponse> result = inventoryItemService.getLowStockItems();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== GET EXPIRED ITEMS TESTS ====================

    @Test
    @DisplayName("GetExpiredItems - Should return expired items")
    void getExpiredItems_Success() {
        // Arrange
        when(inventoryItemRepository.findByExpirationDateBefore(any(LocalDate.class)))
            .thenReturn(Arrays.asList(item));

        // Act
        List<InventoryItemResponse> result = inventoryItemService.getExpiredItems();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== CREATE ITEM TESTS ====================

    @Test
    @DisplayName("CreateItem - Should create item successfully")
    void createItem_Success() {
        // Arrange
        when(inventoryItemRepository.existsBySku("SKU-001")).thenReturn(false);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(item);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        // Act
        InventoryItemResponse result = inventoryItemService.createItem(itemRequest);

        // Assert
        assertNotNull(result);
        verify(inventoryItemRepository).existsBySku("SKU-001");
        verify(warehouseRepository).findById(1L);
        verify(inventoryItemRepository).save(any(InventoryItem.class));
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    @DisplayName("CreateItem - Should throw exception for duplicate SKU")
    void createItem_DuplicateSKU() {
        // Arrange
        when(inventoryItemRepository.existsBySku("SKU-001")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
            () -> inventoryItemService.createItem(itemRequest));
    }

    @Test
    @DisplayName("CreateItem - Should throw exception when warehouse not found")
    void createItem_WarehouseNotFound() {
        // Arrange
        when(inventoryItemRepository.existsBySku("SKU-001")).thenReturn(false);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
            () -> inventoryItemService.createItem(itemRequest));
    }

    @Test
    @DisplayName("CreateItem - Should throw exception when capacity exceeded")
    void createItem_CapacityExceeded() {
        // Arrange
        warehouse.setCurrentCapacity(new BigDecimal("9900.00"));
        itemRequest.setQuantity(200); // 200 * 10 = 2000 volume

        when(inventoryItemRepository.existsBySku("SKU-001")).thenReturn(false);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        // Act & Assert
        assertThrows(WarehouseCapacityExceededException.class,
            () -> inventoryItemService.createItem(itemRequest));
    }

    // ==================== DELETE ITEM TESTS ====================

    @Test
    @DisplayName("DeleteItem - Should delete item and update warehouse capacity")
    void deleteItem_Success() {
        // Arrange
        item.setVolumePerUnit(new BigDecimal("10.00"));
        item.setQuantity(100);
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        // Act
        inventoryItemService.deleteItem(1L);

        // Assert
        verify(inventoryItemRepository).findById(1L);
        verify(warehouseRepository).save(any(Warehouse.class));
        verify(inventoryItemRepository).delete(item);
    }

    // ==================== ADJUST QUANTITY TESTS ====================

    @Test
    @DisplayName("AdjustQuantity - Should increase quantity successfully")
    void adjustQuantity_Increase() {
        // Arrange
        mockSecurityContext();
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(item);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(activityLoggerService.logAdjustment(any(), anyInt(), anyInt(), anyInt(), any()))
            .thenReturn(new StockActivity());

        // Act
        InventoryItemResponse result = inventoryItemService.adjustQuantity(1L, 50);

        // Assert
        assertNotNull(result);
        verify(inventoryItemRepository).save(any(InventoryItem.class));
    }

    @Test
    @DisplayName("AdjustQuantity - Should throw exception for negative result")
    void adjustQuantity_NegativeResult() {
        // Arrange
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act & Assert
        assertThrows(InvalidOperationException.class,
            () -> inventoryItemService.adjustQuantity(1L, -150));
    }

    @Test
    @DisplayName("AdjustQuantity - Should throw exception when capacity exceeded")
    void adjustQuantity_CapacityExceeded() {
        // Arrange
        warehouse.setCurrentCapacity(new BigDecimal("9900.00"));
        item.setVolumePerUnit(new BigDecimal("10.00"));
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act & Assert
        assertThrows(WarehouseCapacityExceededException.class,
            () -> inventoryItemService.adjustQuantity(1L, 200)); // Would add 2000 volume
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
