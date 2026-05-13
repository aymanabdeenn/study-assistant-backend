package com.a3m.studyassistant.backend.features.summary;

import com.a3m.studyassistant.backend.features.ai.orchestration.PromptProviderService;
import com.a3m.studyassistant.backend.features.integration.google.chat.GeminiChatService;
import com.a3m.studyassistant.backend.features.integration.google.chat.dto.ChatResponse;
import com.a3m.studyassistant.backend.features.integration.google.chat.dto.SummaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Map;

@Service
public class SummaryService {

    private final GeminiChatService chatService;
    private final PromptProviderService promptProviderService;
    private ObjectMapper objectMapper;

    private final Map<String, Object> summarySchema;

    @Autowired
    public SummaryService(GeminiChatService chatService, PromptProviderService promptProviderService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.promptProviderService = promptProviderService;
        this.objectMapper = objectMapper;

        InputStream is = getClass().getResourceAsStream("/ai_schemas/summary_schema.json");
        this.summarySchema = objectMapper.readValue(is, new TypeReference<Map<String, Object>>() {});
    }

    public SummaryResponse generateSummary(String chunksContent, float coverage, String topic) {
        String userPrompt = "Topic: " + topic + "\nContext:\n" + chunksContent;

        String systemPrompt = promptProviderService.getPrompt("summary_system");
        String specializedInstruction = systemPrompt.replace("{{coverage}}", String.valueOf(coverage * 100));

        // 2. Get the RAW JSON string from Google
        ChatResponse rawResponse = chatService.generate(userPrompt, specializedInstruction, chunksContent, summarySchema);

        if (rawResponse != null && rawResponse.usageMetadata() != null) {
            System.out.println("Tokens used: " + rawResponse.usageMetadata().totalTokenCount());
        } else {
            System.out.println("Metadata not available.");
        }

        // 3. Extract the JSON string from the "Speech Bubble"
        String jsonString = rawResponse.candidates().get(0).content().parts().get(0).text();

        // 4. MAP it to your clean Domain Object
        return objectMapper.readValue(jsonString, SummaryResponse.class);
    }

}
