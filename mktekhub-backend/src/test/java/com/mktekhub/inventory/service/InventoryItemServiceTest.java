package com.mktekhub.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mktekhub.inventory.dto.InventoryItemRequest;
import com.mktekhub.inventory.dto.InventoryItemResponse;
import com.mktekhub.inventory.dto.WarehouseResponse;
import com.mktekhub.inventory.exception.DuplicateResourceException;
import com.mktekhub.inventory.exception.InvalidOperationException;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.model.InventoryItem;
import com.mktekhub.inventory.model.User;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.InventoryItemRepository;
import com.mktekhub.inventory.repository.StockActivityRepository;
import com.mktekhub.inventory.repository.UserRepository;
import com.mktekhub.inventory.repository.WarehouseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
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
class InventoryItemServiceTest {

    @Mock
    private InventoryItemRepository itemRepository;

    @Mock
    private WarehouseService warehouseService; 

    @Mock
    private ActivityLoggerService activityLoggerService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockActivityRepository stockActivityRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private InventoryItemService inventoryItemService;

    private User testUser;
    private Warehouse testWarehouse;
    private WarehouseResponse testWarehouseResponse;
    private InventoryItem testItem;
    private InventoryItemRequest testRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // Setup test warehouse
        testWarehouse = new Warehouse();
        testWarehouse.setId(1L);
        testWarehouse.setName("Test Warehouse");
        testWarehouse.setLocation("Test Location");
        testWarehouse.setMaxCapacity(new BigDecimal("1000.00"));
        testWarehouse.setCurrentCapacity(new BigDecimal("400.00")); 

        // Setup warehouse response 
        testWarehouseResponse = new WarehouseResponse();
        testWarehouseResponse.setId(1L);
        testWarehouseResponse.setName("Test Warehouse");
        testWarehouseResponse.setLocation("Test Location");
        testWarehouseResponse.setMaxCapacity(new BigDecimal("1000.00"));
        testWarehouseResponse.setCurrentCapacity(new BigDecimal("400.00"));

        // Setup test item
        testItem = new InventoryItem();
        testItem.setId(1L);
        testItem.setSku("TEST-001");
        testItem.setName("Test Item");
        testItem.setDescription("Test Description");
        testItem.setCategory("Electronics");
        testItem.setBrand("TestBrand");
        testItem.setQuantity(100);
        testItem.setUnitPrice(new BigDecimal("50.00"));
        testItem.setVolumePerUnit(new BigDecimal("5.00")); // 100 * 5.00 = 500.00 volume
        testItem.setReorderLevel(10);
        testItem.setWarehouse(testWarehouse);
        testItem.setExpirationDate(LocalDate.now().plusMonths(6));
        testItem.setWarrantyEndDate(LocalDate.now().plusYears(1));

