package com.a3m.studyassistant.backend.features.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ResourceChunkService {
    private final ResourceChunkRepository resourceChunkRepository;

    @Autowired
    public ResourceChunkService(ResourceChunkRepository resourceChunkRepository) {
        this.resourceChunkRepository = resourceChunkRepository;
    }

    public List<ResourceChunkProjection> getChunksList(UUID resourceId, String queryEmbedding, int limit) {
        return resourceChunkRepository.findSimilarChunks(resourceId, queryEmbedding, limit);
    }

    @Transactional
    public void saveChunk(String content, int pageNumber, float[] embedding, int chunkIndex, Resource resource) {
        UUID newChunkId = UUID.randomUUID();

        // Convert the float[] array into a clean Postgres vector string representation: "[0.23, -0.11, 0.45...]"
        String embeddingVectorString = Arrays.toString(embedding);

        // Execute the manual native insert statement
        resourceChunkRepository.insertChunkWithVector(
                newChunkId,
                resource.getId(),
                chunkIndex,
                pageNumber,
                content,
                embeddingVectorString
        );
    }

}
