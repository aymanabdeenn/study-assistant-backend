package com.a3m.studyassistant.backend.features.quiz;

import com.a3m.studyassistant.backend.features.integration.google.chat.dto.QuizResponse;
import com.a3m.studyassistant.backend.features.rag.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class QuizTester implements CommandLineRunner {

    private final QuizService quizService;
    private final RagService ragService;

    @Autowired
    public QuizTester(QuizService quizService, RagService ragService) {
        this.quizService = quizService;
        this.ragService = ragService;
    }

    @Override
    public void run(String... args) throws Exception {
        UUID resourceId = UUID.fromString("ff3fdee0-1929-4339-a60f-fa8614610ae0");

        String chunksContent =  ragService.getRelevantContext(resourceId, 5);

        QuizResponse quiz = quizService.generateQuiz(chunksContent, "python Introduction");
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
