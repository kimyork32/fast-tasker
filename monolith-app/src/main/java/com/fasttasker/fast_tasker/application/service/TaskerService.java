package com.fasttasker.fast_tasker.application.service;

import com.fasttasker.common.config.RabbitMQConfig;
import com.fasttasker.common.constant.RabbitMQConstants;
import com.fasttasker.fast_tasker.application.dto.conversation.ConversationRequest;
import com.fasttasker.fast_tasker.application.dto.conversation.MessageContentRequest;
import com.fasttasker.fast_tasker.application.dto.conversation.MessageRequest;
import com.fasttasker.fast_tasker.application.dto.notification.NotificationRequest;
import com.fasttasker.fast_tasker.application.dto.task.AssignTaskerRequest;
import com.fasttasker.fast_tasker.application.dto.task.AssignTaskerResponse;
import com.fasttasker.fast_tasker.application.dto.tasker.TaskerRequest;
import com.fasttasker.fast_tasker.application.dto.tasker.TaskerResponse;
import com.fasttasker.fast_tasker.application.exception.TaskAccessDeniedException;
import com.fasttasker.fast_tasker.application.mapper.TaskerMapper;
import com.fasttasker.fast_tasker.domain.notification.NotificationType;
import com.fasttasker.fast_tasker.domain.task.ITaskRepository;
import com.fasttasker.fast_tasker.domain.task.Task;
import com.fasttasker.fast_tasker.domain.tasker.ITaskerRepository;
import com.fasttasker.fast_tasker.domain.tasker.Profile;
import com.fasttasker.fast_tasker.domain.tasker.Tasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class TaskerService {

    private final ITaskerRepository taskerRepository;
    private final ITaskRepository taskRepository;
    private final TaskerMapper taskerMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ConversationService conversationService;

    public TaskerService(
            ITaskerRepository taskerRepository,
            ITaskRepository taskRepository,
            TaskerMapper taskerMapper,
            RabbitTemplate rabbitTemplate,
            ConversationService conversationService
    ) {
        this.taskerRepository = taskerRepository;
        this.taskRepository = taskRepository;
        this.taskerMapper = taskerMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.conversationService = conversationService;
    }

    /**
     * @param request tasker request from client
     */
    @Transactional
    public TaskerResponse registerTasker(TaskerRequest request) {
        UUID accountId = UUID.fromString(request.accountId());
        Tasker tasker = taskerRepository.findByAccountId(accountId);

        Profile profile = taskerMapper.toProfileEntity(request.profile());

        tasker.updateProfile(profile);

        taskerRepository.save(tasker);

        return taskerMapper.toResponse(tasker);
    }

    /**
     * @param taskerId tasker id
     */
    @Transactional(readOnly = true)
    public TaskerResponse getById(UUID taskerId) {
        Tasker tasker = taskerRepository.findById(taskerId);

        return taskerMapper.toResponse(tasker);
    }

    /**
     * get TaskerResponse by account id
     *
     * @param accountId account id
     * @return taskerResponse
     */
    @Transactional(readOnly = true)
    public TaskerResponse getByAccountId(UUID accountId) {
        Tasker tasker = taskerRepository.findByAccountId(accountId);
        return taskerMapper.toResponse(tasker);
    }

    @Transactional
    public AssignTaskerResponse assignTaskToTasker(AssignTaskerRequest request, UUID accountId) {
        UUID posterId = getPosterIdFromAccount(accountId);

        UUID taskId = parseUuid(request.taskId(), "taskId");
        UUID taskerId = parseUuid(request.taskerId(), "taskerId");
        UUID offerId = parseUuid(request.offerId(), "offerId");

        log.info("taskerId: {}", taskerId);

        Task task = taskRepository.findById(taskId);

        validatePosterOwnsTask(posterId, task);

        assignTaskerAndSave(task, taskerId);

        notifyOfferAccepted(taskerId, offerId);

        UUID conversationId = startPosterTaskerConversation(taskId, posterId, taskerId);

        sendGreetingMessage(conversationId, posterId);

        return taskerMapper.toAssignTaskerResponse(request);
    }

    private UUID getPosterIdFromAccount(UUID accountId) {
        return taskerRepository.findByAccountId(accountId).getId();
    }

    private UUID parseUuid(String raw, String fieldName) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid UUID for " + fieldName + ": " + raw, ex);
        }
    }

    private void validatePosterOwnsTask(UUID posterId, Task task) {
        if (!posterId.equals(task.getPosterId())) {
            throw new TaskAccessDeniedException("User does not have permission to assign this task.");
        }
    }

    private void assignTaskerAndSave(Task task, UUID taskerId) {
        task.assignTasker(taskerId);
        taskRepository.save(task);
    }

    private void notifyOfferAccepted(UUID taskerId, UUID offerId) {
        NotificationRequest request = new NotificationRequest(
                taskerId,
                offerId,
                NotificationType.OFFER_ACCEPTED
        );
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConstants.ROUTING_KEY_NOTIFICATION,
                    request
            );
        } catch (Exception e) {
            log.error("cannot send notification to RabbitMQ: {}", e.getMessage());
        }
    }

    private UUID startPosterTaskerConversation(UUID taskId, UUID posterId, UUID taskerId) {
        var conversationRequest = new ConversationRequest(taskId, posterId, taskerId);
        return conversationService.startConversation(conversationRequest);
    }

    private void sendGreetingMessage(UUID conversationId, UUID posterId) {
        var messageRequest = new MessageRequest(
                conversationId,
                new MessageContentRequest("Hola, te he asignado una tarea :)", null)
        );
        conversationService.processAndSendMessage(messageRequest, posterId);
    }

    /**
     * @param taskerId
     */
    public void getTaskHistory(UUID taskerId) {
        // TODO implement here
    }

    /**
     * @param taskerId
     */
    public void getActiveTasks(UUID taskerId) {
        // TODO implement here
    }
}