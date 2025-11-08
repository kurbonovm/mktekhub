package com.mktekhub.inventory.dto;

/**
 * DTO for simple message responses.
 */
public class MessageResponse {

    private String message;

    // Constructors
    public MessageResponse() {
    }

    public MessageResponse(String message) {
        this.message = message;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
