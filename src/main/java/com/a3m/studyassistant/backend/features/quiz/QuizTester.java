package com.a3m.studyassistant.backend.features.quiz;

import com.a3m.studyassistant.backend.features.integration.google.chat.dto.QuizResponse;
import com.a3m.studyassistant.backend.features.rag.RagService;
import com.a3m.studyassistant.backend.features.resource.Resource;
import com.a3m.studyassistant.backend.features.resource.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class QuizTester implements CommandLineRunner {

    private final QuizService quizService;
    private final RagService ragService;
    private final ResourceService resourceService;

    @Autowired
    public QuizTester(QuizService quizService, RagService ragService, ResourceService resourceService) {
        this.quizService = quizService;
        this.ragService = ragService;
        this.resourceService = resourceService;
    }

    @Override
    public void run(String... args) throws Exception {
        UUID resourceId = UUID.fromString("edbc68aa-e5a4-40e5-b858-5c9e7954c43f");
        UUID userId = UUID.fromString("54831360-e278-4b21-bd2f-3764aa232a4c");
        Resource resource = resourceService.getResourceById(userId, resourceId);

        String atomicSummary =  ragService.getFullContext(resourceId, 15);

        QuizResponse quiz = quizService.generateQuiz(atomicSummary, "ADVANCED", 15, resource.getBranch().getTopic().getTitle());
        displayQuiz(quiz);
    }

    public void displayQuiz(QuizResponse quiz) {
        String quizTitle = quiz.quizTitle();
        List<QuizResponse.Question> questions = quiz.questions();

        System.out.println("\n\n\n\n");
        int questionCnt = 1;
        for(QuizResponse.Question q : questions) {
            System.out.println(questionCnt++ + "] " + q.questionText());
            int cnt = 1;
            for(String option : q.options()) {
                System.out.println(cnt++ + ") " + option);
            }
            System.out.println("--------------");
            System.out.println(q.correctOptionIndex());
            System.out.println("--------------");
            System.out.println("Explanation: " + q.explanation() + "\n");
        }
    }

}
