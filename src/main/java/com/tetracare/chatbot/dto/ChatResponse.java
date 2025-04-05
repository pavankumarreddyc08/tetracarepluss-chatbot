package com.tetracare.chatbot.dto;

public class ChatResponse {
    private String botReply;

    // Default constructor
    public ChatResponse() {
    }

    // Parameterized constructor
    public ChatResponse(String botReply) {
        this.botReply = botReply;
    }

    // Getter
    public String getBotReply() {
        return botReply;
    }

    // Setter
    public void setBotReply(String botReply) {
        this.botReply = botReply;
    }
}
