package com.a3m.studyassistant.backend.features.integration.google.chat;

import com.a3m.studyassistant.backend.features.integration.google.chat.dto.ChatResponse;
import com.a3m.studyassistant.backend.features.integration.google.chat.dto.GeminiChatRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiChatService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${google.api.key}")
    private String apiKey;

    @Autowired
    public GeminiChatService(
             RestClient.Builder restClientBuilder
            , @Value("${google.chat.url}") String GOOGLE_CHAT_URL
             , ObjectMapper objectMapper
    ) {
        this.restClient = restClientBuilder.baseUrl(GOOGLE_CHAT_URL).build();
        this.objectMapper = objectMapper;
    }

    public ChatResponse generate(String userPrompt, String systemPrompt, String contextText, Map<String, Object> responseSchema) {

        // 1. Combine user prompt and context into one Content turn
        GeminiChatRequest.Content userContent = new GeminiChatRequest.Content(
                "user",
                List.of(
                        new GeminiChatRequest.Part(userPrompt),
                        new GeminiChatRequest.Part(contextText)
                )
        );

        // 2. Create the System Instruction Content
        GeminiChatRequest.Content systemInstruction = new GeminiChatRequest.Content(
                "system",
                List.of(new GeminiChatRequest.Part(systemPrompt))
        );

        // 3. Assemble the Request
        GeminiChatRequest request = new GeminiChatRequest(
                List.of(userContent),
                systemInstruction,
                new GeminiChatRequest.GenerationConfig("application/json", 0.7, responseSchema)
        );


        // 4. Fire the request
        String jsonString = restClient.post()
                .uri(uri -> uri.queryParam("key", apiKey).build())
                .body(request)
                .retrieve()
                .body(String.class);

        System.out.println("ChatResponse json string: " + jsonString);

        try {
            return objectMapper.readValue(jsonString, ChatResponse.class);
        } catch (JsonProcessingException e) {
            // Log the raw JSON so you can see WHY it failed
            System.err.println("Failed to map Gemini response. Raw JSON: " + jsonString);
            throw new RuntimeException("AI response mapping failed", e);
        }
    }

}
