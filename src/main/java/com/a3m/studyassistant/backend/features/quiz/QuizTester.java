package com.a3m.studyassistant.backend.features.quiz;

import com.a3m.studyassistant.backend.features.integration.google.chat.dto.QuizResponse;
import com.a3m.studyassistant.backend.features.integration.google.embedding.GoogleEmbeddingService;
import com.a3m.studyassistant.backend.features.resource.ResourceChunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

//@Component
public class QuizTester implements CommandLineRunner {

    private final ResourceChunkRepository resourceChunkRepository;
    private final QuizService quizService;
    private final GoogleEmbeddingService googleEmbeddingService;

    @Autowired
    public QuizTester(ResourceChunkRepository resourceChunkRepository, QuizService quizService, GoogleEmbeddingService googleEmbeddingService) {
        this.resourceChunkRepository = resourceChunkRepository;
        this.quizService = quizService;
        this.googleEmbeddingService = googleEmbeddingService;
    }

    @Override
    public void run(String... args) throws Exception {
        UUID resourceId = UUID.fromString("b082f6cd-a14e-4743-aeb6-2d0390031127");

        String topic = "Python Introduction";
        // 1. Create a "Query" for embedding
        String searchQuery = "Detailed explanation and key concepts of " + topic;

        // 2. Convert the query to a vector (Using your EmbeddingService)
        float[] queryVector = googleEmbeddingService.getEmbedding(searchQuery);

        String vectorString = Arrays.toString(queryVector).replace(" ", "");

        // 3. Use your custom Repo method to find the top 5-10 chunks
        List<String> chunksContent = resourceChunkRepository.findSimilarChunks(
                resourceId,
                vectorString,
                1 // Limit to 10 chunks to avoid hitting LLM token limits
        );

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
