package com.fasttasker.fast_tasker.web.controller;

import com.fasttasker.fast_tasker.application.dto.conversation.StartChatRequest;
import com.fasttasker.fast_tasker.application.service.ConversationService;
import com.fasttasker.common.config.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Transactional
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConversationService conversationService;

    @MockBean
    private JwtService jwtService;

    private UUID taskerId;

    @BeforeEach
    void setUp() {
        taskerId = UUID.randomUUID();
        when(jwtService.extractTaskerId(any(Authentication.class))).thenReturn(taskerId);
    }

    @Test
    @WithMockUser
    void shouldGetInboxSuccess() throws Exception {
        when(conversationService.getUserInbox(taskerId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/conversations/inbox")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldStartChatSuccess() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        StartChatRequest request = new StartChatRequest(taskId, targetUserId);

        when(conversationService.startConversation(any())).thenReturn(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/conversations/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldGetMessagesSuccess() throws Exception {
        UUID conversationId = UUID.randomUUID();
        when(conversationService.getHistory(conversationId, taskerId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}