package com.fasttasker.fast_tasker.web.controller;

import com.fasttasker.fast_tasker.application.dto.task.*;
import com.fasttasker.fast_tasker.application.service.TaskService;
import com.fasttasker.fast_tasker.config.DataSeeder;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Transactional
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private DataSeeder dataSeeder;

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
    void shouldCreateTaskSuccess() throws Exception {
        when(taskService.createTask(any(), eq(taskerId))).thenReturn(mock(TaskResponse.class));

        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void shouldGetAllPublicActiveTasksSuccess() throws Exception {
        when(taskService.listActiveTasks()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldGetAllMyTasksSuccess() throws Exception {
        when(taskService.listTasksByPoster(taskerId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/tasks/my-tasks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldGetTaskCompleteSuccess() throws Exception {
        UUID taskId = UUID.randomUUID();
        when(taskService.getTaskCompleteById(taskId)).thenReturn(mock(TaskCompleteResponse.class));

        mockMvc.perform(get("/api/v1/tasks/{taskId}", taskId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldCreateOfferSuccess() throws Exception {
        UUID taskId = UUID.randomUUID();
        when(taskService.createOffer(any(), eq(taskId), eq(taskerId))).thenReturn(mock(OfferProfileResponse.class));

        mockMvc.perform(post("/api/v1/tasks/{taskId}/offers", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void shouldGetAllOffersByTaskSuccess() throws Exception {
        UUID taskId = UUID.randomUUID();
        when(taskService.listOffersByTask(taskId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/tasks/{taskId}/offers", taskId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldCreateQuestionSuccess() throws Exception {
        UUID taskId = UUID.randomUUID();
        when(taskService.createQuestion(any(), eq(taskId), eq(taskerId))).thenReturn(mock(QuestionProfileResponse.class));

        mockMvc.perform(post("/api/v1/tasks/{taskId}/questions", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void shouldGetAllQuestionsByTaskSuccess() throws Exception {
        UUID taskId = UUID.randomUUID();
        when(taskService.listQuestionsByTask(taskId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/tasks/{taskId}/questions", taskId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldCreateAnswerSuccess() throws Exception {
        UUID taskId = UUID.randomUUID();
        when(taskService.answerQuestion(any(), eq(taskId), eq(taskerId))).thenReturn(mock(AnswerProfileResponse.class));

        mockMvc.perform(post("/api/v1/tasks/{taskId}/answer", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated());
    }
}