        // Setup test request
        testRequest = new InventoryItemRequest();
        testRequest.setSku("TEST-002");
        testRequest.setName("New Test Item");
        testRequest.setDescription("New Description");
        testRequest.setCategory("Electronics");
        testRequest.setBrand("TestBrand");
        testRequest.setQuantity(100);
        testRequest.setUnitPrice(new BigDecimal("75.00"));
        testRequest.setVolumePerUnit(new BigDecimal("5.00")); // 100 * 5.00 = 500.00 volume
        testRequest.setReorderLevel(5);
        testRequest.setWarehouseId(1L);
        testRequest.setExpirationDate(LocalDate.now().plusMonths(12));
        testRequest.setWarrantyEndDate(LocalDate.now().plusYears(2));
    }

    @Test
    void testGetAllItems_ReturnsAllItems() {
        // Arrange
        List<InventoryItem> items = Arrays.asList(testItem);
        when(itemRepository.findAll()).thenReturn(items);

        // Act
        List<InventoryItemResponse> result = inventoryItemService.getAllItems();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TEST-001", result.get(0).getSku());
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void testGetItemById_ItemExists_ReturnsItem() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // Act
        InventoryItemResponse result = inventoryItemService.getItemById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("TEST-001", result.getSku());
        assertEquals("Test Item", result.getName());
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    void testGetItemById_ItemNotFound_ThrowsException() {
        // Arrange
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> inventoryItemService.getItemById(999L));
        verify(itemRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateItem_ValidRequest_CreatesItem() {
        // Arrange
        when(itemRepository.existsBySku("TEST-002")).thenReturn(false);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(itemRepository.save(any(InventoryItem.class))).thenReturn(testItem);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);

        // Act
        InventoryItemResponse result = inventoryItemService.createItem(testRequest);

        // Assert
        assertNotNull(result);
        verify(itemRepository, times(1)).existsBySku("TEST-002");
        verify(warehouseRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).save(any(InventoryItem.class));
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
        verifyNoInteractions(stockActivityRepository);
        verifyNoInteractions(warehouseService);
        verifyNoInteractions(userRepository, securityContext, authentication);
    }

    @Test
    void testCreateItem_DuplicateSku_ThrowsException() {
        // Arrange
        when(itemRepository.existsBySku("TEST-002")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> inventoryItemService.createItem(testRequest));
        verify(itemRepository, times(1)).existsBySku("TEST-002");
        verify(itemRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    void testUpdateItem_ValidRequest_UpdatesItem() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.existsBySku("TEST-002")).thenReturn(false);
        when(itemRepository.save(any(InventoryItem.class))).thenReturn(testItem);
        when(stockActivityRepository.save(any())).thenReturn(null);

        // Act
        InventoryItemResponse result = inventoryItemService.updateItem(1L, testRequest);

        // Assert
        assertNotNull(result);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).existsBySku("TEST-002");
        verify(itemRepository, times(1)).save(any(InventoryItem.class));
        verify(stockActivityRepository, times(1)).save(any());
        verifyNoInteractions(warehouseRepository); // No warehouse save or findById
        verifyNoInteractions(warehouseService);
    }

    @Test
    void testUpdateItem_ItemNotFound_ThrowsException() {
        // Arrange
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> inventoryItemService.updateItem(999L, testRequest));
        verify(itemRepository, times(1)).findById(999L);
        verify(itemRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    void testAdjustQuantity_IncreaseQuantity_UpdatesSuccessfully() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(InventoryItem.class))).thenReturn(testItem);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);

        // Act
        InventoryItemResponse result = inventoryItemService.adjustQuantity(1L, 50);

        // Assert
        assertNotNull(result);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).save(any(InventoryItem.class));
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
        verify(activityLoggerService, times(1))
                .logAdjustment(any(InventoryItem.class), eq(50), eq(100), anyInt(), eq(testUser));
        verifyNoInteractions(stockActivityRepository);
    }

    @Test
    void testAdjustQuantity_DecreaseQuantity_UpdatesSuccessfully() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(InventoryItem.class))).thenReturn(testItem);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);

        // Act
        InventoryItemResponse result = inventoryItemService.adjustQuantity(1L, -30);

        // Assert
        assertNotNull(result);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).save(any(InventoryItem.class));
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
        verify(activityLoggerService, times(1))
                .logAdjustment(any(InventoryItem.class), eq(-30), eq(100), anyInt(), eq(testUser));
        verifyNoInteractions(stockActivityRepository);
    }

    @Test
    void testAdjustQuantity_InsufficientStock_ThrowsException() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> inventoryItemService.adjustQuantity(1L, -150));
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    void testDeleteItem_ItemExists_DeletesSuccessfully() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);
        doNothing().when(itemRepository).delete(any(InventoryItem.class));

        // Act
        inventoryItemService.deleteItem(1L);

        // Assert
        verify(itemRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
        verify(itemRepository, times(1)).delete(testItem);
    }

    @Test
    void testDeleteItem_ItemNotFound_ThrowsException() {
        // Arrange
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> inventoryItemService.deleteItem(999L));
        verify(itemRepository, times(1)).findById(999L);
        verify(itemRepository, never()).delete(any(InventoryItem.class));
    }

    @Test
    void testGetItemBySku_ItemExists_ReturnsItem() {
        // Arrange
        when(itemRepository.findBySku("TEST-001")).thenReturn(Optional.of(testItem));

        // Act
        InventoryItemResponse result = inventoryItemService.getItemBySku("TEST-001");

        // Assert
        assertNotNull(result);
        assertEquals("TEST-001", result.getSku());
        verify(itemRepository, times(1)).findBySku("TEST-001");
    }

    @Test
    void testGetItemBySku_ItemNotFound_ThrowsException() {
        // Arrange
        when(itemRepository.findBySku("INVALID-SKU")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> inventoryItemService.getItemBySku("INVALID-SKU"));
        verify(itemRepository, times(1)).findBySku("INVALID-SKU");
    }
}