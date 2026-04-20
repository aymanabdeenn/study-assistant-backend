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
    }

    public String loadPrompt(String fileName) throws IOException {
        try(InputStream is = getClass().getResourceAsStream("/ai_prompts/" + fileName)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public String getPrompt(String key) {
        return prompts.containsKey(key) ? prompts.get(key) : "You are passing the wrong prompt name";
    }

}
