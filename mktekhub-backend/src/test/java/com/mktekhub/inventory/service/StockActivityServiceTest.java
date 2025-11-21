/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mktekhub.inventory.dto.StockActivityResponse;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.model.*;
import com.mktekhub.inventory.repository.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Comprehensive unit tests for StockActivityService. */
@ExtendWith(MockitoExtension.class)
@DisplayName("StockActivityService Tests")
class StockActivityServiceTest {

  @Mock private StockActivityRepository stockActivityRepository;

  @Mock private InventoryItemRepository inventoryItemRepository;

  @Mock private WarehouseRepository warehouseRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private StockActivityService stockActivityService;

  private StockActivity activity1;
  private StockActivity activity2;
  private InventoryItem item;
  private User user;
  private Warehouse warehouse;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setUsername("testuser");

    warehouse = new Warehouse();
    warehouse.setId(1L);
    warehouse.setName("Main Warehouse");

    item = new InventoryItem();
    item.setId(1L);
    item.setSku("SKU-001");
    item.setName("Test Item");

    activity1 = new StockActivity();
    activity1.setId(1L);
    activity1.setItem(item);
    activity1.setItemSku("SKU-001");
    activity1.setActivityType(ActivityType.RECEIVE);
    activity1.setQuantityChange(100);
    activity1.setPerformedBy(user);
    activity1.setTimestamp(LocalDateTime.now());

