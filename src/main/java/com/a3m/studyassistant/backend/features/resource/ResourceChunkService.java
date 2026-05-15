package com.a3m.studyassistant.backend.features.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ResourceChunkService {
    private final ResourceChunkRepository resourceChunkRepository;

    @Autowired
    public ResourceChunkService(ResourceChunkRepository resourceChunkRepository) {
        this.resourceChunkRepository = resourceChunkRepository;
    }

    public List<ResourceChunk> getChunksList(UUID resourceId, String queryEmbedding, int limit) {
        return resourceChunkRepository.findSimilarChunks(resourceId, queryEmbedding, limit);
    }

    @Transactional
    public void saveChunk(String text, int pageNumber, float[] vector, int idx, Resource resource) {
        ResourceChunk chunk = new ResourceChunk();
        chunk.setContent(text);
        chunk.setPageNumber(pageNumber);
        chunk.setEmbedding(vector);
        chunk.setChunkIndex((long) idx);
        chunk.setResource(resource);
        resource.addChunk(chunk);
        resourceChunkRepository.save(chunk);
    }

}
