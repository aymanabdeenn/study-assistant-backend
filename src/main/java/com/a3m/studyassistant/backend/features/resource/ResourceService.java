package com.a3m.studyassistant.backend.features.resource;

import com.a3m.studyassistant.backend.common.exceptions.UnauthorizedException;
import com.a3m.studyassistant.backend.features.ai.orchestration.PromptProviderService;
import com.a3m.studyassistant.backend.features.branch.Branch;
import com.a3m.studyassistant.backend.features.branch.BranchService;
import com.a3m.studyassistant.backend.features.integration.google.chat.GeminiChatService;
import com.a3m.studyassistant.backend.features.integration.google.embedding.GoogleEmbeddingService;
import com.a3m.studyassistant.backend.features.integration.google.embedding.ImageTrackingExtractor;
import com.a3m.studyassistant.backend.features.integration.tika.CustomTikaParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import jakarta.persistence.EntityManager;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.parser.ParseContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@Service
public class ResourceService {

    private final BranchService branchService;
    private final GoogleEmbeddingService googleEmbeddingService;
    private final ResourceChunkService resourceChunkService;
    private final ResourceRepository resourceRepository;
    private final GeminiChatService chatService;
    private final PromptProviderService promptProviderService;
    private final CustomTikaParser customTikaParser;

    private final EntityManager entityManager;

    @Autowired
    public ResourceService(BranchService branchService, GoogleEmbeddingService googleEmbeddingService, ResourceChunkService resourceChunkService, ResourceRepository resourceRepository, GeminiChatService chatService, PromptProviderService promptProviderService, CustomTikaParser customTikaParser, EntityManager entityManager) {
        this.branchService = branchService;
        this.googleEmbeddingService = googleEmbeddingService;
        this.resourceRepository = resourceRepository;
        this.resourceChunkService = resourceChunkService;
        this.chatService = chatService;
        this.promptProviderService = promptProviderService;
        this.customTikaParser = customTikaParser;
        this.entityManager = entityManager;
    }

    public Resource getResourceById(UUID userId, UUID resourceId) {
        Resource resource = resourceRepository.findById(resourceId).orElseThrow(() -> new ResourceNotFoundException("Resource with id " + resourceId + " couldn't be found!"));
        if(!resource.getBranch().getTopic().getUser().getId().equals(userId)) throw new UnauthorizedException("You cannot retrieve resources you do not own.");
        return resource;
    }

    public Resource getResourceByUrl(String url) {
        Resource resource = resourceRepository.findByUrl(url);
        return resource;
    }

    public List<Resource> getResourcesListForBranch(UUID userId, UUID branchId) {
        Branch branch = branchService.getBranchById(userId, branchId);
        return resourceRepository.findByBranchId(branchId);
    }

    @Transactional
    public Resource createResource(UUID userId, UUID branchId, String title, String url, String type, Float size) {
        if(resourceRepository.existsByBranchIdAndTitle(branchId, title)) {
            throw new IllegalArgumentException("A file named '" + title + "' already exists in this branch!");
        }

        Branch branch = branchService.getBranchById(userId, branchId);
        Resource resource = new Resource(title, url, type, size, branch);
        branch.addResource(resource);
        return resourceRepository.save(resource);
    }

    @Transactional
    public void deleteResource(UUID userId, UUID resourceId) {
        Resource resource = resourceRepository.findById(resourceId).orElseThrow(() -> new ResourceNotFoundException("Resource with id " + resourceId + " couldn't be found!"));
        if(!resource.getBranch().getTopic().getUser().getId().equals(userId)) throw new UnauthorizedException("You do not have permissions to this resource.");

        resource.getBranch().removeResource(resource);
        resourceRepository.delete(resource);
    }

    @Transactional
    public Resource modifyResource(UUID userId, UUID resourceId, String title, String url, String type, Float size) {
        Resource resource = resourceRepository.findById(resourceId).orElseThrow(() -> new ResourceNotFoundException("Resource with id " + resourceId + " couldn't be found!"));
        if(!resource.getBranch().getTopic().getUser().getId().equals(userId)) throw new UnauthorizedException("You do not have permissions to modify this resource.");

        if (title != null && !title.equals(resource.getTitle())) {
            UUID branchId = resource.getBranch().getId();
            if (resourceRepository.existsByBranchIdAndTitle(branchId, title)) {
                throw new IllegalArgumentException("A file named '" + title + "' already exists in this branch!");
            }
            resource.setTitle(title);
        }
        if(url != null) resource.setUrl(url);
        if(type != null) resource.setType(type);
        if(size != null) resource.setSize(size);

        return resourceRepository.save(resource);
    }

    @Async
    public void processResource(UUID resourceId, String fileUrl) {
        Resource resource = null;
        int maxAttempts = 6;
        int attempts = 0;

        while (resource == null && attempts < maxAttempts) {
            resource = resourceRepository.findById(resourceId).orElse(null);
            if (resource == null) {
                attempts++;
                System.out.println("Webhook waiting for database transaction commit... Attempt " + attempts);

                entityManager.clear();
                try { Thread.sleep(2500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            }
        }

        if (resource == null) {
            System.err.println("Critical: Webhook timed out waiting for database row creation for URL: " + fileUrl);
            return;
        }


        try {
            resource.setProcessingStatus(ResourceStatus.PROCESSING);
            resourceRepository.save(resource);

            ParseContext context = new ParseContext();
            context.set(EmbeddedDocumentExtractor.class, new ImageTrackingExtractor(chatService, promptProviderService));

            URL url = URI.create(fileUrl).toURL();
            try (InputStream fileStream = url.openStream()) {
                List<Document> documents = customTikaParser.parse(fileStream, context);
                var splitter = DocumentSplitters.recursive(1000, 150);
                List<TextSegment> segments = splitter.splitAll(documents);

                for (int i = 0; i < segments.size(); i++) {
                    TextSegment segment = segments.get(i);
                    String text = segment.text();
                    String pageNumberStr = segment.metadata().getString("page_number");
                    int pageNumber = (pageNumberStr != null) ? Integer.parseInt(pageNumberStr) : 1;

                    float[] vector = googleEmbeddingService.getEmbedding(text);

                    // Safe transaction boundary execution here
                    resourceChunkService.saveChunk(text, pageNumber, vector, i, resource);

                    Thread.sleep(2000);
                }
            }

            // Success transaction state update
            resource.setProcessingStatus(ResourceStatus.DONE);
            resource.setErrorMessage(null);
            resource.setUrl(fileUrl);
            resourceRepository.save(resource);
            System.out.println("Asynchronous processing completely finished for resource ID: " + resource.getId());

        } catch (Exception e) {
            System.err.println("Critical error running async pipeline for file: " + fileUrl);
            e.printStackTrace();

            // Error transaction state update
            resource.setProcessingStatus(ResourceStatus.ERROR);
            resource.setErrorMessage(e.getMessage() != null ? e.getMessage() : "Unknown execution error during parsing.");
            resourceRepository.save(resource);
        }
    }

}
