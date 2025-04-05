package com.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.tetracare.chatbot.service.GeminiService;
import com.tetracare.chatbot.dto.ChatRequest;
import com.tetracare.chatbot.dto.ChatResponse;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String userMessage = request.getMessage();
        System.out.println("📩 Incoming user message: " + userMessage);

        if (userMessage == null || userMessage.trim().isEmpty()) {
            System.out.println("⚠️ User message was empty or null.");
            return ResponseEntity.badRequest().body(new ChatResponse("Please enter a valid message."));
        }

        try {
            String reply = geminiService.getGeminiReply(userMessage);
            System.out.println("🤖 Gemini reply: " + reply);
            ChatResponse response = new ChatResponse(reply);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("🔥 Error in ChatController while getting reply from GeminiService:");
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ChatResponse("Internal Server Error. Please try again later."));
        }
    }
}
