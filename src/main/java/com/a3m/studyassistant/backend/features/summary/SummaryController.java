package com.a3m.studyassistant.backend.features.summary;

import com.a3m.studyassistant.backend.features.integration.google.chat.dto.SummaryResponse;
import com.a3m.studyassistant.backend.features.rag.RagService;
import com.a3m.studyassistant.backend.features.resource.Resource;
import com.a3m.studyassistant.backend.features.resource.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/summaries")
public class SummaryController {
    private final SummaryService summaryService;
    private final RagService ragService;
    private final ResourceService resourceService;

    @Autowired
    public SummaryController(SummaryService summaryService, RagService ragService, ResourceService resourceService) {
        this.summaryService = summaryService;
        this.ragService = ragService;
        this.resourceService = resourceService;
    }

    @PostMapping("/{resourceId}")
    public SummaryResponse generateSummary(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID resourceId, @RequestBody SummaryParametersDTO dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Resource resource = resourceService.getResourceById(userId, resourceId);

        String atomicSummary = ragService.getFullContext(resourceId, 15);
//       return summaryService.generateSummary(atomicSummary, (float)0.7, resource.getBranch().getTopic().getTitle());
        return summaryService.generateSummary(atomicSummary, dto.coverage(), resource.getBranch().getTitle());
    }
}
