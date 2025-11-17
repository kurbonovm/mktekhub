package com.mktekhub.inventory.service;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        item.setSku("SKU001");
        item.setName("Test Item");
        item.setQuantity(100);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    @Test
    void logActivity_SavesActivityCorrectly() {
        // Arrange
        StockActivity expectedActivity = new StockActivity();
        when(stockActivityRepository.save(any(StockActivity.class))).thenReturn(expectedActivity);

        // Act
        StockActivity result = activityLoggerService.logActivity(
                item,
                ActivityType.ADJUSTMENT,
                10,
                100,
                110,
                user,
                "Test notes"
        );

        // Assert
        assertNotNull(result);
        verify(stockActivityRepository).save(any(StockActivity.class));
    }

    @Test
    void logActivity_SetsAllFieldsCorrectly() {
        // Arrange
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> {
            StockActivity activity = invocation.getArgument(0);

            // Verify all fields
            assertEquals(item, activity.getItem());
            assertEquals("SKU001", activity.getItemSku());
            assertEquals(ActivityType.ADJUSTMENT, activity.getActivityType());
            assertEquals(10, activity.getQuantityChange());
            assertEquals(100, activity.getPreviousQuantity());
            assertEquals(110, activity.getNewQuantity());
            assertEquals(user, activity.getPerformedBy());
            assertEquals("Test notes", activity.getNotes());

            return activity;
        });

        // Act
        activityLoggerService.logActivity(item, ActivityType.ADJUSTMENT, 10, 100, 110, user, "Test notes");

        // Assert
        verify(stockActivityRepository).save(any(StockActivity.class));
    }

    @Test
    void logAdjustment_CreatesAdjustmentActivity_WithPositiveChange() {
        // Arrange
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> {
            StockActivity activity = invocation.getArgument(0);

            // Verify adjustment-specific fields
            assertEquals(ActivityType.ADJUSTMENT, activity.getActivityType());
            assertEquals(10, activity.getQuantityChange());
            assertEquals(100, activity.getPreviousQuantity());
            assertEquals(110, activity.getNewQuantity());
            assertTrue(activity.getNotes().contains("+10"));
            assertTrue(activity.getNotes().contains("Manual quantity adjustment"));

            return activity;
        });

        // Act
        activityLoggerService.logAdjustment(item, 10, 100, 110, user);

        // Assert
        verify(stockActivityRepository).save(any(StockActivity.class));
    }

    @Test
    void logAdjustment_CreatesAdjustmentActivity_WithNegativeChange() {
        // Arrange
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> {
            StockActivity activity = invocation.getArgument(0);

            // Verify adjustment-specific fields
            assertEquals(ActivityType.ADJUSTMENT, activity.getActivityType());
            assertEquals(-20, activity.getQuantityChange());
            assertEquals(100, activity.getPreviousQuantity());
            assertEquals(80, activity.getNewQuantity());
            assertTrue(activity.getNotes().contains("-20"));
            assertFalse(activity.getNotes().contains("+-"));

            return activity;
        });

        // Act
        activityLoggerService.logAdjustment(item, -20, 100, 80, user);

        // Assert
        verify(stockActivityRepository).save(any(StockActivity.class));
    }

    @Test
    void logTransfer_CreatesTransferActivity() {
        // Arrange
        String notes = "Transfer from Warehouse A to Warehouse B";
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> {
            StockActivity activity = invocation.getArgument(0);

            // Verify transfer-specific fields
            assertEquals(ActivityType.TRANSFER, activity.getActivityType());
            assertEquals(50, activity.getQuantityChange());
            assertEquals(100, activity.getPreviousQuantity());
            assertEquals(150, activity.getNewQuantity());
            assertEquals(notes, activity.getNotes());

            return activity;
        });

        // Act
        activityLoggerService.logTransfer(item, 50, 100, 150, user, notes);

        // Assert
        verify(stockActivityRepository).save(any(StockActivity.class));
    }

    @Test
    void logReceive_CreatesReceiveActivity() {
        // Arrange
        String notes = "Received shipment";
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> {
            StockActivity activity = invocation.getArgument(0);

            // Verify receive-specific fields
            assertEquals(ActivityType.RECEIVE, activity.getActivityType());
            assertEquals(50, activity.getQuantityChange());
            assertEquals(0, activity.getPreviousQuantity());
            assertEquals(50, activity.getNewQuantity());
            assertEquals(notes, activity.getNotes());

            return activity;
        });

        // Act
        activityLoggerService.logReceive(item, 50, user, notes);

        // Assert
        verify(stockActivityRepository).save(any(StockActivity.class));
    }

    @Test
    void logUpdate_CreatesUpdateActivity() {
        // Arrange
        String notes = "Item details updated";
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> {
            StockActivity activity = invocation.getArgument(0);

            // Verify update-specific fields
            assertEquals(ActivityType.UPDATE, activity.getActivityType());
            assertEquals(0, activity.getQuantityChange());
            assertEquals(item.getQuantity(), activity.getPreviousQuantity());
            assertEquals(item.getQuantity(), activity.getNewQuantity());
            assertEquals(notes, activity.getNotes());

            return activity;
        });

        // Act
        activityLoggerService.logUpdate(item, user, notes);

        // Assert
        verify(stockActivityRepository).save(any(StockActivity.class));
    }

    @Test
    void logActivity_HandlesNullNotes() {
        // Arrange
        when(stockActivityRepository.save(any(StockActivity.class))).thenAnswer(invocation -> {
            StockActivity activity = invocation.getArgument(0);
            assertNull(activity.getNotes());
            return activity;
        });

        // Act
        activityLoggerService.logActivity(item, ActivityType.ADJUSTMENT, 10, 100, 110, user, null);

        // Assert
        verify(stockActivityRepository).save(any(StockActivity.class));
    }

    @Test
    void logActivity_ReturnsPersistedEntity() {
        // Arrange
        StockActivity persistedActivity = new StockActivity();
        persistedActivity.setId(123L);
        when(stockActivityRepository.save(any(StockActivity.class))).thenReturn(persistedActivity);

        // Act
        StockActivity result = activityLoggerService.logActivity(
                item, ActivityType.ADJUSTMENT, 10, 100, 110, user, "Test"
        );

        // Assert
        assertNotNull(result);
        assertEquals(123L, result.getId());
    }
}
