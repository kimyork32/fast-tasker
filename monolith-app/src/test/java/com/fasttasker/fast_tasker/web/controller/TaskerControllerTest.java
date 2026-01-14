package com.fasttasker.fast_tasker.web.controller;

import com.fasttasker.fast_tasker.application.dto.task.AssignTaskerResponse;
import com.fasttasker.fast_tasker.application.dto.tasker.TaskerResponse;
import com.fasttasker.fast_tasker.application.service.TaskerService;
import com.fasttasker.fast_tasker.config.DataSeeder;
import com.fasttasker.common.config.JwtService;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Transactional
class TaskerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskerService taskerService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private DataSeeder dataSeeder;

    private UUID accountId;
    private UUID taskerId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        taskerId = UUID.randomUUID();

        when(jwtService.extractAccountId(any(Authentication.class))).thenReturn(accountId);
        when(jwtService.extractTaskerId(any(Authentication.class))).thenReturn(taskerId);
    }

    @Test
    @WithMockUser
    void shouldInitialRegisterSuccess() throws Exception {
        TaskerResponse taskerResponse = mock(TaskerResponse.class);
        when(taskerResponse.id()).thenReturn(taskerId.toString());
        when(taskerResponse.accountId()).thenReturn(accountId.toString());

        when(taskerService.registerTasker(any())).thenReturn(taskerResponse);
        when(jwtService.generateToken(any(), any(), eq(true))).thenReturn("new-jwt-token");

        String jsonRequest = """
                {
                    "accountId": "ignored",
                    "profile": {
                        "firstName": "Test",
                        "lastName": "User"
                    }
                }
                """;

        mockMvc.perform(put("/api/v1/tasker/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldGetTaskerByIdSuccess() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        when(taskerService.getById(targetUserId)).thenReturn(mock(TaskerResponse.class));

        mockMvc.perform(get("/api/v1/tasker/user/{userId}", targetUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldGetTaskerMeSuccess() throws Exception {
        when(taskerService.getByAccountId(accountId)).thenReturn(mock(TaskerResponse.class));

        mockMvc.perform(get("/api/v1/tasker/user/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldAssignTaskerSuccess() throws Exception {
        when(taskerService.assignTaskToTasker(any(), eq(accountId)))
                .thenReturn(mock(AssignTaskerResponse.class));

        String jsonRequest = """
                {
                    "taskId": "%s",
                    "taskerId": "%s",
                    "offerId": "%s"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(put("/api/v1/tasker/assign-tasker")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
    }
}