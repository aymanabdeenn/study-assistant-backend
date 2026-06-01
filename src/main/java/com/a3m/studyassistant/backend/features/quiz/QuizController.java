package com.a3m.studyassistant.backend.features.quiz;

import com.a3m.studyassistant.backend.features.integration.google.chat.dto.QuizResponse;
import com.a3m.studyassistant.backend.features.rag.RagService;
import com.a3m.studyassistant.backend.features.resource.Resource;
import com.a3m.studyassistant.backend.features.resource.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/quizzes")
public class QuizController {
    private final QuizService quizService;
    private final RagService ragService;
    private final ResourceService resourceService;

    @Autowired
    public QuizController(QuizService quizService, RagService ragService, ResourceService resourceService) {
        this.quizService = quizService;
        this.ragService = ragService;
        this.resourceService = resourceService;
    }

    @PostMapping("/{resourceId}")
    public QuizResponse generateQuiz(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID resourceId, @RequestBody QuizParametersDTO dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Resource resource = resourceService.getResourceById(userId, resourceId);
        String atomicSummary =  ragService.getFullContext(userId, resourceId, 15);

//       return quizService.generateQuiz(atomicSummary, "ADVANCED", 15, resource.getBranch().getTitle());
        return quizService.generateQuiz(atomicSummary, dto.difficulty(), dto.numOfQuestions(), resource.getBranch().getTitle());
    }
}
