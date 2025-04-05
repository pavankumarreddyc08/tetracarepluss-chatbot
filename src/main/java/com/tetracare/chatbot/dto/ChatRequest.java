package com.tetracare.chatbot.dto;

public class ChatRequest {
    private String message;

    // Default constructor
    public ChatRequest() {
    }

    // Parameterized constructor
    public ChatRequest(String message) {
        this.message = message;
    }

    // Getter
    public String getMessage() {
        return message;
    }

    // Setter
    public void setMessage(String message) {
        this.message = message;
    }
}
