package com.a3m.studyassistant.backend.features.rag;

import com.a3m.studyassistant.backend.features.integration.google.embedding.GoogleEmbeddingService;
import com.a3m.studyassistant.backend.features.resource.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RagService {

    private final ResourceChunkRepository resourceChunkRepository;
    private final GoogleEmbeddingService googleEmbeddingService;
    private final ResourceReductionService resourceReductionService;
    private final ResourceService resourceService;
    private final MapReduceService mapReduceService;

    private final ConcurrentMap<UUID, Object> fileLocks = new ConcurrentHashMap<>();

    @Autowired
    public RagService(ResourceChunkRepository resourceChunkRepository, GoogleEmbeddingService googleEmbeddingService, ResourceReductionService resourceReductionService,ResourceService resourceService, MapReduceService mapReduceService) {
        this.resourceChunkRepository = resourceChunkRepository;
        this.googleEmbeddingService = googleEmbeddingService;
        this.resourceReductionService = resourceReductionService;
        this.resourceService = resourceService;
        this.mapReduceService = mapReduceService;
    }

    public String getRelevantContext(UUID resourceId, String message,int limit) {
        UUID userId = UUID.fromString("54831360-e278-4b21-bd2f-3764aa232a4c");
        Resource resource = resourceService.getResourceById(userId, resourceId);

        float[] queryVector = googleEmbeddingService.getEmbedding(message);

        String vectorString = formatVectorForPostgres(queryVector);

        List<ResourceChunk> chunks = resourceChunkRepository.findSimilarChunks(
                resourceId,
                vectorString,
                limit // Limit to 10 chunks to avoid hitting LLM token limits
        );

        StringBuilder contextBuilder = new StringBuilder();
        for(ResourceChunk chunk: chunks) {
            contextBuilder.append("[Source Page: ")
                    .append(chunk.getPageNumber())
                    .append("]\n")
                    .append(chunk.getContent())
                    .append("\n\n");
        }

        return contextBuilder.toString();
    }

    public String getFullContext(UUID userId, UUID resourceId, int limit) {
        Resource resource = resourceService.getResourceById(userId, resourceId);

        // 1. Initial Fast Check
        ResourceReduction reduction = resourceReductionService.getReductionByResourceId(resourceId);
        if (reduction != null) {
            return reduction.getFinalSummaryString();
        }

        // 2. Thread Safety Lock: Compute an isolated mutex lock unique to this specific file ID
        Object lock = fileLocks.computeIfAbsent(resourceId, k -> new Object());

        synchronized (lock) {
            // 3. Double-Check Pattern: Did another thread just finish generating this while we were waiting?
            reduction = resourceReductionService.getReductionByResourceId(resourceId);
            if (reduction != null) {
                return reduction.getFinalSummaryString();
            }

            // 4. Cache Miss confirmed - Run heavy pipeline safely inside the lock boundaries
            List<String> chunksContent = resourceChunkRepository.findAllContentByResourceId(resourceId);
            String atomicSummary = mapReduceService.synthesize(chunksContent, resource.getBranch().getTitle());

            resourceReductionService.createReduction(resource, atomicSummary);

            // Clean up the lock memory from RAM since processing is done
            fileLocks.remove(resourceId);

            return atomicSummary;
        }
    }

    private String formatVectorForPostgres(float[] vector) {
        return Arrays.toString(vector).replace(" ", "");
    }

}
