package com.a3m.studyassistant.backend.features.integration.google.embedding;

import com.a3m.studyassistant.backend.features.integration.google.embedding.dto.EmbeddingRequest;
import com.a3m.studyassistant.backend.features.integration.google.embedding.dto.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class GoogleEmbeddingService {

    private final RestClient restClient;

    @Value("${google.api.key}")
    private String apiKey;

    @Autowired
    public GoogleEmbeddingService(
            RestClient.Builder restClientBuilder
            , @Value("${google.api.url}") String GOOGLE_API_URL
    ) {
        this.restClient = restClientBuilder.baseUrl(GOOGLE_API_URL).build();
    }

    public float[] getEmbedding(String text) {
        EmbeddingRequest request = new EmbeddingRequest(
                "models/gemini-embedding-2-preview", // Updated model
                new EmbeddingRequest.Content(List.of(new EmbeddingRequest.Part(text))),
                1536 // The 1536 dimensionality trick
        );

        EmbeddingResponse response = restClient.post()
                .uri(uri -> uri.queryParam("key", apiKey).build())
                .body(request)
                .retrieve()
                .body(EmbeddingResponse.class);

        return (response != null) ? response.embedding().values() : new float[0];
    }

}
