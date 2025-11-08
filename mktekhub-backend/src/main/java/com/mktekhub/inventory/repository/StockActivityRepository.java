package com.mktekhub.inventory.repository;

import com.mktekhub.inventory.model.ActivityType;
import com.mktekhub.inventory.model.StockActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for StockActivity entity.
 * Provides database operations for stock activity tracking and auditing.
 */
@Repository
public interface StockActivityRepository extends JpaRepository<StockActivity, Long> {

    /**
     * Find all activities for a specific item.
     */
    List<StockActivity> findByItemId(Long itemId);

    /**
     * Find all activities by type.
     */
    List<StockActivity> findByActivityType(ActivityType activityType);

    /**
     * Find all activities performed by a specific user.
     */
    List<StockActivity> findByPerformedById(Long userId);

    /**
     * Find all activities for a specific warehouse (as source).
     */
    List<StockActivity> findBySourceWarehouseId(Long warehouseId);

    /**
     * Find all activities for a specific warehouse (as destination).
     */
    List<StockActivity> findByDestinationWarehouseId(Long warehouseId);

    /**
     * Find all activities within a date range.
     */
    List<StockActivity> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find recent activities for an item ordered by timestamp descending.
     */
    List<StockActivity> findByItemIdOrderByTimestampDesc(Long itemId);
}
