package com.fasttasker.fast_tasker.web.controller;

import com.fasttasker.fast_tasker.application.service.ConversationService;
import com.fasttasker.fast_tasker.application.dto.conversation.*;
import com.fasttasker.common.config.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// reference: https://dev.to/supernovabirth/building-a-real-time-chatroom-with-spring-boot-and-websockets-3pk3
// but this is to production, the "reference" is like helloWorld in webSockets
@Slf4j
@RestController
public class ChatController {

    private final ConversationService conversationService;
    private final JwtService jwtService;

    public ChatController(ConversationService conversationService, JwtService jwtService) {
        this.conversationService = conversationService;
        this.jwtService = jwtService;
    }

    /**
     * receive message from the client
     * part WEBSOCKETS (real time)
     * @param messageRequest messageRequest
     */
    @MessageMapping("/chat/.send")
    public void sendMessage(@Payload MessageRequest messageRequest, Authentication authentication) {
        // extract ID of the token
        UUID senderId = jwtService.extractTaskerId(authentication);
        conversationService.processAndSendMessage(messageRequest, senderId);
    }

    /**
     * Retrieves the user's chat list
     * @param authentication token
     * @return list of conversations
     */
    @GetMapping("/api/v1/conversations/inbox")
    public ResponseEntity<List<ConversationSummary>> getInbox(Authentication authentication) {
        // extract ID of the token
        UUID taskerId = jwtService.extractTaskerId(authentication);
        log.info("Account Id: {}", taskerId);
        return ResponseEntity.ok(conversationService.getUserInbox(taskerId));
    }

    /**
     * start a new conversation or securely resume an existing one
     * @param request request from frontend
     * @param authentication token
     * @return conversation id
     */
    @PostMapping("api/v1/conversations/start")
    public ResponseEntity<UUID> startChat(
            @RequestBody StartChatRequest request,
            Authentication authentication
    ) {
        UUID participantA = jwtService.extractTaskerId(authentication);  // obtain userId from the token
        UUID participantB = request.targetUserId();

        ConversationRequest secureRequest = new ConversationRequest(
                request.taskId(),
                participantA,
                participantB
        );

        return ResponseEntity.ok(conversationService.startConversation(secureRequest));
    }

    @GetMapping("api/v1/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable UUID conversationId,
            Authentication authentication
    ) {
        // Extract the user ID from the token to ensure authorization
        UUID requesterId = jwtService.extractTaskerId(authentication);
        log.info("requesterId: {}", requesterId);
        List<MessageResponse> response = conversationService.getHistory(conversationId, requesterId);
        return ResponseEntity.ok(response);
    }
}
