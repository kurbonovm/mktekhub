package com.mktekhub.inventory.service;

import com.mktekhub.inventory.dto.StockActivityResponse;
import com.mktekhub.inventory.exception.ResourceNotFoundException;
import com.mktekhub.inventory.model.ActivityType;
import com.mktekhub.inventory.model.StockActivity;
import com.mktekhub.inventory.repository.InventoryItemRepository;
import com.mktekhub.inventory.repository.StockActivityRepository;
import com.mktekhub.inventory.repository.UserRepository;
import com.mktekhub.inventory.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for stock activity tracking and history.
 */
@Service
public class StockActivityService {

    @Autowired
    private StockActivityRepository stockActivityRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all stock activities
     */
    public List<StockActivityResponse> getAllActivities() {
        return stockActivityRepository.findAll().stream()
            .map(StockActivityResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get stock activity by ID
     */
    public StockActivityResponse getActivityById(Long id) {
        StockActivity activity = stockActivityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("StockActivity", "id", id));
        return StockActivityResponse.fromEntity(activity);
    }

    /**
     * Get activities for a specific inventory item
     */
    public List<StockActivityResponse> getActivitiesByItemId(Long itemId) {
        // Validate item exists
        if (!inventoryItemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("InventoryItem", "id", itemId);
        }

        return stockActivityRepository.findByItemId(itemId).stream()
            .map(StockActivityResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get activities for a specific inventory item (sorted by timestamp desc)
     */
    public List<StockActivityResponse> getActivitiesByItemIdSorted(Long itemId) {
        // Validate item exists
        if (!inventoryItemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("InventoryItem", "id", itemId);
        }

        return stockActivityRepository.findByItemIdOrderByTimestampDesc(itemId).stream()
            .map(StockActivityResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get activities for a specific item SKU
     */
    public List<StockActivityResponse> getActivitiesByItemSku(String sku) {
        // Validate item exists
        if (!inventoryItemRepository.existsBySku(sku)) {
            throw new ResourceNotFoundException("InventoryItem", "sku", sku);
        }

        // Find item by SKU and get its activities
        Long itemId = inventoryItemRepository.findBySku(sku)
            .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "sku", sku))
            .getId();

        return getActivitiesByItemIdSorted(itemId);
    }

    /**
     * Get activities by activity type
     */
    public List<StockActivityResponse> getActivitiesByType(ActivityType activityType) {
        return stockActivityRepository.findByActivityType(activityType).stream()
            .map(StockActivityResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get activities performed by a specific user
     */
    public List<StockActivityResponse> getActivitiesByUserId(Long userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        return stockActivityRepository.findByPerformedById(userId).stream()
            .map(StockActivityResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get activities by username
     */
    public List<StockActivityResponse> getActivitiesByUsername(String username) {
        // Validate user exists
        Long userId = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username))
            .getId();

        return getActivitiesByUserId(userId);
    }

    /**
     * Get activities for a specific warehouse (as source)
     */
    public List<StockActivityResponse> getActivitiesBySourceWarehouse(Long warehouseId) {
        // Validate warehouse exists
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse", "id", warehouseId);
        }

        return stockActivityRepository.findBySourceWarehouseId(warehouseId).stream()
            .map(StockActivityResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get activities for a specific warehouse (as destination)
     */
    public List<StockActivityResponse> getActivitiesByDestinationWarehouse(Long warehouseId) {
        // Validate warehouse exists
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse", "id", warehouseId);
        }

        return stockActivityRepository.findByDestinationWarehouseId(warehouseId).stream()
            .map(StockActivityResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get all activities for a warehouse (both as source and destination)
     */
    public List<StockActivityResponse> getActivitiesByWarehouse(Long warehouseId) {
        // Validate warehouse exists
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse", "id", warehouseId);
        }

        List<StockActivity> activities = stockActivityRepository.findBySourceWarehouseId(warehouseId);
        activities.addAll(stockActivityRepository.findByDestinationWarehouseId(warehouseId));

        return activities.stream()
            .distinct()
            .map(StockActivityResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get activities within a date range
     */
    public List<StockActivityResponse> getActivitiesByDateRange(
            LocalDateTime startDate, LocalDateTime endDate) {

        return stockActivityRepository.findByTimestampBetween(startDate, endDate).stream()
            .map(StockActivityResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get activities with multiple filters
     * All parameters are optional (nullable)
     */
    public List<StockActivityResponse> getActivitiesWithFilters(
            Long itemId,
            String sku,
            ActivityType activityType,
            Long userId,
            String username,
            Long warehouseId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        List<StockActivity> activities = stockActivityRepository.findAll();

        // Apply filters
        if (itemId != null) {
            if (!inventoryItemRepository.existsById(itemId)) {
                throw new ResourceNotFoundException("InventoryItem", "id", itemId);
            }
            activities = activities.stream()
                .filter(a -> a.getItem().getId().equals(itemId))
                .collect(Collectors.toList());
        }

        if (sku != null && !sku.isEmpty()) {
            activities = activities.stream()
                .filter(a -> a.getItemSku().equalsIgnoreCase(sku))
                .collect(Collectors.toList());
        }

        if (activityType != null) {
            activities = activities.stream()
                .filter(a -> a.getActivityType().equals(activityType))
                .collect(Collectors.toList());
        }

        if (userId != null) {
            if (!userRepository.existsById(userId)) {
                throw new ResourceNotFoundException("User", "id", userId);
            }
            activities = activities.stream()
                .filter(a -> a.getPerformedBy().getId().equals(userId))
                .collect(Collectors.toList());
        }

        if (username != null && !username.isEmpty()) {
            if (!userRepository.findByUsername(username).isPresent()) {
                throw new ResourceNotFoundException("User", "username", username);
            }
            activities = activities.stream()
                .filter(a -> a.getPerformedBy().getUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
        }

        if (warehouseId != null) {
            if (!warehouseRepository.existsById(warehouseId)) {
                throw new ResourceNotFoundException("Warehouse", "id", warehouseId);
            }
            activities = activities.stream()
                .filter(a -> (a.getSourceWarehouse() != null &&
                             a.getSourceWarehouse().getId().equals(warehouseId)) ||
                            (a.getDestinationWarehouse() != null &&
                             a.getDestinationWarehouse().getId().equals(warehouseId)))
                .collect(Collectors.toList());
        }

        if (startDate != null && endDate != null) {
            activities = activities.stream()
                .filter(a -> !a.getTimestamp().isBefore(startDate) &&
                           !a.getTimestamp().isAfter(endDate))
                .collect(Collectors.toList());
        } else if (startDate != null) {
            activities = activities.stream()
                .filter(a -> !a.getTimestamp().isBefore(startDate))
                .collect(Collectors.toList());
        } else if (endDate != null) {
            activities = activities.stream()
                .filter(a -> !a.getTimestamp().isAfter(endDate))
                .collect(Collectors.toList());
        }

        return activities.stream()
            .map(StockActivityResponse::fromEntity)
            .collect(Collectors.toList());
    }
}
