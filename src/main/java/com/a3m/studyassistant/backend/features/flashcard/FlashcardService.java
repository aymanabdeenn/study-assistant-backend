package com.a3m.studyassistant.backend.features.flashcard;

import com.a3m.studyassistant.backend.features.ai.orchestration.PromptProviderService;
import com.a3m.studyassistant.backend.features.integration.google.chat.GeminiChatService;
import com.a3m.studyassistant.backend.features.integration.google.chat.dto.ChatResponse;
import com.a3m.studyassistant.backend.features.integration.google.chat.dto.FlashcardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Map;

@Service
public class FlashcardService {

    private final GeminiChatService chatService;
    private final PromptProviderService promptProviderService;
    private final ObjectMapper objectMapper;

    private final Map<String, Object> flashcardSchema;

    @Autowired
    public FlashcardService(GeminiChatService chatService, PromptProviderService promptProviderService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.promptProviderService = promptProviderService;
        this.objectMapper = objectMapper;

        InputStream is = getClass().getResourceAsStream("/ai_schemas/flashcard_schema.json");
        this.flashcardSchema = objectMapper.readValue(is, new TypeReference<Map<String, Object>>() {});
    }

    public FlashcardResponse generateFlashcard(String chunksContent, String topic, int count) {
        String userPrompt = "Topic: " + topic + "\nContext:\n" + chunksContent;

        String systemPrompt = promptProviderService.getPrompt("flashcard_system");
        String specializedInstruction = systemPrompt.replace("{{count}}", String.valueOf(count));

        ChatResponse rawResponse = chatService.generate(userPrompt, specializedInstruction, chunksContent, flashcardSchema);

        if(rawResponse != null && rawResponse.usageMetadata() != null) {
            System.out.println("Tokens used: " + rawResponse.usageMetadata().totalTokenCount());
        }
        else {
            System.out.println("Metadata not available.");
        }

        String jsonString = rawResponse.candidates().get(0).content().parts().get(0).text();

        return objectMapper.readValue(jsonString, FlashcardResponse.class);
    }

}
