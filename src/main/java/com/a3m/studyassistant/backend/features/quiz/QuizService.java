package com.a3m.studyassistant.backend.features.quiz;

import com.a3m.studyassistant.backend.features.ai.orchestration.PromptProviderService;
import com.a3m.studyassistant.backend.features.integration.google.chat.GeminiChatService;
import com.a3m.studyassistant.backend.features.integration.google.chat.dto.ChatResponse;
import com.a3m.studyassistant.backend.features.integration.google.chat.dto.QuizResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
public class QuizService{

    private final GeminiChatService chatService;
    private final PromptProviderService promptProviderService;
    private final ObjectMapper objectMapper;

    private final Map<String, Object> quizSchema;

    @Autowired
    public QuizService(GeminiChatService geminiChatService, PromptProviderService promptProviderService, ObjectMapper objectMapper) throws IOException {
        this.chatService = geminiChatService;
        this.promptProviderService = promptProviderService;
        this.objectMapper = objectMapper;

        InputStream is = getClass().getResourceAsStream("/ai_schemas/quiz_schema.json");
        this.quizSchema = objectMapper.readValue(is, new TypeReference<Map<String, Object>>() {});
    }

    public QuizResponse generateQuiz(String chunksContent, String difficulty, int numOfQuestions, String topic) {
        String userPrompt = "Topic: " + topic + "\nContext:\n" + chunksContent;

        String systemPrompt = "";
        switch(difficulty) {
            case "FOUNDATIONAL": systemPrompt = promptProviderService.getPrompt("foundational_quiz_system"); break;
            case "INTERMEDIATE": systemPrompt = promptProviderService.getPrompt("intermediate_quiz_system"); break;
            case "ADVANCED": systemPrompt = promptProviderService.getPrompt("advanced_quiz_system"); break;
            default: systemPrompt = promptProviderService.getPrompt("foundational_quiz_system");
        }
        String specializedInstruction = systemPrompt.replace("{{numOfQuestions}}", String.valueOf(numOfQuestions));

        // 2. Get the RAW JSON string from Google
        ChatResponse rawResponse = chatService.generate(userPrompt, specializedInstruction, chunksContent, quizSchema);

        if (rawResponse != null && rawResponse.usageMetadata() != null) {
            System.out.println("Tokens used: " + rawResponse.usageMetadata().totalTokenCount());
        } else {
            System.out.println("Metadata not available.");
        }

        // 3. Extract the JSON string from the "Speech Bubble"
        String jsonString = rawResponse.candidates().get(0).content().parts().get(0).text();

        // 4. MAP it to your clean Domain Object
        try {
            return objectMapper.readValue(jsonString, QuizResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AI returned invalid JSON for Quiz", e);
        }
    }

}
