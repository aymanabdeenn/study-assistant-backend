package com.a3m.studyassistant.backend.features.chat;

import com.a3m.studyassistant.backend.features.ai.orchestration.PromptProviderService;
import com.a3m.studyassistant.backend.features.chat.dto.ConversationAgentResponse;
import com.a3m.studyassistant.backend.features.integration.google.chat.GeminiChatService;
import com.a3m.studyassistant.backend.features.integration.google.chat.dto.ChatResponse;
import com.a3m.studyassistant.backend.features.rag.RagService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChatService {
    private final GeminiChatService geminiChatService;
    private final RagService ragService;
    private final ObjectMapper objectMapper;
    private final PromptProviderService promptProviderService;

    @Autowired
    public ChatService(GeminiChatService geminiChatService, RagService ragService, ObjectMapper objectMapper, PromptProviderService promptProviderService) {
        this.geminiChatService = geminiChatService;
        this.ragService = ragService;
        this.objectMapper = objectMapper;
        this.promptProviderService = promptProviderService;
    }

    public ConversationAgentResponse askAgent(UUID resourceId, String message) {
        String relevantChunksContent = ragService.getRelevantContext(resourceId, message, 10);
        String userPrompt = "### SOURCE DATA\n" +
                "The following excerpts are from the student's uploaded study materials:\n" +
                "---------------------\n" +
                relevantChunksContent + "\n" +
                "---------------------\n\n" +
                "### STUDENT QUESTION\n" +
                message + "\n\n" +
                "### RESPONSE GUIDELINE\n" +
                "Identify the answer in the Source Data above. If the answer is present, explain it clearly. If it is missing, follow the protocol for missing information.";
        ChatResponse response = geminiChatService.generate(message, promptProviderService.getPrompt("conversation_system"), relevantChunksContent, null);

        try {
            String responseStr = response.candidates().get(0).content().parts().get(0).text();
            System.out.println(responseStr);
            return objectMapper.readValue(responseStr, ConversationAgentResponse.class);
        }
        catch(JsonProcessingException ex) {
            System.out.println("An error occurred while trying to retain the agent's response");
            throw new RuntimeException("AI response for the question failed.", ex);
        }
    }
}
