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
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.parser.ParseContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
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

    @Autowired
    public ResourceService(BranchService branchService, GoogleEmbeddingService googleEmbeddingService, ResourceChunkService resourceChunkService, ResourceRepository resourceRepository, GeminiChatService chatService, PromptProviderService promptProviderService, CustomTikaParser customTikaParser) {
        this.branchService = branchService;
        this.googleEmbeddingService = googleEmbeddingService;
        this.resourceRepository = resourceRepository;
        this.resourceChunkService = resourceChunkService;
        this.chatService = chatService;
        this.promptProviderService = promptProviderService;
        this.customTikaParser = customTikaParser;
    }

    public Resource getResourceById(UUID userId, UUID resourceId) {
        Resource resource = resourceRepository.findById(resourceId).orElseThrow(() -> new ResourceNotFoundException("Resource with id " + resourceId + " couldn't be found!"));
        if(!resource.getBranch().getTopic().getUser().getId().equals(userId)) throw new UnauthorizedException("You cannot retrieve resources you do not own.");
        return resource;
    }

    public List<Resource> getResourcesListForBranch(UUID userId, UUID branchId) {
        Branch branch = branchService.getBranchById(userId, branchId);
        return resourceRepository.findByBranchId(branchId);
    }

    @Transactional
    public Resource createResource(UUID userId, UUID branchId, String title, String url, String type, Float size) {
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

        if(title != null) resource.setTitle(title);
        if(url != null) resource.setUrl(url);
        if(type != null) resource.setType(type);
        if(size != null) resource.setSize(size);

        return resourceRepository.save(resource);
    }

    public void processResource(InputStream fileStream, Resource resource) {
        ParseContext context = new ParseContext();
        context.set(EmbeddedDocumentExtractor.class, new ImageTrackingExtractor(chatService, promptProviderService));

        // 1. Receive the List of Documents (one per page)
        List<Document> documents = customTikaParser.parse(fileStream, context);

        // 2. Split ALL documents at once
        // LangChain4j will handle the list and return segments for all pages
        var splitter = DocumentSplitters.recursive(1000, 150);
        List<TextSegment> segments = splitter.splitAll(documents);

        // 3. Process the segments
        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            String text = segment.text();

            // 🛠️ Pro-Tip: Retrieve the page number from the segment metadata!
            // LangChain4j copies metadata from Document to TextSegment automatically.
            String pageNumberStr = segment.metadata().getString("page_number");
            int pageNumber = (pageNumberStr != null) ? Integer.parseInt(pageNumberStr) : 1;

            float[] vector = googleEmbeddingService.getEmbedding(text);

            // Pass the pageNumber to your saveChunk method if your DB supports it
            resourceChunkService.saveChunk(text, pageNumber,vector, i, resource);

            // Rate limiting for the Embedding API
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            fileStream.close();
        } catch (Exception ex) {
            System.out.println("The input stream couldn't be closed");
        }
    }

}
