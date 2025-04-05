package com.tetracare.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tetracare.chatbot.dto.gemini.GeminiRequest;
import com.tetracare.chatbot.dto.gemini.GeminiRequest.Content;
import com.tetracare.chatbot.dto.gemini.GeminiRequest.Part;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        boolean keyPresent = apiKey != null && !apiKey.isEmpty();
        System.out.println("✅ Gemini API Key Loaded: " + (keyPresent ? "YES" : "NO"));
        if (!keyPresent) {
            System.out.println("❌ Gemini API key is missing or not loaded.");
        }

        webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta1/models/gemini-pro:generateContent")
                .build();
    }

    public String getGeminiReply(String userMessage) {
        Part part = new Part(userMessage);
        Content content = new Content(List.of(part));
        GeminiRequest request = new GeminiRequest(List.of(content));

        System.out.println("📤 Sending request to Gemini API...");
        System.out.println("🔸 Request Body: " + request);

        try {
            String rawJson = webClient.post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), clientResponse -> {
                        System.out.println("❌ Gemini API Error. HTTP Status: " + clientResponse.statusCode());
                        return Mono.error(new RuntimeException("Gemini API responded with error"));
                    })
                    .bodyToMono(String.class)
                    .doOnNext(response -> System.out.println("🟢 Gemini API Raw Response: " + response))
                    .onErrorResume(e -> {
                        System.out.println("🔴 Error during Gemini API call:");
                        e.printStackTrace();
                        return Mono.just("{\"error\": \"Something went wrong\"}");
                    })
                    .block();

            System.out.println("📦 Raw JSON from Gemini: " + rawJson);
            return extractTextFromJson(rawJson);

        } catch (Exception e) {
            System.out.println("🔥 Exception in getGeminiReply():");
            e.printStackTrace();
            return "Gemini API Error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }

    private String extractTextFromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                System.out.println("❌ Gemini API Error Response: " + error.toPrettyString());
                return "Gemini API Error: " + error.path("message").asText("Unknown error");
            }

            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                System.out.println("⚠️ No valid 'candidates' in response.");
                return "Sorry, I didn't receive a valid reply.";
            }

            String reply = candidates.get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text").asText("Sorry, I couldn't understand that.");

            System.out.println("✅ Gemini Final Reply: " + reply);
            return reply;

        } catch (Exception e) {
            System.out.println("🔴 Exception while parsing Gemini JSON:");
            e.printStackTrace();
            System.out.println("🔸 Raw JSON: " + json);
            return "Error parsing Gemini response.";
        }
    }
}
