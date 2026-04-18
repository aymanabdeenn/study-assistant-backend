package com.a3m.studyassistant.backend.features.rag;

import com.a3m.studyassistant.backend.features.integration.google.embedding.GoogleEmbeddingService;
import com.a3m.studyassistant.backend.features.resource.Resource;
import com.a3m.studyassistant.backend.features.resource.ResourceChunkRepository;
import com.a3m.studyassistant.backend.features.resource.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class RagService {

    private final ResourceChunkRepository resourceChunkRepository;
    private final GoogleEmbeddingService googleEmbeddingService;
    private final ResourceService resourceService;
    private final MapReduceService mapReduceService;

    @Autowired
    public RagService(ResourceChunkRepository resourceChunkRepository, GoogleEmbeddingService googleEmbeddingService, ResourceService resourceService, MapReduceService mapReduceService) {
        this.resourceChunkRepository = resourceChunkRepository;
        this.googleEmbeddingService = googleEmbeddingService;
        this.resourceService = resourceService;
        this.mapReduceService = mapReduceService;
    }

    public String getRelevantContext(UUID resourceId, int limit) {
        UUID userId = UUID.fromString("54831360-e278-4b21-bd2f-3764aa232a4c");
        Resource resource = resourceService.getResourceById(userId, resourceId);
        String searchQuery = "Detailed explanation and key concepts of " + resource.getBranch().getTitle();

        float[] queryVector = googleEmbeddingService.getEmbedding(searchQuery);

        String vectorString = formatVectorForPostgres(queryVector);

        List<String> chunksContent = resourceChunkRepository.findSimilarChunks(
                resourceId,
                vectorString,
                limit // Limit to 10 chunks to avoid hitting LLM token limits
        );

        return String.join("\n\n---\n\n", chunksContent);
    }

    public String getFullContext(UUID resourceId, int limit) {
        UUID userId = UUID.fromString("54831360-e278-4b21-bd2f-3764aa232a4c");
        Resource resource = resourceService.getResourceById(userId, resourceId);

        List<String> chunksContent = resourceChunkRepository.findAllContentByResourceId(resourceId);
        return mapReduceService.synthesize(chunksContent, resource.getBranch().getTopic().getTitle());
    }

    private String formatVectorForPostgres(float[] vector) {
        return Arrays.toString(vector).replace(" ", "");
    }

}
