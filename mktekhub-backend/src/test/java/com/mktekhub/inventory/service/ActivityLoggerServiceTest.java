/* Licensed under the Apache License, Version 2.0 */
package com.mktekhub.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mktekhub.inventory.model.*;
import com.mktekhub.inventory.repository.StockActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Comprehensive unit tests for ActivityLoggerService. */
@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityLoggerService Tests")
class ActivityLoggerServiceTest {

  @Mock private StockActivityRepository stockActivityRepository;

  @InjectMocks private ActivityLoggerService activityLoggerService;

  private InventoryItem item;
  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setUsername("testuser");

    item = new InventoryItem();
    item.setId(1L);
    item.setSku("SKU-001");
    item.setName("Test Item");
    item.setQuantity(100);
  }

  @Test
  @DisplayName("LogActivity - Should log generic activity")
  void logActivity_Success() {
    // Arrange
    StockActivity savedActivity = new StockActivity();
    when(stockActivityRepository.save(any(StockActivity.class))).thenReturn(savedActivity);

    // Act
    StockActivity result =
        activityLoggerService.logActivity(item, ActivityType.RECEIVE, 50, 0, 50, user, "Test note");

    // Assert
    assertNotNull(result);
    ArgumentCaptor<StockActivity> captor = ArgumentCaptor.forClass(StockActivity.class);
    verify(stockActivityRepository).save(captor.capture());
    StockActivity activity = captor.getValue();

    assertEquals(item, activity.getItem());
    assertEquals("SKU-001", activity.getItemSku());
    assertEquals(ActivityType.RECEIVE, activity.getActivityType());
    assertEquals(50, activity.getQuantityChange());
    assertEquals(0, activity.getPreviousQuantity());
    assertEquals(50, activity.getNewQuantity());
    assertEquals("Test note", activity.getNotes());
    assertEquals(user, activity.getPerformedBy());
  }

  @Test
  @DisplayName("LogAdjustment - Should log adjustment with quantity details")
  void logAdjustment_Success() {
    // Arrange
    StockActivity savedActivity = new StockActivity();
    when(stockActivityRepository.save(any(StockActivity.class))).thenReturn(savedActivity);

    // Act
    StockActivity result = activityLoggerService.logAdjustment(item, 10, 100, 110, user);

    // Assert
    assertNotNull(result);
    ArgumentCaptor<StockActivity> captor = ArgumentCaptor.forClass(StockActivity.class);
    verify(stockActivityRepository).save(captor.capture());
    StockActivity activity = captor.getValue();

    assertEquals(ActivityType.ADJUSTMENT, activity.getActivityType());
    assertEquals(10, activity.getQuantityChange());
    assertEquals(100, activity.getPreviousQuantity());
    assertEquals(110, activity.getNewQuantity());
    assertTrue(activity.getNotes().contains("Manual quantity adjustment"));
    assertTrue(activity.getNotes().contains("+10"));
  }

  @Test
  @DisplayName("LogAdjustment - Should format negative adjustment correctly")
  void logAdjustment_NegativeChange() {
    // Arrange
    when(stockActivityRepository.save(any(StockActivity.class))).thenReturn(new StockActivity());

    // Act
    activityLoggerService.logAdjustment(item, -15, 100, 85, user);

    // Assert
    ArgumentCaptor<StockActivity> captor = ArgumentCaptor.forClass(StockActivity.class);
    verify(stockActivityRepository).save(captor.capture());
    StockActivity activity = captor.getValue();

    assertEquals(-15, activity.getQuantityChange());
    assertTrue(activity.getNotes().contains("-15"));
  }

  @Test
  @DisplayName("LogTransfer - Should log transfer with notes")
  void logTransfer_Success() {
    // Arrange
    when(stockActivityRepository.save(any(StockActivity.class))).thenReturn(new StockActivity());

    // Act
    activityLoggerService.logTransfer(item, 25, 100, 75, user, "Transferred to another warehouse");

    // Assert
    ArgumentCaptor<StockActivity> captor = ArgumentCaptor.forClass(StockActivity.class);
    verify(stockActivityRepository).save(captor.capture());
    StockActivity activity = captor.getValue();

    assertEquals(ActivityType.TRANSFER, activity.getActivityType());
    assertEquals(25, activity.getQuantityChange());
    assertEquals(100, activity.getPreviousQuantity());
    assertEquals(75, activity.getNewQuantity());
    assertEquals("Transferred to another warehouse", activity.getNotes());
  }

  @Test
  @DisplayName("LogReceive - Should log receive activity")
  void logReceive_Success() {
    // Arrange
    when(stockActivityRepository.save(any(StockActivity.class))).thenReturn(new StockActivity());

    // Act
    activityLoggerService.logReceive(item, 50, user, "Received from supplier");

    // Assert
    ArgumentCaptor<StockActivity> captor = ArgumentCaptor.forClass(StockActivity.class);
    verify(stockActivityRepository).save(captor.capture());
    StockActivity activity = captor.getValue();

    assertEquals(ActivityType.RECEIVE, activity.getActivityType());
    assertEquals(50, activity.getQuantityChange());
    assertEquals(0, activity.getPreviousQuantity());
    assertEquals(50, activity.getNewQuantity());
    assertEquals("Received from supplier", activity.getNotes());
  }

  @Test
  @DisplayName("LogUpdate - Should log update activity")
  void logUpdate_Success() {
    // Arrange
    when(stockActivityRepository.save(any(StockActivity.class))).thenReturn(new StockActivity());

    // Act
    activityLoggerService.logUpdate(item, user, "Price updated");

    // Assert
    ArgumentCaptor<StockActivity> captor = ArgumentCaptor.forClass(StockActivity.class);
    verify(stockActivityRepository).save(captor.capture());
    StockActivity activity = captor.getValue();

    assertEquals(ActivityType.UPDATE, activity.getActivityType());
    assertEquals(0, activity.getQuantityChange());
    assertEquals(100, activity.getPreviousQuantity());
    assertEquals(100, activity.getNewQuantity());
    assertEquals("Price updated", activity.getNotes());
  }

  @Test
  @DisplayName("LogActivity - Should handle null notes")
  void logActivity_NullNotes() {
    // Arrange
    when(stockActivityRepository.save(any(StockActivity.class))).thenReturn(new StockActivity());

    // Act
    activityLoggerService.logActivity(item, ActivityType.RECEIVE, 10, 0, 10, user, null);

    // Assert
    ArgumentCaptor<StockActivity> captor = ArgumentCaptor.forClass(StockActivity.class);
    verify(stockActivityRepository).save(captor.capture());
    StockActivity activity = captor.getValue();

    assertNull(activity.getNotes());
  }

  @Test
  @DisplayName("LogActivity - Should handle negative quantity change")
  void logActivity_NegativeQuantity() {
    // Arrange
    when(stockActivityRepository.save(any(StockActivity.class))).thenReturn(new StockActivity());

    // Act
    activityLoggerService.logActivity(
        item, ActivityType.ADJUSTMENT, -20, 100, 80, user, "Stock reduction");

    // Assert
    ArgumentCaptor<StockActivity> captor = ArgumentCaptor.forClass(StockActivity.class);
    verify(stockActivityRepository).save(captor.capture());
    StockActivity activity = captor.getValue();

    assertEquals(-20, activity.getQuantityChange());
    assertEquals(100, activity.getPreviousQuantity());
    assertEquals(80, activity.getNewQuantity());
  }
}
