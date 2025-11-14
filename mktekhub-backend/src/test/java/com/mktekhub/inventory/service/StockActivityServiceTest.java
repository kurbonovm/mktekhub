package com.mktekhub.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mktekhub.inventory.dto.StockActivityResponse;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.model.ActivityType;
import com.mktekhub.inventory.model.InventoryItem;
import com.mktekhub.inventory.model.StockActivity;
import com.mktekhub.inventory.model.User;
import com.mktekhub.inventory.repository.InventoryItemRepository;
import com.mktekhub.inventory.repository.StockActivityRepository;
import com.mktekhub.inventory.repository.UserRepository;
import com.mktekhub.inventory.model.Warehouse;
import com.mktekhub.inventory.repository.WarehouseRepository;

import java.time.LocalDateTime;
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

    private StockActivity testActivity;
    private InventoryItem testItem;
    private User testUser;
    private Warehouse testWarehouse;

    @BeforeEach
    void setUp() {
        // Setup test item
        testItem = new InventoryItem();
        testItem.setId(1L);
        testItem.setSku("TEST-001");

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        // Setup test warehouse
        testWarehouse = new Warehouse();
        testWarehouse.setId(1L);
        testWarehouse.setName("Test Warehouse");

        // Setup test activity
        testActivity = new StockActivity();
        testActivity.setId(1L);
        testActivity.setItem(testItem);
        testActivity.setItemSku("TEST-001");
        testActivity.setActivityType(ActivityType.ADJUSTMENT);
        testActivity.setQuantityChange(10);
        testActivity.setPerformedBy(testUser);
        testActivity.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testGetAllActivities_ReturnsAllActivities() {
        // Arrange
        List<StockActivity> activities = Arrays.asList(testActivity);
        when(stockActivityRepository.findAll()).thenReturn(activities);

        // Act
        List<StockActivityResponse> result = stockActivityService.getAllActivities();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stockActivityRepository, times(1)).findAll();
    }

    @Test
    void testGetActivityById_ActivityExists_ReturnsActivity() {
        // Arrange
        when(stockActivityRepository.findById(1L)).thenReturn(Optional.of(testActivity));

        // Act
        StockActivityResponse result = stockActivityService.getActivityById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(ActivityType.ADJUSTMENT, result.getActivityType());
        verify(stockActivityRepository, times(1)).findById(1L);
    }

    @Test
    void testGetActivityById_ActivityNotFound_ThrowsException() {
        // Arrange
        when(stockActivityRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> stockActivityService.getActivityById(999L));
        verify(stockActivityRepository, times(1)).findById(999L);
    }

    @Test
    void testGetActivitiesByItemId_ItemExists_ReturnsActivities() {
        // Arrange
        when(inventoryItemRepository.existsById(1L)).thenReturn(true);
        List<StockActivity> activities = Arrays.asList(testActivity);
        when(stockActivityRepository.findByItemId(1L)).thenReturn(activities);

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByItemId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventoryItemRepository, times(1)).existsById(1L);
        verify(stockActivityRepository, times(1)).findByItemId(1L);
    }

    @Test
    void testGetActivitiesByItemId_ItemNotFound_ThrowsException() {
        // Arrange
        when(inventoryItemRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> stockActivityService.getActivitiesByItemId(999L));
        verify(inventoryItemRepository, times(1)).existsById(999L);
        verify(stockActivityRepository, never()).findByItemId(anyLong());
    }

    @Test
    void testGetActivitiesByType_ReturnsActivities() {
        // Arrange
        List<StockActivity> activities = Arrays.asList(testActivity);
        when(stockActivityRepository.findByActivityType(ActivityType.ADJUSTMENT)).thenReturn(activities);

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByType(ActivityType.ADJUSTMENT);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stockActivityRepository, times(1)).findByActivityType(ActivityType.ADJUSTMENT);
    }

    @Test
    void testGetActivitiesByUserId_UserExists_ReturnsActivities() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        List<StockActivity> activities = Arrays.asList(testActivity);
        when(stockActivityRepository.findByPerformedById(1L)).thenReturn(activities);

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).existsById(1L);
        verify(stockActivityRepository, times(1)).findByPerformedById(1L);
    }

    @Test
    void testGetActivitiesByUserId_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> stockActivityService.getActivitiesByUserId(999L));
        verify(userRepository, times(1)).existsById(999L);
        verify(stockActivityRepository, never()).findByPerformedById(anyLong());
    }

    @Test
    void testGetActivitiesByDateRange_ReturnsActivities() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<StockActivity> activities = Arrays.asList(testActivity);
        when(stockActivityRepository.findByTimestampBetween(start, end)).thenReturn(activities);

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByDateRange(start, end);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stockActivityRepository, times(1)).findByTimestampBetween(start, end);
    }

    @Test
    void testGetActivitiesByItemIdSorted_ItemExists_ReturnsSortedActivities() {
        // Arrange
        when(inventoryItemRepository.existsById(1L)).thenReturn(true);
        List<StockActivity> activities = Arrays.asList(testActivity);
        when(stockActivityRepository.findByItemIdOrderByTimestampDesc(1L)).thenReturn(activities);

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByItemIdSorted(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventoryItemRepository, times(1)).existsById(1L);
        verify(stockActivityRepository, times(1)).findByItemIdOrderByTimestampDesc(1L);
    }

    @Test
    void testGetActivitiesByItemSku_ItemExists_ReturnsActivities() {
        // Arrange
        when(inventoryItemRepository.existsBySku("TEST-001")).thenReturn(true);
        when(inventoryItemRepository.findBySku("TEST-001")).thenReturn(Optional.of(testItem));
        when(inventoryItemRepository.existsById(1L)).thenReturn(true);
        List<StockActivity> activities = Arrays.asList(testActivity);
        when(stockActivityRepository.findByItemIdOrderByTimestampDesc(1L)).thenReturn(activities);

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByItemSku("TEST-001");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventoryItemRepository, times(1)).existsBySku("TEST-001");
        verify(inventoryItemRepository, times(1)).findBySku("TEST-001");
    }

    @Test
    void testGetActivitiesByUsername_UserExists_ReturnsActivities() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsById(1L)).thenReturn(true);
        List<StockActivity> activities = Arrays.asList(testActivity);
        when(stockActivityRepository.findByPerformedById(1L)).thenReturn(activities);

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(stockActivityRepository, times(1)).findByPerformedById(1L);
    }

    @Test
    void testGetActivitiesByUsername_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> stockActivityService.getActivitiesByUsername("nonexistent"));
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    void testGetActivitiesBySourceWarehouse_WarehouseExists_ReturnsActivities() {
        // Arrange
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        List<StockActivity> activities = Arrays.asList(testActivity);
        when(stockActivityRepository.findBySourceWarehouseId(1L)).thenReturn(activities);

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesBySourceWarehouse(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(warehouseRepository, times(1)).existsById(1L);
        verify(stockActivityRepository, times(1)).findBySourceWarehouseId(1L);
    }

    @Test
    void testGetActivitiesByDestinationWarehouse_WarehouseExists_ReturnsActivities() {
        // Arrange
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        List<StockActivity> activities = Arrays.asList(testActivity);
        when(stockActivityRepository.findByDestinationWarehouseId(1L)).thenReturn(activities);

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByDestinationWarehouse(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(warehouseRepository, times(1)).existsById(1L);
        verify(stockActivityRepository, times(1)).findByDestinationWarehouseId(1L);
    }

    @Test
    void testGetActivitiesByWarehouse_WarehouseExists_ReturnsActivities() {
        // Arrange
        when(warehouseRepository.existsById(1L)).thenReturn(true);
        List<StockActivity> sourceActivities = Arrays.asList(testActivity);
        List<StockActivity> destActivities = Arrays.asList();
        when(stockActivityRepository.findBySourceWarehouseId(1L)).thenReturn(sourceActivities);
        when(stockActivityRepository.findByDestinationWarehouseId(1L)).thenReturn(destActivities);

        // Act
        List<StockActivityResponse> result = stockActivityService.getActivitiesByWarehouse(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(warehouseRepository, times(1)).existsById(1L);
        verify(stockActivityRepository, times(1)).findBySourceWarehouseId(1L);
        verify(stockActivityRepository, times(1)).findByDestinationWarehouseId(1L);
    }
}
