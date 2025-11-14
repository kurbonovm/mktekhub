package com.mktekhub.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mktekhub.inventory.model.ActivityType;
import com.mktekhub.inventory.model.InventoryItem;
import com.mktekhub.inventory.model.StockActivity;
import com.mktekhub.inventory.model.User;
import com.mktekhub.inventory.repository.StockActivityRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityLoggerServiceTest {

    @Mock
    private StockActivityRepository stockActivityRepository;

    @InjectMocks
    private ActivityLoggerService activityLoggerService;

    private InventoryItem item;
    private User user;

    @BeforeEach
    void setUp() {
        item = new InventoryItem();
        item.setId(1L);
        item.setSku("TEST-001");
        item.setName("Test Item");
        item.setQuantity(100);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    @Test
    void testLogActivity_SavesAndReturnsActivity() {
        // Arrange
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StockActivity result = activityLoggerService.logActivity(
                item, ActivityType.ADJUSTMENT, 10, 90, 100, user, "Test notes");

        // Assert
        assertNotNull(result);
        assertEquals(ActivityType.ADJUSTMENT, result.getActivityType());
        verify(stockActivityRepository, times(1)).save(any(StockActivity.class));
    }

    @Test
    void testLogAdjustment_SavesAdjustmentActivity() {
        // Arrange
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StockActivity result = activityLoggerService.logAdjustment(item, 10, 90, 100, user);

        // Assert
        assertNotNull(result);
        assertEquals(ActivityType.ADJUSTMENT, result.getActivityType());
        verify(stockActivityRepository, times(1)).save(any(StockActivity.class));
    }

    @Test
    void testLogTransfer_SavesTransferActivity() {
        // Arrange
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StockActivity result = activityLoggerService.logTransfer(item, 10, 90, 100, user, "Transfer notes");

        // Assert
        assertNotNull(result);
        assertEquals(ActivityType.TRANSFER, result.getActivityType());
        verify(stockActivityRepository, times(1)).save(any(StockActivity.class));
    }

    @Test
    void testLogReceive_SavesReceiveActivity() {
        // Arrange
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StockActivity result = activityLoggerService.logReceive(item, 50, user, "Receive notes");

        // Assert
        assertNotNull(result);
        assertEquals(ActivityType.RECEIVE, result.getActivityType());
        verify(stockActivityRepository, times(1)).save(any(StockActivity.class));
    }

    @Test
    void testLogUpdate_SavesUpdateActivity() {
        // Arrange
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StockActivity result = activityLoggerService.logUpdate(item, user, "Update notes");

        // Assert
        assertNotNull(result);
        assertEquals(ActivityType.UPDATE, result.getActivityType());
        verify(stockActivityRepository, times(1)).save(any(StockActivity.class));
    }
}
