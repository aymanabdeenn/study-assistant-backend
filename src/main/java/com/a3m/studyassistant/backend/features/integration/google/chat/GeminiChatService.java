package com.a3m.studyassistant.backend.features.integration.google.chat;

import com.a3m.studyassistant.backend.features.integration.google.chat.dto.ChatResponse;
import com.a3m.studyassistant.backend.features.integration.google.chat.dto.GeminiChatRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
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
                        GeminiChatRequest.Part.ofText(userPrompt),
                        GeminiChatRequest.Part.ofText(contextText)
                )
        );

        // 2. Create the System Instruction Content
        GeminiChatRequest.Content systemInstruction = new GeminiChatRequest.Content(
                "system",
                List.of(GeminiChatRequest.Part.ofText(systemPrompt))
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

        try {
            return objectMapper.readValue(jsonString, ChatResponse.class);
        } catch (JsonProcessingException e) {
            // Log the raw JSON so you can see WHY it failed
            System.err.println("Failed to map Gemini response. Raw JSON: " + jsonString);
            throw new RuntimeException("AI response mapping failed", e);
        }
    }

    public String describeImage(byte[] imageBytes, String mimeType, String systemPrompt) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // 1. Create content with both instructions and the image
        GeminiChatRequest.Content userContent = new GeminiChatRequest.Content(
                "user",
                List.of(
                        GeminiChatRequest.Part.ofText("Describe this technical image for a study guide."),
                        GeminiChatRequest.Part.ofImage(mimeType, base64Image)
                )
        );

        GeminiChatRequest.Content systemInstruction = new GeminiChatRequest.Content(
                "system",
                List.of(GeminiChatRequest.Part.ofText(systemPrompt))
        );

        // 2. Build request (usually plain text for descriptions, so mimeType is text/plain)
        GeminiChatRequest request = new GeminiChatRequest(
                List.of(userContent),
                systemInstruction,
                new GeminiChatRequest.GenerationConfig("text/plain", 0.4, null)
        );

        // 3. Fire request (same restClient logic as your other method)
        String jsonString = restClient.post()
                .uri(uri -> uri.queryParam("key", apiKey).build())
                .body(request)
                .retrieve()
                .body(String.class);

        return extractTextFromResponse(jsonString);
    }

    public String extractTextFromResponse(String jsonString) {
        try {
            ChatResponse response = objectMapper.readValue(jsonString, ChatResponse.class);

            // Safety check: Ensure the response actually has content
            if (response.candidates() != null && !response.candidates().isEmpty()) {
                return response.candidates().get(0).content().parts().get(0).text();
            }

            return "No description generated for this image.";
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AI response mapping for image interpretation failed", e);
        }
    }

}
