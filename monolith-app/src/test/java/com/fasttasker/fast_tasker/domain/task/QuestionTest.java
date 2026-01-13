package com.fasttasker.fast_tasker.domain.task;

import com.fasttasker.common.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionTest {

    private Task mockTask;
    private UUID validAskedById;
    private String validDescription;

    @BeforeEach
    void setUp() {
        mockTask = Mockito.mock(Task.class);
        validAskedById = UUID.randomUUID();
        validDescription = "This is a valid question?";
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        void shouldCreateQuestionSuccessfully() {
            Question question = new Question(validDescription, validAskedById, mockTask);

            assertThat(question.getId()).isNotNull();
            assertThat(question.getDescription()).isEqualTo(validDescription);
            assertThat(question.getAskedById()).isEqualTo(validAskedById);
            assertThat(question.getTask()).isEqualTo(mockTask);
            assertThat(question.getStatus()).isEqualTo(QuestionStatus.PENDING);
            assertThat(question.getCreatedAt()).isNotNull();
        }

        @Test
        void shouldThrowWhenDescriptionIsNull() {
            assertThatThrownBy(() -> new Question(null, validAskedById, mockTask))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Question description cannot be empty");
        }

        @Test
        void shouldThrowWhenDescriptionIsTooLong() {
            String longDescription = "a".repeat(501);
            assertThatThrownBy(() -> new Question(longDescription, validAskedById, mockTask))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Question description exceeds 500 characters");
        }

        @Test
        void shouldThrowWhenAskedByIdIsNull() {
            assertThatThrownBy(() -> new Question(validDescription, null, mockTask))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("AskedById cannot be null");
        }

        @Test
        void shouldThrowWhenTaskIsNull() {
            assertThatThrownBy(() -> new Question(validDescription, validAskedById, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Task cannot be null");
        }
    }

    @Nested
    @DisplayName("Encapsulation Tests")
    class EncapsulationTests {
        @Test
        void getAnswersShouldReturnUnmodifiableList() {
            Question question = new Question(validDescription, validAskedById, mockTask);
            var answers = question.getAnswers();
            var mockAnswer = Mockito.mock(Answer.class);
            assertThatThrownBy(() -> answers.add(mockAnswer))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Business Method Tests")
    class BusinessMethodTests {

        private Question question;

        @BeforeEach
        void init() {
            question = new Question(validDescription, validAskedById, mockTask);
        }

        @Test
        void shouldAddAnswerSuccessfully() {
            Answer answer = Mockito.mock(Answer.class);
            question.addAnswer(answer);

            assertThat(question.getAnswers()).contains(answer);
            assertThat(question.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
        }

        @Test
        void shouldThrowWhenAddingNullAnswer() {
            assertThatThrownBy(() -> question.addAnswer(null))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Answer cannot be null");
        }

        @Test
        void shouldThrowWhenAnsweringDeletedQuestion() {
            question.markAsDeleted();
            Answer answer = Mockito.mock(Answer.class);

            assertThatThrownBy(() -> question.addAnswer(answer))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Cannot answer a deleted question");
        }

        @Test
        void shouldEditDescriptionSuccessfully() {
            String newDescription = "This is an updated question.";
            question.editDescription(newDescription);
            assertThat(question.getDescription()).isEqualTo(newDescription);
        }

        @Test
        void shouldThrowWhenEditingWithEmptyDescription() {
            assertThatThrownBy(() -> question.editDescription(" "))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Question description cannot be empty");
        }

        @Test
        void shouldThrowWhenEditingAnsweredQuestion() {
            question.addAnswer(Mockito.mock(Answer.class)); // Status is now ANSWERED
            assertThatThrownBy(() -> question.editDescription("new desc"))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Cannot edit a question that has already been answered");
        }

        @Test
        void shouldPostAnswerUsingLegacyMethod() {
            String answerContent = "This is a legacy answer.";
            UUID responderId = UUID.randomUUID();
            question.postAnswer(answerContent, responderId);

            assertThat(question.getAnswers()).hasSize(1);
            Answer postedAnswer = question.getAnswers().getFirst();
            assertThat(postedAnswer.getDescription()).isEqualTo(answerContent);
            assertThat(postedAnswer.getResponderId()).isEqualTo(responderId);
            assertThat(question.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
        }

        @Test
        void shouldMarkAsDeleted() {
            question.markAsDeleted();
            assertThat(question.getStatus()).isEqualTo(QuestionStatus.DELETED);
        }
    }
}