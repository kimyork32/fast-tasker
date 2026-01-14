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

class AnswerTest {

    private Question mockQuestion;
    private UUID validResponderId;
    private String validDescription;

    @BeforeEach
    void setUp() {
        mockQuestion = Mockito.mock(Question.class);
        validResponderId = UUID.randomUUID();
        validDescription = "This is a valid answer description.";
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        void shouldCreateAnswerSuccessfully() {
            Answer answer = new Answer(validDescription, validResponderId, mockQuestion);

            assertThat(answer.getId()).isNotNull();
            assertThat(answer.getDescription()).isEqualTo(validDescription);
            assertThat(answer.getResponderId()).isEqualTo(validResponderId);
            assertThat(answer.getQuestion()).isEqualTo(mockQuestion);
            assertThat(answer.getCreatedAt()).isNotNull();
        }

        @Test
        void shouldThrowWhenDescriptionIsNull() {
            assertThatThrownBy(() -> new Answer(null, validResponderId, mockQuestion))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Description cannot be null or empty");
        }

        @Test
        void shouldThrowWhenDescriptionIsTooLong() {
            String longDescription = "a".repeat(501);
            assertThatThrownBy(() -> new Answer(longDescription, validResponderId, mockQuestion))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Description exceeds 500 characters");
        }

        @Test
        void shouldThrowWhenResponderIdIsNull() {
            assertThatThrownBy(() -> new Answer(validDescription, null, mockQuestion))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("AnsweredId cannot be null");
        }

        @Test
        void shouldThrowWhenQuestionIsNull() {
            assertThatThrownBy(() -> new Answer(validDescription, validResponderId, null))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Question cannot be null");
        }
    }

    @Nested
    @DisplayName("Business Method Tests")
    class BusinessMethodTests {
        @Test
        void shouldEditDescriptionSuccessfully() {
            Answer answer = new Answer(validDescription, validResponderId, mockQuestion);
            String newDescription = "This is an updated answer.";

            answer.editDescription(newDescription);

            assertThat(answer.getDescription()).isEqualTo(newDescription);
        }

        @Test
        void shouldThrowWhenEditingWithEmptyDescription() {
            Answer answer = new Answer(validDescription, validResponderId, mockQuestion);

            assertThatThrownBy(() -> answer.editDescription(" "))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Answer description cannot be empty");
        }
    }
}