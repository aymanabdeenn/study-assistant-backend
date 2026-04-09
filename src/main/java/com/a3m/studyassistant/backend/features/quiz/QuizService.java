package com.a3m.studyassistant.backend.features.quiz;

import com.a3m.studyassistant.backend.features.integration.google.chat.GeminiChatService;
import com.a3m.studyassistant.backend.features.integration.google.chat.dto.ChatResponse;
import com.a3m.studyassistant.backend.features.integration.google.chat.dto.QuizResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class QuizService{

    private final GeminiChatService chatService;
    private final ObjectMapper objectMapper;

    @Autowired
    public QuizService(GeminiChatService geminiChatService, ObjectMapper objectMapper) {
        this.chatService = geminiChatService;
        this.objectMapper = objectMapper;
    }

    public QuizResponse generateQuiz(List<String> chunksContent, String topic) {
        String contextText = String.join("\n\n", chunksContent);
        String userPrompt = "Topic: " + topic + "\nContext:\n" + contextText;
        String systemPrompt = "You are a teacher Create 5 multiple choice questions.";
        Map<String, Object> quizSchema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "quizTitle", Map.of("type", "STRING"),
                        "questions", Map.of(
                                "type", "ARRAY",
                                "items", Map.of(
                                        "type", "OBJECT",
                                        "properties", Map.of(
                                                "questionText", Map.of("type", "STRING"),
                                                "options", Map.of(
                                                        "type", "ARRAY",
                                                        "items", Map.of("type", "STRING")
                                                ),
                                                "correctOptionIndex", Map.of("type", "INTEGER"),
                                                "explanation", Map.of("type", "STRING") // Why it's correct
                                        ),
                                        "required", List.of("questionText", "options", "correctOptionIndex")
                                )
                        )
                ),
                "required", List.of("quizTitle", "questions")
        );

        // 2. Get the RAW JSON string from Google
        ChatResponse rawResponse = chatService.generate(userPrompt, systemPrompt, contextText, quizSchema);

        if (rawResponse != null && rawResponse.usageMetadata() != null) {
            System.out.println("Tokens used: " + rawResponse.usageMetadata().totalTokenCount());
        } else {
            System.out.println("Metadata not available.");
        }

        // 3. Extract the JSON string from the "Speech Bubble"
        String jsonString = rawResponse.candidates().get(0).content().parts().get(0).text();

        System.out.println("QuizResponse json string: " + jsonString);

        // 4. MAP it to your clean Domain Object
        try {
            return objectMapper.readValue(jsonString, QuizResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AI returned invalid JSON for Quiz", e);
        }
    }

}
