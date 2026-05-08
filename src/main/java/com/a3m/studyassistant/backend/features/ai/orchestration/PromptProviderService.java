package com.a3m.studyassistant.backend.features.ai.orchestration;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class PromptProviderService {

    private final Map<String, String> prompts = new HashMap<>();

    @PostConstruct
    public void init() throws IOException {
        prompts.put("quiz_system", loadPrompt("quiz_system_prompt.txt"));
        prompts.put("map_facts_system", loadPrompt("map_facts_system_prompt.txt"));
        prompts.put("reduce_facts_system", loadPrompt("reduce_facts_system_prompt.txt"));
        prompts.put("summary_system", loadPrompt("summary_system_prompt.txt"));
        prompts.put("parse_image_system", loadPrompt("parse_image_system_prompt.txt"));
        prompts.put("conversation_system", loadPrompt("conversation_system_prompt.txt"));
    }

    public String loadPrompt(String fileName) throws IOException {
        try(InputStream is = getClass().getResourceAsStream("/ai_prompts/" + fileName)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public String getPrompt(String key) {
        if (!prompts.containsKey(key)) {
            throw new IllegalArgumentException("Requested prompt key [" + key + "] does not exist!");
        }
        return prompts.get(key);
    }

}
