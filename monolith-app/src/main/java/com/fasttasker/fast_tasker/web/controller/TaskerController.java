package com.fasttasker.fast_tasker.web.controller;

import com.fasttasker.fast_tasker.application.service.TaskerService;
import com.fasttasker.fast_tasker.application.dto.task.AssignTaskerRequest;
import com.fasttasker.fast_tasker.application.dto.task.AssignTaskerResponse;
import com.fasttasker.fast_tasker.application.dto.tasker.TaskerRegistrationResponse;
import com.fasttasker.fast_tasker.application.dto.tasker.TaskerRequest;
import com.fasttasker.fast_tasker.application.dto.tasker.TaskerResponse;
import com.fasttasker.common.config.JwtService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 *
 */
@Slf4j
@RestController
@RequestMapping("api/v1/tasker")
public class TaskerController {

    private final TaskerService taskerService;
    private final JwtService jwtService;

    /**
     * constructor for dependencies injection
     */
    @Autowired
    public TaskerController(TaskerService taskerService, JwtService jwtService) {
        this.taskerService = taskerService;
        this.jwtService = jwtService;
    }

    @PutMapping("/register")
    public ResponseEntity<TaskerRegistrationResponse> initialRegister(
            @Valid @RequestBody TaskerRequest request,
            Authentication authentication
    ) {
        UUID accountId = jwtService.extractAccountId(authentication);
        var serviceRequest = new TaskerRequest(accountId.toString(), request.profile());
        TaskerResponse taskerResponse = taskerService.registerTasker(serviceRequest);
        log.info("taskerId: {}", accountId);
        log.info("accountId: {}", UUID.fromString(taskerResponse.id()));
        String newToken = jwtService.generateToken(accountId, UUID.fromString(taskerResponse.id()), true); // profileCompleted = true
        // creating DTO with the token
        var response = new TaskerRegistrationResponse(
                taskerResponse.id(),
                taskerResponse.accountId(),
                taskerResponse.profile(),
                newToken
        );

        // return profile and new token
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<TaskerResponse> getTaskerById(@PathVariable UUID userId) {
        TaskerResponse response = taskerService.getById((userId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/me")
    public ResponseEntity<TaskerResponse> getTaskerMe(Authentication authentication) {
        UUID accountId = jwtService.extractAccountId(authentication);
        TaskerResponse response = taskerService.getByAccountId(accountId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/assign-tasker")
    public ResponseEntity<AssignTaskerResponse> assignTasker(
            Authentication authentication,
            @Valid @RequestBody AssignTaskerRequest request
    ) {
        UUID accountId = jwtService.extractAccountId(authentication);
        AssignTaskerResponse response = taskerService.assignTaskToTasker(request, accountId);
        return ResponseEntity.ok(response);
    }

}
