package com.fasttasker.fast_tasker.application;

import com.fasttasker.fast_tasker.application.dto.task.*;
import com.fasttasker.fast_tasker.application.dto.tasker.*;
import com.fasttasker.fast_tasker.application.exception.TaskNotFoundException;
import com.fasttasker.fast_tasker.application.mapper.TaskMapper;
import com.fasttasker.fast_tasker.application.mapper.TaskerMapper;
import com.fasttasker.fast_tasker.application.service.TaskService;
import com.fasttasker.fast_tasker.domain.task.*;
import com.fasttasker.fast_tasker.domain.tasker.ITaskerRepository;
import com.fasttasker.fast_tasker.domain.tasker.Tasker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    ITaskRepository taskRepository;
    @Mock
    ITaskerRepository taskerRepository;
    @Mock
    TaskMapper taskMapper;
    @Mock
    TaskerMapper taskerMapper;

    @InjectMocks
    TaskService taskService;

    final UUID taskId = UUID.randomUUID();
    final UUID taskerId = UUID.randomUUID();
    final UUID posterId = UUID.randomUUID();
    Task task;

    @BeforeEach
    void setUp() {
        task = mock(Task.class);
    }

    @Nested
    @DisplayName("createTask()")
    class CreateTask {
        @Test
        @DisplayName("Saves task and returns response")
        void savesTaskWhenValid() {
            TaskRequest request = mock(TaskRequest.class);
            Task taskEntity = mock(Task.class);
            TaskResponse response = mock(TaskResponse.class);

            when(taskMapper.toTaskEntity(request, posterId)).thenReturn(taskEntity);
            when(taskRepository.save(taskEntity)).thenReturn(taskEntity);
            when(taskMapper.toResponse(taskEntity)).thenReturn(response);

            var result = taskService.createTask(request, posterId);

            assertThat(result).isEqualTo(response);
            verify(taskRepository).save(taskEntity);
        }
    }

    @Nested
    @DisplayName("Listing Tasks")
    class ListTasks {
        @Test
        @DisplayName("listActiveTasks: returns mapped list")
        void listActiveTasksReturnsList() {
            when(taskRepository.findByStatus(TaskStatus.ACTIVE)).thenReturn(List.of(task));
            when(taskMapper.toResponse(task)).thenReturn(mock(TaskResponse.class));

            assertThat(taskService.listActiveTasks()).hasSize(1);
        }

        @Test
        @DisplayName("listTasksByPoster: returns mapped list")
        void listTasksByPosterReturnsList() {
            when(taskRepository.findByPosterId(posterId)).thenReturn(List.of(task));
            when(taskMapper.toResponse(task)).thenReturn(mock(TaskResponse.class));

            assertThat(taskService.listTasksByPoster(posterId)).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getTaskById()")
    class GetTaskById {
        @Test
        @DisplayName("Returns response when found")
        void returnsResponseWhenFound() {
            when(taskRepository.findById(taskId)).thenReturn(task);

            TaskResponse response = mock(TaskResponse.class);
            when(taskMapper.toResponse(task)).thenReturn(response);

            assertThat(taskService.getTaskById(taskId)).isEqualTo(response);
        }

        @Test
        @DisplayName("Throws exception when not found")
        void throwsExceptionWhenNotFound() {
            when(taskRepository.findById(taskId)).thenReturn(null);

            assertThatThrownBy(() -> taskService.getTaskById(taskId))
                    .isInstanceOf(TaskNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getTaskCompleteById()")
    class GetTaskComplete {
        @Test
        @DisplayName("Returns complete response with profile")
        void returnsCompleteResponse() {
            when(taskRepository.findById(taskId)).thenReturn(task);
            when(task.getPosterId()).thenReturn(posterId);

            Tasker tasker = mock(Tasker.class);
            when(taskerRepository.findById(posterId)).thenReturn(tasker);

            TaskCompleteResponse response = mock(TaskCompleteResponse.class);
            when(taskMapper.toTaskCompleteResponse(eq(task), any())).thenReturn(response);

            assertThat(taskService.getTaskCompleteById(taskId)).isEqualTo(response);
        }
    }

    @Nested
    @DisplayName("createQuestion()")
    class CreateQuestion {
        @Test
        @DisplayName("Adds question and returns profile response")
        void addsQuestionSuccessfully() {
            QuestionRequest request = mock(QuestionRequest.class);
            Question question = mock(Question.class);
            UUID questionId = UUID.randomUUID();

            when(taskRepository.findById(taskId)).thenReturn(task);

            when(taskMapper.toQuestionEntity(request, taskerId, task)).thenReturn(question);
            when(question.getId()).thenReturn(questionId);
            when(question.getAskedById()).thenReturn(taskerId);

            when(task.getQuestions()).thenReturn(List.of(question));
            when(taskRepository.save(task)).thenReturn(task);

            when(taskerRepository.findById(taskerId)).thenReturn(mock(Tasker.class));

            QuestionProfileResponse response = mock(QuestionProfileResponse.class);
            when(taskMapper.toQuestionProfileResponse(any(), any(), any())).thenReturn(response);

            var result = taskService.createQuestion(request, taskId, taskerId);

            assertThat(result).isEqualTo(response);
            verify(task).addQuestion(question);
        }
    }

    @Nested
    @DisplayName("listQuestionsByTask()")
    class ListQuestions {
        @BeforeEach
        void init() {
            when(taskRepository.findById(taskId)).thenReturn(task);
        }

        @Test
        @DisplayName("Returns empty when no questions")
        void returnsEmptyWhenNone() {
            when(task.getQuestions()).thenReturn(Collections.emptyList());
            assertThat(taskService.listQuestionsByTask(taskId)).isEmpty();
        }

        @Test
        @DisplayName("Returns mapped DTOs when questions exist")
        void returnsMappedDtosWhenExist() {
            Question question = mock(Question.class);
            Answer answer = mock(Answer.class);
            UUID responderId = UUID.randomUUID();

            when(question.getAskedById()).thenReturn(taskerId);

            when(answer.getResponderId()).thenReturn(responderId);
            when(question.getAnswers()).thenReturn(List.of(answer));

            when(task.getQuestions()).thenReturn(List.of(question));

            when(taskerRepository.findAllById(anyList())).thenReturn(List.of(mock(Tasker.class), mock(Tasker.class)));

            when(taskMapper.toQuestionResponse(question)).thenReturn(mock(QuestionResponse.class));
            when(taskMapper.toAnswerResponse(answer)).thenReturn(mock(AnswerResponse.class));
            when(taskerMapper.toMinimalProfileResponse(any())).thenReturn(mock(MinimalProfileResponse.class));

            when(taskerMapper.toChatProfileResponse(any())).thenReturn(mock(ChatProfileResponse.class));

            when(taskMapper.toQuestionProfileResponse(any(), any(), anyList())).thenReturn(mock(QuestionProfileResponse.class));

            var result = taskService.listQuestionsByTask(taskId);

            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("createOffer()")
    class CreateOffer {
        @Test
        @DisplayName("Adds offer and returns response")
        void addsOfferSuccessfully() {
            OfferRequest request = mock(OfferRequest.class);
            Offer offer = mock(Offer.class);
            UUID offerId = UUID.randomUUID();

            when(taskRepository.findById(taskId)).thenReturn(task);
            when(taskMapper.toOfferEntity(request, taskerId, task)).thenReturn(offer);
            when(offer.getId()).thenReturn(offerId);

            when(task.getOffers()).thenReturn(List.of(offer));
            when(taskRepository.save(task)).thenReturn(task);

            when(taskerRepository.findById(taskerId)).thenReturn(mock(Tasker.class));

            OfferProfileResponse response = mock(OfferProfileResponse.class);
            when(taskMapper.toOfferProfileResponse(any(), any())).thenReturn(response);

            var result = taskService.createOffer(request, taskId, taskerId);

            assertThat(result).isEqualTo(response);
            verify(task).addOffer(offer);
        }
    }

    @Nested
    @DisplayName("listOffersByTask()")
    class ListOffers {
        @BeforeEach
        void init() {
            when(taskRepository.findById(taskId)).thenReturn(task);
        }

        @Test
        @DisplayName("Returns empty when no offers")
        void returnsEmptyWhenNone() {
            when(task.getOffers()).thenReturn(Collections.emptyList());
            assertThat(taskService.listOffersByTask(taskId)).isEmpty();
        }

        @Test
        @DisplayName("Returns mapped offers when exist")
        void returnsOffersWhenExist() {
            Offer offer = mock(Offer.class);
            when(offer.getOffertedById()).thenReturn(taskerId);

            when(task.getOffers()).thenReturn(List.of(offer));
            when(taskerRepository.findAllById(anyList())).thenReturn(List.of(mock(Tasker.class)));

            OfferProfileResponse expectedDto = mock(OfferProfileResponse.class);
            when(taskMapper.toOfferProfileResponse(any(), any())).thenReturn(expectedDto);

            assertThat(taskService.listOffersByTask(taskId))
                    .hasSize(1)
                    .first().isEqualTo(expectedDto);
        }
    }

    @Nested
    @DisplayName("answerQuestion()")
    class AnswerQuestion {
        @Test
        @DisplayName("Adds answer and returns response")
        void addsAnswerSuccessfully() {
            UUID questionId = UUID.randomUUID();
            UUID answerId = UUID.randomUUID();
            AnswerRequest request = new AnswerRequest(questionId.toString(), "Answer text");

            Question question = mock(Question.class);
            Answer answer = mock(Answer.class);

            when(taskRepository.findById(taskId)).thenReturn(task);
            when(task.getQuestionById(questionId)).thenReturn(question);

            when(taskMapper.toAnswerEntity(request, taskerId, question)).thenReturn(answer);
            when(answer.getId()).thenReturn(answerId);

            when(taskRepository.save(task)).thenReturn(task);
            when(task.getQuestionById(questionId)).thenReturn(question);
            when(question.getAnswers()).thenReturn(List.of(answer));

            when(taskerRepository.findById(taskerId)).thenReturn(mock(Tasker.class));

            AnswerProfileResponse response = mock(AnswerProfileResponse.class);
            when(taskMapper.toAnswerProfileResponse(any(), any())).thenReturn(response);

            var result = taskService.answerQuestion(request, taskId, taskerId);

            assertThat(result).isEqualTo(response);
            verify(question).addAnswer(answer);
        }
    }
}
