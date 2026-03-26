package com.a3m.studyassistant.backend.features.topic;

import com.a3m.studyassistant.backend.features.topic.dto.TopicCreationDTO;
import com.a3m.studyassistant.backend.features.topic.dto.TopicModificationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/topic")
public class TopicController {

    private final TopicService topicService;

    @Autowired
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/{topicId}")
    public ResponseEntity<?> getTopic(@PathVariable UUID topicId) {
        Topic topic = topicService.getTopicById(topicId);
        return ResponseEntity.ok(topic);
    }

    @GetMapping
    public ResponseEntity<?> getTopicsForUser(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        List<Topic> topics = topicService.getTopicsListForUser(userId);
        return ResponseEntity.ok(topics);
    }

    @PostMapping
    public ResponseEntity<?> createTopic(@AuthenticationPrincipal Jwt jwt, @RequestBody TopicCreationDTO dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Topic topic = topicService.createTopic(userId, dto.getTitle(), dto.getDescription());
        return ResponseEntity.ok(topic);
    }

    @PatchMapping("/modify/{topicId}")
    public ResponseEntity<?> modifyTopic(@PathVariable UUID topicId, @AuthenticationPrincipal Jwt jwt, @RequestBody TopicModificationDTO dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Topic topic = topicService.modifyTopic(topicId, userId, dto.getTitle(), dto.getDescription());
        return ResponseEntity.ok(topic);
    }

    @DeleteMapping("/{topicId}")
    public ResponseEntity<?> deleteTopic(@PathVariable UUID topicId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        topicService.deleteTopic(topicId, userId);
        return ResponseEntity.ok("Topic with id " + topicId + " has been deleted successfully!");
    }

}
