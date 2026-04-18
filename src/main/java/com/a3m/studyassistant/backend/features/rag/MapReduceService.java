package com.a3m.studyassistant.backend.features.rag;

import com.a3m.studyassistant.backend.features.integration.google.chat.GeminiChatService;
import com.a3m.studyassistant.backend.features.integration.google.chat.dto.AtomicFactResponse;
import com.a3m.studyassistant.backend.features.integration.google.chat.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MapReduceService {
    private final GeminiChatService chatService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MapReduceService(GeminiChatService chatService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.objectMapper = objectMapper;
    }

    public List<String> batchChunks(List<String> chunksContent, int batchSize) {
        List<String> batches = new ArrayList<>();
        for (int i = 0; i < chunksContent.size(); i += batchSize) {
            List<String> batch = chunksContent.subList(i, Math.min(i + batchSize, chunksContent.size()));
            batches.add(String.join("\n\n", batch));
        }
        return batches;
    }

    public String synthesize(List<String> chunksContent, String topic) {
        List<String> batches = batchChunks(chunksContent, 5);
        List<String> atomicListSummary = map(batches, topic);

        return reduce(atomicListSummary, topic);
    }

    public List<String> map(List<String> batches, String topic) {
        String systemPrompt = "Extract all core academic concepts regarding the specific topic. " +
                "Each fact must be a complete, standalone sentence. Stick to the exact wording of text you have been sent. Return strictly JSON.";

        Map<String, Object> atomicFactsSchema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "facts", Map.of(
                                "type", "ARRAY",
                                "items", Map.of("type", "STRING"),
                                "description", "List of technical facts"
                        )
                ),
                "required", List.of("facts")
        );

        List<String> allBullets = new ArrayList<>();

        for (String batch : batches) {
            String userPrompt = "Topic: " + topic + "\nContent: " + batch;
            ChatResponse rawResponse = chatService.generate(userPrompt, systemPrompt, batch, atomicFactsSchema);

            String jsonString = rawResponse.candidates().get(0).content().parts().get(0).text();

            // CLEANUP: Remove potential markdown backticks
            jsonString = jsonString.replaceAll("```json|```", "").trim();

            try {
                AtomicFactResponse response = objectMapper.readValue(jsonString, AtomicFactResponse.class);
                allBullets.addAll(response.facts());
            } catch (Exception e) {
                System.err.println("Failed to parse batch: " + e.getMessage());
            }
        }
        return allBullets;
    }

    public String reduce(List<String> allBullets, String topic) {
        if (allBullets.isEmpty()) return "No content found to summarize.";

        String joinedFacts = String.join("\n* ", allBullets);

        String systemPrompt = "You are an expert academic assistant. Use the provided list of atomic facts " +
                "to create a comprehensive, well-structured summary. Ensure 100% coverage of the facts.";

        String userPrompt = "Topic: " + topic + "\nFacts:\n" + joinedFacts;

        // For the final summary, we might not need a strict JSON schema unless
        // you have a SummaryResponse DTO. If you want plain text, pass null for schema.
        ChatResponse response = chatService.generate(userPrompt, systemPrompt, joinedFacts, null);

        return response.candidates().get(0).content().parts().get(0).text();
    }
}
