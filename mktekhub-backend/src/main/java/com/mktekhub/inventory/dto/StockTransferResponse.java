package com.mktekhub.inventory.dto;

import com.mktekhub.inventory.model.StockActivity;

import java.time.LocalDateTime;

/**
 * DTO for stock transfer response.
 */
public class StockTransferResponse {
    private Long activityId;
    private String itemSku;
    private String itemName;
    private String sourceWarehouseName;
    private String destinationWarehouseName;
    private Integer quantityTransferred;
    private Integer previousQuantity;
    private Integer newQuantity;
    private LocalDateTime timestamp;
    private String performedBy;
    private String notes;

    // Constructors
    public StockTransferResponse() {
    }

    /**
     * Convert StockActivity entity to StockTransferResponse DTO.
     */
    public static StockTransferResponse fromEntity(StockActivity activity) {
        StockTransferResponse response = new StockTransferResponse();
        response.setActivityId(activity.getId());
        response.setItemSku(activity.getItemSku());
        response.setItemName(activity.getItem().getName());
        response.setSourceWarehouseName(activity.getSourceWarehouse() != null ?
            activity.getSourceWarehouse().getName() : null);
        response.setDestinationWarehouseName(activity.getDestinationWarehouse() != null ?
            activity.getDestinationWarehouse().getName() : null);
        response.setQuantityTransferred(activity.getQuantityChange());
        response.setPreviousQuantity(activity.getPreviousQuantity());
        response.setNewQuantity(activity.getNewQuantity());
        response.setTimestamp(activity.getTimestamp());
        response.setPerformedBy(activity.getPerformedBy().getUsername());
        response.setNotes(activity.getNotes());
        return response;
    }

    // Getters and Setters
    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getItemSku() {
        return itemSku;
    }

    public void setItemSku(String itemSku) {
        this.itemSku = itemSku;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getSourceWarehouseName() {
        return sourceWarehouseName;
    }

    public void setSourceWarehouseName(String sourceWarehouseName) {
        this.sourceWarehouseName = sourceWarehouseName;
    }

    public String getDestinationWarehouseName() {
        return destinationWarehouseName;
    }

    public void setDestinationWarehouseName(String destinationWarehouseName) {
        this.destinationWarehouseName = destinationWarehouseName;
    }

    public Integer getQuantityTransferred() {
        return quantityTransferred;
    }

    public void setQuantityTransferred(Integer quantityTransferred) {
        this.quantityTransferred = quantityTransferred;
    }

    public Integer getPreviousQuantity() {
        return previousQuantity;
    }

    public void setPreviousQuantity(Integer previousQuantity) {
        this.previousQuantity = previousQuantity;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
