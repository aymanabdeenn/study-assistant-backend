package com.a3m.studyassistant.backend.features.flashcard;

import com.a3m.studyassistant.backend.features.integration.google.chat.dto.FlashcardResponse;
import com.a3m.studyassistant.backend.features.rag.RagService;
import com.a3m.studyassistant.backend.features.resource.Resource;
import com.a3m.studyassistant.backend.features.resource.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/flashcards")
public class FlashcardController {
    private final FlashcardService flashcardService;
    private final RagService ragService;
    private final ResourceService resourceService;

    @Autowired
    public FlashcardController(FlashcardService flashcardService, RagService ragService, ResourceService resourceService) {
        this.flashcardService = flashcardService;
        this.ragService = ragService;
        this.resourceService = resourceService;
    }

    @PostMapping("/{resourceId}")
    public FlashcardResponse generateFlashcards(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID resourceId, @RequestBody FlashCardParametersDTO dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Resource resource = resourceService.getResourceById(userId, resourceId);

        String atomicSummary = ragService.getFullContext(resourceId, 11);
//        return flashcardService.generateFlashcard(atomicSummary, "Introduction to python.", 10);
        return flashcardService.generateFlashcard(atomicSummary, resource.getBranch().getTitle(), dto.count());
    }
}
