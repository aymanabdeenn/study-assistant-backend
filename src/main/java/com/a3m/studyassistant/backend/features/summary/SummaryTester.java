package com.a3m.studyassistant.backend.features.summary;

import com.a3m.studyassistant.backend.features.integration.google.chat.dto.QuizResponse;
import com.a3m.studyassistant.backend.features.integration.google.chat.dto.SummaryResponse;
import com.a3m.studyassistant.backend.features.rag.RagService;
import com.a3m.studyassistant.backend.features.resource.Resource;
import com.a3m.studyassistant.backend.features.resource.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

//@Component
public class SummaryTester implements CommandLineRunner {

    private final SummaryService summaryService;
    private final RagService ragService;
    private final ResourceService resourceService;

    @Autowired
    public SummaryTester(SummaryService summaryService, RagService ragService, ResourceService resourceService) {
        this.summaryService = summaryService;
        this.ragService = ragService;
        this.resourceService = resourceService;
    }

    @Override
    public void run(String... args) throws Exception {
        UUID resourceId = UUID.fromString("ff3fdee0-1929-4339-a60f-fa8614610ae0");
        UUID userId = UUID.fromString("54831360-e278-4b21-bd2f-3764aa232a4c");
        Resource resource = resourceService.getResourceById(userId, resourceId);

        String atomicSummary =  ragService.getFullContext(resourceId, 10);

        SummaryResponse summary = summaryService.generateSummary(atomicSummary, (float)0.7, resource.getBranch().getTopic().getTitle());
        System.out.println(summary.title());
        System.out.println("\n\n");
        System.out.println(summary.content());
        System.out.println("\n\nKey Takeaways:");
        System.out.println(summary.keyTakeaways());
    }
}