    activity2 = new StockActivity();
    activity2.setId(2L);
    activity2.setItem(item);
    activity2.setItemSku("SKU-001");
    activity2.setActivityType(ActivityType.ADJUSTMENT);
    activity2.setQuantityChange(-10);
    activity2.setPerformedBy(user);
    activity2.setTimestamp(LocalDateTime.now().minusDays(1));
  }

  // ==================== GET ALL ACTIVITIES ====================

  @Test
  @DisplayName("GetAllActivities - Should return all activities")
  void getAllActivities_Success() {
    // Arrange
    when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity1, activity2));

    // Act
    List<StockActivityResponse> result = stockActivityService.getAllActivities();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(stockActivityRepository).findAll();
  }

  @Test
  @DisplayName("GetAllActivities - Should return empty list when no activities")
  void getAllActivities_Empty() {
    // Arrange
    when(stockActivityRepository.findAll()).thenReturn(Collections.emptyList());

    // Act
    List<StockActivityResponse> result = stockActivityService.getAllActivities();

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  // ==================== GET ACTIVITY BY ID ====================

  @Test
  @DisplayName("GetActivityById - Should return activity when found")
  void getActivityById_Success() {
    // Arrange
    when(stockActivityRepository.findById(1L)).thenReturn(Optional.of(activity1));

    // Act
    StockActivityResponse result = stockActivityService.getActivityById(1L);

    // Assert
    assertNotNull(result);
    verify(stockActivityRepository).findById(1L);
  }

  @Test
  @DisplayName("GetActivityById - Should throw exception when not found")
  void getActivityById_NotFound() {
    // Arrange
    when(stockActivityRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> stockActivityService.getActivityById(999L));
  }

  // ==================== GET ACTIVITIES BY ITEM ID ====================

  @Test
  @DisplayName("GetActivitiesByItemId - Should return activities for item")
  void getActivitiesByItemId_Success() {
    // Arrange
    when(inventoryItemRepository.existsById(1L)).thenReturn(true);
    when(stockActivityRepository.findByItemId(1L)).thenReturn(Arrays.asList(activity1, activity2));

    // Act
    List<StockActivityResponse> result = stockActivityService.getActivitiesByItemId(1L);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(inventoryItemRepository).existsById(1L);
    verify(stockActivityRepository).findByItemId(1L);
  }

  @Test
  @DisplayName("GetActivitiesByItemId - Should throw exception when item not found")
  void getActivitiesByItemId_ItemNotFound() {
    // Arrange
    when(inventoryItemRepository.existsById(999L)).thenReturn(false);

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class, () -> stockActivityService.getActivitiesByItemId(999L));
  }

  // ==================== GET ACTIVITIES BY ITEM ID SORTED ====================

  @Test
  @DisplayName("GetActivitiesByItemIdSorted - Should return sorted activities")
  void getActivitiesByItemIdSorted_Success() {
    // Arrange
    when(inventoryItemRepository.existsById(1L)).thenReturn(true);
    when(stockActivityRepository.findByItemIdOrderByTimestampDesc(1L))
        .thenReturn(Arrays.asList(activity1, activity2));

    // Act
    List<StockActivityResponse> result = stockActivityService.getActivitiesByItemIdSorted(1L);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(stockActivityRepository).findByItemIdOrderByTimestampDesc(1L);
  }

  // ==================== GET ACTIVITIES BY SKU ====================

  @Test
  @DisplayName("GetActivitiesByItemSku - Should return activities for SKU")
  void getActivitiesByItemSku_Success() {
    // Arrange
    when(inventoryItemRepository.existsBySku("SKU-001")).thenReturn(true);
    when(inventoryItemRepository.findBySku("SKU-001")).thenReturn(Optional.of(item));
    when(inventoryItemRepository.existsById(1L)).thenReturn(true);
    when(stockActivityRepository.findByItemIdOrderByTimestampDesc(1L))
        .thenReturn(Arrays.asList(activity1));

    // Act
    List<StockActivityResponse> result = stockActivityService.getActivitiesByItemSku("SKU-001");

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("GetActivitiesByItemSku - Should throw exception when SKU not found")
  void getActivitiesByItemSku_NotFound() {
    // Arrange
    when(inventoryItemRepository.existsBySku("INVALID")).thenReturn(false);

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class,
        () -> stockActivityService.getActivitiesByItemSku("INVALID"));
  }

  // ==================== GET ACTIVITIES BY TYPE ====================

  @Test
  @DisplayName("GetActivitiesByType - Should return activities of specific type")
  void getActivitiesByType_Success() {
    // Arrange
    when(stockActivityRepository.findByActivityType(ActivityType.RECEIVE))
        .thenReturn(Collections.singletonList(activity1));

    // Act
    List<StockActivityResponse> result =
        stockActivityService.getActivitiesByType(ActivityType.RECEIVE);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  // ==================== GET ACTIVITIES BY USER ID ====================

  @Test
  @DisplayName("GetActivitiesByUserId - Should return activities for user")
  void getActivitiesByUserId_Success() {
    // Arrange
    when(userRepository.existsById(1L)).thenReturn(true);
    when(stockActivityRepository.findByPerformedById(1L))
        .thenReturn(Arrays.asList(activity1, activity2));

    // Act
    List<StockActivityResponse> result = stockActivityService.getActivitiesByUserId(1L);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("GetActivitiesByUserId - Should throw exception when user not found")
  void getActivitiesByUserId_UserNotFound() {
    // Arrange
    when(userRepository.existsById(999L)).thenReturn(false);

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class, () -> stockActivityService.getActivitiesByUserId(999L));
  }

  // ==================== GET ACTIVITIES BY USERNAME ====================

  @Test
  @DisplayName("GetActivitiesByUsername - Should return activities for username")
  void getActivitiesByUsername_Success() {
    // Arrange
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(userRepository.existsById(1L)).thenReturn(true);
    when(stockActivityRepository.findByPerformedById(1L)).thenReturn(Arrays.asList(activity1));

    // Act
    List<StockActivityResponse> result = stockActivityService.getActivitiesByUsername("testuser");

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("GetActivitiesByUsername - Should throw exception when username not found")
  void getActivitiesByUsername_NotFound() {
    // Arrange
    when(userRepository.findByUsername("invalid")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class,
        () -> stockActivityService.getActivitiesByUsername("invalid"));
  }

  // ==================== GET ACTIVITIES BY WAREHOUSE ====================

  @Test
  @DisplayName("GetActivitiesBySourceWarehouse - Should return source warehouse activities")
  void getActivitiesBySourceWarehouse_Success() {
    // Arrange
    when(warehouseRepository.existsById(1L)).thenReturn(true);
    when(stockActivityRepository.findBySourceWarehouseId(1L))
        .thenReturn(Collections.singletonList(activity1));

    // Act
    List<StockActivityResponse> result = stockActivityService.getActivitiesBySourceWarehouse(1L);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName(
      "GetActivitiesByDestinationWarehouse - Should return destination warehouse activities")
  void getActivitiesByDestinationWarehouse_Success() {
    // Arrange
    when(warehouseRepository.existsById(1L)).thenReturn(true);
    when(stockActivityRepository.findByDestinationWarehouseId(1L))
        .thenReturn(Collections.singletonList(activity1));

    // Act
    List<StockActivityResponse> result =
        stockActivityService.getActivitiesByDestinationWarehouse(1L);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("GetActivitiesByWarehouse - Should return all warehouse activities")
  void getActivitiesByWarehouse_Success() {
    // Arrange
    when(warehouseRepository.existsById(1L)).thenReturn(true);
    when(stockActivityRepository.findBySourceWarehouseId(1L))
        .thenReturn(new ArrayList<>(Collections.singletonList(activity1)));
    when(stockActivityRepository.findByDestinationWarehouseId(1L))
        .thenReturn(new ArrayList<>(Collections.singletonList(activity2)));

    // Act
    List<StockActivityResponse> result = stockActivityService.getActivitiesByWarehouse(1L);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
  }

  // ==================== GET ACTIVITIES BY DATE RANGE ====================

  @Test
  @DisplayName("GetActivitiesByDateRange - Should return activities in date range")
  void getActivitiesByDateRange_Success() {
    // Arrange
    LocalDateTime start = LocalDateTime.now().minusDays(7);
    LocalDateTime end = LocalDateTime.now();
    when(stockActivityRepository.findByTimestampBetween(start, end))
        .thenReturn(Arrays.asList(activity1, activity2));

    // Act
    List<StockActivityResponse> result = stockActivityService.getActivitiesByDateRange(start, end);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
  }

  // ==================== GET ACTIVITIES WITH FILTERS ====================

  @Test
  @DisplayName("GetActivitiesWithFilters - Should filter by item ID")
  void getActivitiesWithFilters_ByItemId() {
    // Arrange
    when(inventoryItemRepository.existsById(1L)).thenReturn(true);
    when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity1, activity2));

    // Act
    List<StockActivityResponse> result =
        stockActivityService.getActivitiesWithFilters(1L, null, null, null, null, null, null, null);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("GetActivitiesWithFilters - Should filter by SKU")
  void getActivitiesWithFilters_BySku() {
    // Arrange
    when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity1, activity2));

    // Act
    List<StockActivityResponse> result =
        stockActivityService.getActivitiesWithFilters(
            null, "SKU-001", null, null, null, null, null, null);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("GetActivitiesWithFilters - Should filter by activity type")
  void getActivitiesWithFilters_ByType() {
    // Arrange
    when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity1, activity2));

    // Act
    List<StockActivityResponse> result =
        stockActivityService.getActivitiesWithFilters(
            null, null, ActivityType.RECEIVE, null, null, null, null, null);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("GetActivitiesWithFilters - Should filter by user ID")
  void getActivitiesWithFilters_ByUserId() {
    // Arrange
    when(userRepository.existsById(1L)).thenReturn(true);
    when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity1, activity2));

    // Act
    List<StockActivityResponse> result =
        stockActivityService.getActivitiesWithFilters(null, null, null, 1L, null, null, null, null);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("GetActivitiesWithFilters - Should filter by username")
  void getActivitiesWithFilters_ByUsername() {
    // Arrange
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity1, activity2));

    // Act
    List<StockActivityResponse> result =
        stockActivityService.getActivitiesWithFilters(
            null, null, null, null, "testuser", null, null, null);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("GetActivitiesWithFilters - Should filter by warehouse ID")
  void getActivitiesWithFilters_ByWarehouseId() {
    // Arrange
    activity1.setSourceWarehouse(warehouse);
    when(warehouseRepository.existsById(1L)).thenReturn(true);
    when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity1, activity2));

    // Act
    List<StockActivityResponse> result =
        stockActivityService.getActivitiesWithFilters(null, null, null, null, null, 1L, null, null);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("GetActivitiesWithFilters - Should filter by date range")
  void getActivitiesWithFilters_ByDateRange() {
    // Arrange
    LocalDateTime start = LocalDateTime.now().minusDays(2);
    LocalDateTime end = LocalDateTime.now().plusDays(1);
    when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity1, activity2));

    // Act
    List<StockActivityResponse> result =
        stockActivityService.getActivitiesWithFilters(
            null, null, null, null, null, null, start, end);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("GetActivitiesWithFilters - Should filter by start date only")
  void getActivitiesWithFilters_ByStartDate() {
    // Arrange
    LocalDateTime start = LocalDateTime.now().minusDays(2);
    when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity1, activity2));

    // Act
    List<StockActivityResponse> result =
        stockActivityService.getActivitiesWithFilters(
            null, null, null, null, null, null, start, null);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("GetActivitiesWithFilters - Should filter by end date only")
  void getActivitiesWithFilters_ByEndDate() {
    // Arrange
    LocalDateTime end = LocalDateTime.now().plusDays(1);
    when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity1, activity2));

    // Act
    List<StockActivityResponse> result =
        stockActivityService.getActivitiesWithFilters(
            null, null, null, null, null, null, null, end);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("GetActivitiesWithFilters - Should handle no filters")
  void getActivitiesWithFilters_NoFilters() {
    // Arrange
    when(stockActivityRepository.findAll()).thenReturn(Arrays.asList(activity1, activity2));

    // Act
    List<StockActivityResponse> result =
        stockActivityService.getActivitiesWithFilters(
            null, null, null, null, null, null, null, null);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("GetActivitiesWithFilters - Should throw exception for invalid item ID")
  void getActivitiesWithFilters_InvalidItemId() {
    // Arrange
    when(inventoryItemRepository.existsById(999L)).thenReturn(false);

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class,
        () ->
            stockActivityService.getActivitiesWithFilters(
                999L, null, null, null, null, null, null, null));
  }

  @Test
  @DisplayName("GetActivitiesWithFilters - Should throw exception for invalid username")
  void getActivitiesWithFilters_InvalidUsername() {
    // Arrange
    when(userRepository.findByUsername("invalid")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class,
        () ->
            stockActivityService.getActivitiesWithFilters(
                null, null, null, null, "invalid", null, null, null));
  }
}
