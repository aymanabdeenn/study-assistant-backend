package com.a3m.studyassistant.backend.features.flashcard;

import com.a3m.studyassistant.backend.features.integration.google.chat.dto.FlashcardResponse;
import com.a3m.studyassistant.backend.features.rag.RagService;
import com.a3m.studyassistant.backend.features.resource.Resource;
import com.a3m.studyassistant.backend.features.resource.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

//@Component
public class FlashcardTester implements CommandLineRunner {

    private final FlashcardService flashcardService;
    private final RagService ragService;
    private final ResourceService resourceService;

    @Autowired
    public FlashcardTester(FlashcardService flashcardService, RagService ragService, ResourceService resourceService) {
        this.flashcardService = flashcardService;
        this.ragService = ragService;
        this.resourceService = resourceService;
    }

    @Override
    public void run(String... args) throws Exception {
        UUID resourceId = UUID.fromString("6df6b4f1-17bd-4dc8-ad6b-90ad03a71c19");
        UUID userId = UUID.fromString("54831360-e278-4b21-bd2f-3764aa232a4c");
        Resource resource = resourceService.getResourceById(userId, resourceId);

        String atomicSummary = ragService.getFullContext(resourceId, 11);

        FlashcardResponse flashcards = flashcardService.generateFlashcard(atomicSummary, "Introduction to python.", 10);

        for(FlashcardDTO card : flashcards.cards()) {
            System.out.println("Front: " + card.front());
            System.out.println("Back: " + card.back());
            System.out.println("Hint: " + card.hint());
            System.out.println("Category: " + card.category());
        }
    }

}
