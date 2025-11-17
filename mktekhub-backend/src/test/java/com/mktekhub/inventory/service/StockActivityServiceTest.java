package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.StockActivityResponse;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.model.ActivityType;
import com.mktekhub.inventory.model.InventoryItem;
import com.mktekhub.inventory.model.StockActivity;
import com.mktekhub.inventory.model.User;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.InventoryItemRepository;
import com.mktekhub.inventory.repository.StockActivityRepository;
import com.mktekhub.inventory.repository.UserRepository;
import com.mktekhub.inventory.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockActivityServiceTest {

    @Mock
    private StockActivityRepository stockActivityRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StockActivityService stockActivityService;

    private StockActivity activity1;
    private StockActivity activity2;
    private InventoryItem item;
    private User user;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setName("Main Warehouse");

        item = new InventoryItem();
        item.setId(1L);
        item.setSku("SKU001");
        item.setName("Test Item");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        activity1 = new StockActivity();
        activity1.setId(1L);
        activity1.setItem(item);
        activity1.setItemSku("SKU001");
        activity1.setActivityType(ActivityType.ADJUSTMENT);
        activity1.setQuantityChange(10);
        activity1.setPreviousQuantity(90);
        activity1.setNewQuantity(100);
        activity1.setPerformedBy(user);
        activity1.setTimestamp(LocalDateTime.now());

        activity2 = new StockActivity();
        activity2.setId(2L);
        activity2.setItem(item);
        activity2.setItemSku("SKU001");
        activity2.setActivityType(ActivityType.TRANSFER);
        activity2.setQuantityChange(20);
        activity2.setPreviousQuantity(100);
        activity2.setNewQuantity(120);
        activity2.setPerformedBy(user);
        activity2.setTimestamp(LocalDateTime.now());
    }

    @Test
    void getAllActivities_ReturnsAllActivities() {
        // Arrange
        when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity1, activity2));

        // Act
        List<StockActivityResponse> result = stockActivityService.getAllActivities();

        // Assert
        assertEquals(2, result.size());
        verify(stockActivityRepository).findAll();
    }

    @Test
    void getActivityById_ReturnsActivity_WhenExists() {
        // Arrange
        when(stockActivityRepository.findById(1L)).thenReturn(Optional.of(activity1));

        // Act
        StockActivityResponse result = stockActivityService.getActivityById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("SKU001", result.getItemSku());
        verify(stockActivityRepository).findById(1L);
    }

    @Test
    void getActivityById_ThrowsException_WhenNotFound() {
        // Arrange
        when(stockActivityRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> stockActivityService.getActivityById(999L));
    }

    @Test
    void getActivitiesByItemId_ReturnsActivities_WhenItemExists() {
        // Arrange
        when(inventoryItemRepository.existsById(1L)).thenReturn(true);
        when(stockActivityRepository.findByItemId(1L)).thenReturn(Arrays.asList(activity1, activity2));

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByItemId(1L);

        // Assert
        assertEquals(2, result.size());
        verify(inventoryItemRepository).existsById(1L);
        verify(stockActivityRepository).findByItemId(1L);
    }

    @Test
    void getActivitiesByItemId_ThrowsException_WhenItemNotExists() {
        // Arrange
        when(inventoryItemRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> stockActivityService.getActivitiesByItemId(999L));
        verify(stockActivityRepository, never()).findByItemId(anyLong());
    }

    @Test
    void getActivitiesByItemIdSorted_ReturnsSortedActivities() {
        // Arrange
        when(inventoryItemRepository.existsById(1L)).thenReturn(true);
        when(stockActivityRepository.findByItemIdOrderByTimestampDesc(1L))
                .thenReturn(Arrays.asList(activity2, activity1));

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByItemIdSorted(1L);

        // Assert
        assertEquals(2, result.size());
        verify(stockActivityRepository).findByItemIdOrderByTimestampDesc(1L);
    }

    @Test
    void getActivitiesByItemSku_ReturnsActivities_WhenSkuExists() {
        // Arrange
        when(inventoryItemRepository.existsBySku("SKU001")).thenReturn(true);
        when(inventoryItemRepository.findBySku("SKU001")).thenReturn(Optional.of(item));
        when(inventoryItemRepository.existsById(1L)).thenReturn(true);
        when(stockActivityRepository.findByItemIdOrderByTimestampDesc(1L))
                .thenReturn(Arrays.asList(activity1, activity2));

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByItemSku("SKU001");

        // Assert
        assertEquals(2, result.size());
        verify(inventoryItemRepository).existsBySku("SKU001");
        verify(inventoryItemRepository).findBySku("SKU001");
    }

    @Test
    void getActivitiesByItemSku_ThrowsException_WhenSkuNotExists() {
        // Arrange
        when(inventoryItemRepository.existsBySku("INVALID")).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> stockActivityService.getActivitiesByItemSku("INVALID"));
    }

    @Test
    void getActivitiesByType_ReturnsFilteredActivities() {
        // Arrange
        when(stockActivityRepository.findByActivityType(ActivityType.ADJUSTMENT))
                .thenReturn(List.of(activity1));

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByType(ActivityType.ADJUSTMENT);

        // Assert
        assertEquals(1, result.size());
        verify(stockActivityRepository).findByActivityType(ActivityType.ADJUSTMENT);
    }

    @Test
    void getActivitiesByUserId_ReturnsActivities_WhenUserExists() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        when(stockActivityRepository.findByPerformedById(1L)).thenReturn(Arrays.asList(activity1, activity2));

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByUserId(1L);

        // Assert
        assertEquals(2, result.size());
        verify(userRepository).existsById(1L);
        verify(stockActivityRepository).findByPerformedById(1L);
    }

    @Test
    void getActivitiesByUserId_ThrowsException_WhenUserNotExists() {
        // Arrange
        when(userRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> stockActivityService.getActivitiesByUserId(999L));
    }

    @Test
    void getActivitiesByUsername_ReturnsActivities_WhenUserExists() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(stockActivityRepository.findByPerformedById(1L)).thenReturn(Arrays.asList(activity1, activity2));

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByUsername("testuser");

        // Assert
        assertEquals(2, result.size());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getActivitiesBySourceWarehouse_ReturnsActivities_WhenWarehouseExists() {
        // Arrange
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(stockActivityRepository.findBySourceWarehouseId(1L)).thenReturn(List.of(activity1));

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesBySourceWarehouse(1L);

        // Assert
        assertEquals(1, result.size());
        verify(warehouseRepository).existsById(1L);
        verify(stockActivityRepository).findBySourceWarehouseId(1L);
    }

    @Test
    void getActivitiesByDestinationWarehouse_ReturnsActivities_WhenWarehouseExists() {
        // Arrange
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(stockActivityRepository.findByDestinationWarehouseId(1L)).thenReturn(List.of(activity2));

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByDestinationWarehouse(1L);

        // Assert
        assertEquals(1, result.size());
        verify(warehouseRepository).existsById(1L);
        verify(stockActivityRepository).findByDestinationWarehouseId(1L);
    }

    @Test
    void getActivitiesByWarehouse_ReturnsCombinedActivities() {
        // Arrange
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        when(stockActivityRepository.findBySourceWarehouseId(1L)).thenReturn(new java.util.ArrayList<>(List.of(activity1)));
        when(stockActivityRepository.findByDestinationWarehouseId(1L)).thenReturn(new java.util.ArrayList<>(List.of(activity2)));

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByWarehouse(1L);

        // Assert
        assertEquals(2, result.size());
        verify(stockActivityRepository).findBySourceWarehouseId(1L);
        verify(stockActivityRepository).findByDestinationWarehouseId(1L);
    }

    @Test
    void getActivitiesByDateRange_ReturnsFilteredActivities() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        when(stockActivityRepository.findByTimestampBetween(start, end))
                .thenReturn(Arrays.asList(activity1, activity2));

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByDateRange(start, end);

        // Assert
        assertEquals(2, result.size());
        verify(stockActivityRepository).findByTimestampBetween(start, end);
    }

    @Test
    void getActivitiesWithFilters_FiltersCorrectly_WithMultipleFilters() {
        // Arrange
        when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity1, activity2));
        when(inventoryItemRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesWithFilters(
                1L, null, ActivityType.ADJUSTMENT, 1L, null, null, null, null
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals(ActivityType.ADJUSTMENT, result.get(0).getActivityType());
    }

    @Test
    void getActivitiesWithFilters_ReturnsAllActivities_WhenNoFiltersProvided() {
        // Arrange
        when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity1, activity2));

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesWithFilters(
                null, null, null, null, null, null, null, null
        );

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void getActivitiesWithFilters_ThrowsException_WhenItemIdNotExists() {
        // Arrange
        when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity1, activity2));
        when(inventoryItemRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                stockActivityService.getActivitiesWithFilters(999L, null, null, null, null, null, null, null)
        );
    }
}
