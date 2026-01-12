package com.fasttasker.fast_tasker.web.controller;

import com.fasttasker.fast_tasker.application.service.TaskService;
import com.fasttasker.fast_tasker.application.dto.task.*;
import com.fasttasker.common.config.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


/**
 * task controller
 */
@RestController
@RequestMapping("api/v1/tasks") // Plural, REST conversion
public class TaskController {

    private final TaskService taskService;
    private final JwtService jwtService;
    /**
     * constructor for dependencies injection
     */
    @Autowired
    public TaskController(TaskService taskService, JwtService jwtService) {
        this.taskService = taskService;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @RequestBody TaskRequest request,
            Authentication authentication
    ) {
        UUID posterId = jwtService.extractTaskerId(authentication);

        TaskResponse response = taskService.createTask(request, posterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllPublicActiveTasks() {
        List<TaskResponse> tasks = taskService.listActiveTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<List<TaskResponse>> getAllMyTasks(Authentication authentication) {
        UUID posterId = jwtService.extractTaskerId(authentication);
        List<TaskResponse> posterTasks = taskService.listTasksByPoster(posterId);
        return ResponseEntity.ok(posterTasks);
    }

    @GetMapping("{taskId}")
    public ResponseEntity<TaskCompleteResponse> getTaskComplete(@PathVariable ("taskId") UUID taskId) {
        TaskCompleteResponse taskCompleteResponse = taskService.getTaskCompleteById(taskId);
        return ResponseEntity.ok(taskCompleteResponse);
    }

    @PostMapping("/{taskId}/offers")
    public ResponseEntity<OfferProfileResponse> createOffer(
            @PathVariable UUID taskId,
            @RequestBody OfferRequest offerRequest,
            Authentication authentication
    ) {
        UUID taskerId = jwtService.extractTaskerId(authentication);
        OfferProfileResponse offerProfileResponse = taskService.createOffer(offerRequest, taskId, taskerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(offerProfileResponse);
    }

    @GetMapping("/{taskId}/offers")
    public ResponseEntity<List<OfferProfileResponse>> getAllOffersByTask(@PathVariable ("taskId") UUID taskId) {
        List<OfferProfileResponse> offers = taskService.listOffersByTask(taskId);
        return ResponseEntity.ok(offers);
    }

    @PostMapping("/{taskId}/questions")
    public ResponseEntity<QuestionProfileResponse> createQuestion(
            @PathVariable UUID taskId,
            @RequestBody QuestionRequest questionRequest,
            Authentication authentication
    ) {
        UUID askedById = jwtService.extractTaskerId(authentication);
        QuestionProfileResponse response = taskService.createQuestion(questionRequest, taskId, askedById);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{taskId}/questions")
    public  ResponseEntity<List<QuestionProfileResponse>> getAllQuestionsByTask(@PathVariable ("taskId") UUID taskId) {
        List<QuestionProfileResponse> questions = taskService.listQuestionsByTask(taskId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/{taskId}/answer")
    public ResponseEntity<AnswerProfileResponse> createAnswer(
            @PathVariable UUID taskId,
            @RequestBody AnswerRequest answerRequest,
            Authentication authentication
    ) {
        UUID responderId = jwtService.extractTaskerId(authentication);
        AnswerProfileResponse response = taskService.answerQuestion(answerRequest, taskId, responderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}