package com.a3m.studyassistant.backend.features.chat;

import com.a3m.studyassistant.backend.features.chat.dto.ConversationAgentResponse;
import com.a3m.studyassistant.backend.features.chat.dto.ConversationUserRequest;
import com.a3m.studyassistant.backend.features.resource.Resource;
import com.a3m.studyassistant.backend.features.resource.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    private final ResourceService resourceService;

    @Autowired
    public ChatController(ChatService chatService, ResourceService resourceService) {
        this.chatService = chatService;
        this.resourceService = resourceService;
    }

    @PostMapping
    public ResponseEntity<?> getResponseFromAgent(@AuthenticationPrincipal Jwt jwt, @RequestBody ConversationUserRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        UUID resourceId = UUID.fromString(request.resourceId());
        Resource resource = resourceService.getResourceById(userId, resourceId);

        ConversationAgentResponse response = chatService.askAgent(userId, resource.getId(), request.message());

        return ResponseEntity.ok(response);
    }
}
