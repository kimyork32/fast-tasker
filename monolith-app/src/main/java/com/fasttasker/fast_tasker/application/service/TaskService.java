package com.fasttasker.fast_tasker.application.service;

import com.fasttasker.fast_tasker.application.dto.task.*;
import com.fasttasker.fast_tasker.application.dto.tasker.ChatProfileResponse;
import com.fasttasker.fast_tasker.application.dto.tasker.MinimalProfileResponse;
import com.fasttasker.fast_tasker.application.exception.*;
import com.fasttasker.fast_tasker.application.mapper.TaskMapper;
import com.fasttasker.fast_tasker.application.mapper.TaskerMapper;
import com.fasttasker.fast_tasker.domain.task.*;
import com.fasttasker.fast_tasker.domain.tasker.ITaskerRepository;
import com.fasttasker.fast_tasker.domain.tasker.Tasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasttasker.common.exception.DomainException;

/**
 * Service to handle Task business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final ITaskRepository taskRepository;
    private final ITaskerRepository taskerRepository;
    private final TaskMapper taskMapper;
    private final TaskerMapper taskerMapper;

    /**
     * Creates a new task posted by a user
     */
    @Transactional
    public TaskResponse createTask(TaskRequest taskRequest, UUID posterId) {
        Task newTask = taskMapper.toTaskEntity(taskRequest, posterId);
        Task savedTask = taskRepository.save(newTask);
        return taskMapper.toResponse(savedTask);
    }

    /**
     * Retrieves all tasks currently in ACTIVE status
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> listActiveTasks() {
        return taskRepository.findByStatus(TaskStatus.ACTIVE)
                .stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    /**
     * Retrieves all tasks created by a specific user (poster)
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> listTasksByPoster(UUID posterId) {
        return taskRepository.findByPosterId(posterId)
                .stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    /**
     * Finds a single task by its unique ID
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID taskId) {
        Task task = findTask(taskId);
        return taskMapper.toResponse(task);
    }

    /**
     * Retrieves full details of a task, including the poster's profile information
     */
    @Transactional(readOnly = true)
    public TaskCompleteResponse getTaskCompleteById(UUID taskId) {
        Task task = findTask(taskId);
        Tasker tasker = taskerRepository.findById(task.getPosterId());
        MinimalProfileResponse profileResponse = taskerMapper.toMinimalProfileResponse(tasker);
        return taskMapper.toTaskCompleteResponse(task, profileResponse);
    }

    /**
     * Adds a new question to a specific task
     */
    @Transactional
    public QuestionProfileResponse createQuestion(QuestionRequest questionRequest, UUID taskId, UUID askedById) {
        Task task = findTask(taskId);
        Question question = taskMapper.toQuestionEntity(questionRequest, askedById, task);

        task.addQuestion(question);
        Task taskSaved = taskRepository.save(task);

        Question savedQuestion = taskSaved.getQuestions().stream()
                .filter(q -> q.getId().equals(question.getId()))
                .findFirst()
                .orElseThrow(() -> new QuestionNotFoundException("Question was not saved correctly"));

        Tasker tasker = taskerRepository.findById(question.getAskedById());
        MinimalProfileResponse profileResponse = taskerMapper.toMinimalProfileResponse(tasker);
        QuestionResponse questionResponse = taskMapper.toQuestionResponse(savedQuestion);

        return taskMapper.toQuestionProfileResponse(questionResponse, profileResponse, null);
    }

    /**
     * Lists all questions for a task, including user profiles
     */
    @Transactional(readOnly = true)
    public List<QuestionProfileResponse> listQuestionsByTask(UUID taskId) {
        Task task = findTask(taskId);
        List<Question> questions = task.getQuestions();

        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, Tasker> taskersMap = preloadTaskersForQuestions(questions);

        return questions.stream()
                .map(question -> mapToQuestionProfile(question, taskersMap))
                .toList();
    }

    /**
     * Creates an offer for a task from a tasker
     */
    @Transactional
    public OfferProfileResponse createOffer(OfferRequest offerRequest, UUID taskId, UUID taskerId) {
        Task task = findTask(taskId);
        Offer offer = taskMapper.toOfferEntity(offerRequest, taskerId, task);

        task.addOffer(offer);
        Task savedTask = taskRepository.save(task);

        Offer savedOffer = savedTask.getOffers().stream()
                .filter(o -> o.getId().equals(offer.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException("Offer was not saved correctly"));

        Tasker tasker = taskerRepository.findById(taskerId);
        MinimalProfileResponse profileResponse = taskerMapper.toMinimalProfileResponse(tasker);
        OfferResponse offerResponse = taskMapper.toOfferResponse(savedOffer);

        return taskMapper.toOfferProfileResponse(offerResponse, profileResponse);
    }

    /**
     * Lists all offers for a task, including tasker profiles
     */
    @Transactional(readOnly = true)
    public List<OfferProfileResponse> listOffersByTask(UUID taskId) {
        Task task = findTask(taskId);
        List<Offer> offers = task.getOffers();

        if (offers.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, Tasker> taskersMap = preloadTaskersForOffers(offers);

        return offers.stream()
                .map(offer -> mapToOfferProfile(offer, taskersMap))
                .toList();
    }

    /**
     * Posts an answer to a specific question on a task
     */
    @Transactional
    public AnswerProfileResponse answerQuestion(AnswerRequest answerRequest, UUID taskId, UUID responderId) {
        UUID questionId = UUID.fromString(answerRequest.questionId());
        Task task = findTask(taskId);
        Question question = task.getQuestionById(questionId);

        Answer answer = taskMapper.toAnswerEntity(answerRequest, responderId, question);
        question.addAnswer(answer);

        Task taskSaved = taskRepository.save(task);
        Question questionSaved = taskSaved.getQuestionById(questionId);

        Answer answerSaved = questionSaved.getAnswers().stream()
                .filter(a -> a.getId().equals(answer.getId()))
                .findFirst()
                .orElseThrow(() -> new AnswerNotFoundException("Answer was not saved correctly"));

        Tasker tasker = taskerRepository.findById(responderId);
        ChatProfileResponse profileResponse = taskerMapper.toChatProfileResponse(tasker);
        AnswerResponse answerResponse = taskMapper.toAnswerResponse(answerSaved);

        return taskMapper.toAnswerProfileResponse(answerResponse, profileResponse);
    }

    private Task findTask(UUID taskId) {
        return Optional.ofNullable(taskRepository.findById(taskId))
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
    }

    private Map<UUID, Tasker> loadTaskersMap(List<UUID> ids) {
        return taskerRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(
                        Tasker::getId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    private Map<UUID, Tasker> preloadTaskersForQuestions(List<Question> questions) {
        List<UUID> taskerIds = questions.stream()
                .flatMap(q -> Stream.concat(
                        Stream.of(q.getAskedById()),
                        q.getAnswers().stream().map(Answer::getResponderId)
                ))
                .distinct()
                .toList();
        return loadTaskersMap(taskerIds);
    }

    private Map<UUID, Tasker> preloadTaskersForOffers(List<Offer> offers) {
        List<UUID> taskerIds = offers.stream()
                .map(Offer::getOffertedById)
                .distinct()
                .toList();
        return loadTaskersMap(taskerIds);
    }

    private QuestionProfileResponse mapToQuestionProfile(Question question, Map<UUID, Tasker> taskersMap) {
        Tasker asker = taskersMap.get(question.getAskedById());
        MinimalProfileResponse askerProfile = taskerMapper.toMinimalProfileResponse(asker);
        QuestionResponse questionResponse = taskMapper.toQuestionResponse(question);

        List<AnswerProfileResponse> answers = question.getAnswers().stream()
                .map(answer -> mapToAnswerProfile(answer, taskersMap))
                .toList();

        return taskMapper.toQuestionProfileResponse(questionResponse, askerProfile, answers);
    }

    private AnswerProfileResponse mapToAnswerProfile(Answer answer, Map<UUID, Tasker> taskersMap) {
        Tasker responder = taskersMap.get(answer.getResponderId());
        ChatProfileResponse responderProfile = taskerMapper.toChatProfileResponse(responder);
        AnswerResponse answerResponse = taskMapper.toAnswerResponse(answer);
        return taskMapper.toAnswerProfileResponse(answerResponse, responderProfile);
    }

    private OfferProfileResponse mapToOfferProfile(Offer offer, Map<UUID, Tasker> taskersMap) {
        Tasker tasker = taskersMap.get(offer.getOffertedById());
        MinimalProfileResponse profileResponse = taskerMapper.toMinimalProfileResponse(tasker);
        OfferResponse offerResponse = taskMapper.toOfferResponse(offer);
        return taskMapper.toOfferProfileResponse(offerResponse, profileResponse);
    }
}