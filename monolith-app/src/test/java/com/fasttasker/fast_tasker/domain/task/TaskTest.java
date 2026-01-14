package com.fasttasker.fast_tasker.domain.task;

import com.fasttasker.common.exception.DomainException;
import com.fasttasker.fast_tasker.domain.tasker.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class TaskTest {

    private String validTitle;
    private String validDescription;
    private int validBudget;
    private Location validLocation;
    private LocalDate validTaskDate;
    private UUID validPosterId;

    @BeforeEach
    void setUp() {
        validTitle = "Test Task Title";
        validDescription = "This is a detailed description for the test task.";
        validBudget = 100;
        validLocation = new Location(40.7128, -74.0060, "Test Address", "10001");
        validTaskDate = LocalDate.now().plusDays(5);
        validPosterId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Constructor and Validation Tests")
    class ConstructorTests {

        @Test
        void shouldCreateTaskSuccessfully() {
            Task task = new Task(validTitle, validDescription, validBudget, validLocation, validTaskDate, validPosterId);

            assertThat(task).isNotNull();
            assertThat(task.getId()).isNotNull();
            assertThat(task.getTitle()).isEqualTo(validTitle);
            assertThat(task.getDescription()).isEqualTo(validDescription);
            assertThat(task.getBudget()).isEqualTo(validBudget);
            assertThat(task.getLocation()).isEqualTo(validLocation);
            assertThat(task.getTaskDate()).isEqualTo(validTaskDate);
            assertThat(task.getPosterId()).isEqualTo(validPosterId);
            assertThat(task.getStatus()).isEqualTo(TaskStatus.ACTIVE);
            assertThat(task.getAssignedTaskerId()).isNull();
        }

        @Test
        void shouldThrowWhenTitleIsNull() {
            assertThatThrownBy(() -> new Task(null, validDescription, validBudget, validLocation, validTaskDate, validPosterId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Title cannot be null or empty");
        }

        @Test
        void shouldThrowWhenDescriptionIsEmpty() {
            assertThatThrownBy(() -> new Task(validTitle, "", validBudget, validLocation, validTaskDate, validPosterId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Description cannot be null or empty");
        }

        @Test
        void shouldThrowWhenLocationIsNull() {
            assertThatThrownBy(() -> new Task(validTitle, validDescription, validBudget, null, validTaskDate, validPosterId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Location cannot be null");
        }

        @Test
        void shouldThrowWhenBudgetIsTooLow() {
            assertThatThrownBy(() -> new Task(validTitle, validDescription, 4, validLocation, validTaskDate, validPosterId))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Budget must be between 5 and 999");
        }

        @Test
        void shouldThrowWhenTaskDateIsInThePast() {
            LocalDate pastDate = LocalDate.now().minusDays(1);
            assertThatThrownBy(() -> new Task(validTitle, validDescription, validBudget, validLocation, pastDate, validPosterId))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Task date cannot be in the past");
        }
    }

    @Nested
    @DisplayName("Encapsulation Tests")
    class EncapsulationTests {

        @Test
        void getQuestionsShouldReturnUnmodifiableList() {
            Task task = new Task(validTitle, validDescription, validBudget, validLocation, validTaskDate, validPosterId);
            List<Question> questions =  task.getQuestions();
            assertThatThrownBy(() ->questions.add(Mockito.mock(Question.class)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void getOffersShouldReturnUnmodifiableList() {
            Task task = new Task(validTitle, validDescription, validBudget, validLocation, validTaskDate, validPosterId);
            List<Offer> offers = task.getOffers();
            assertThatThrownBy(() -> offers.add(Mockito.mock(Offer.class)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Business Method Tests")
    class BusinessMethodTests {

        private Task task;

        @BeforeEach
        void init() {
            task = new Task(validTitle, validDescription, validBudget, validLocation, validTaskDate, validPosterId);
        }

        @Test
        void shouldAddQuestionToActiveTask() {
            Question question = new Question("A valid question?", UUID.randomUUID(), task);
            task.addQuestion(question);
            assertThat(task.getQuestions()).contains(question);
        }

        @Test
        void shouldThrowWhenAddingQuestionToCompletedTask() {
            task.assignTasker(UUID.randomUUID());
            task.completeTask();
            Question question = new Question("A question?", UUID.randomUUID(), task);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
            assertThatThrownBy(() -> task.addQuestion(question))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Cannot add questions to a completed or cancelled task");
        }

        @Test
        void shouldAddOfferToActiveTask() {
            Offer offer = new Offer(150, "I can do it", UUID.randomUUID(), task);
            task.addOffer(offer);
            assertThat(task.getOffers()).contains(offer);
        }

        @Test
        void shouldThrowWhenAddingOfferToAssignedTask() {
            task.assignTasker(UUID.randomUUID());
            Offer offer = new Offer(150, "I can do it", UUID.randomUUID(), task);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.ASSIGNED);
            assertThatThrownBy(() -> task.addOffer(offer))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Can only add offers to active tasks");
        }

        @Test
        void shouldAnswerQuestion() {
            Question question = spy(new Question("A valid question?", UUID.randomUUID(), task));
            task.addQuestion(question);
            Answer answer = new Answer("This is the answer.", validPosterId, question);

            task.answerQuestion(question.getId(), answer);

            verify(question).addAnswer(answer);
        }

        @Test
        void shouldAcceptOfferAndAssignTasker() {
            UUID taskerId1 = UUID.randomUUID();
            UUID taskerId2 = UUID.randomUUID();
            Offer offerToAccept = spy(new Offer(120, "I'm the best", taskerId1, task));
            Offer offerToReject = spy(new Offer(130, "No, I am", taskerId2, task));

            task.addOffer(offerToAccept);
            task.addOffer(offerToReject);

            task.acceptOffer(offerToAccept.getId());

            assertThat(task.getStatus()).isEqualTo(TaskStatus.ASSIGNED);
            assertThat(task.getAssignedTaskerId()).isEqualTo(taskerId1);
            verify(offerToAccept).accept();
            verify(offerToReject).reject();
        }

        @Test
        void shouldStartWorkOnAssignedTask() {
            task.assignTasker(UUID.randomUUID());
            task.startWork();
            assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        }

        @Test
        void shouldCompleteTaskFromInProgress() {
            task.assignTasker(UUID.randomUUID());
            task.startWork();
            task.completeTask();
            assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        }

        @Test
        void shouldCompleteTaskFromAssigned() {
            task.assignTasker(UUID.randomUUID());
            task.completeTask();
            assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        }

        @Test
        void shouldCancelTask() {
            task.assignTasker(UUID.randomUUID());
            task.cancel();
            assertThat(task.getStatus()).isEqualTo(TaskStatus.CANCELLED);
        }

        @Test
        void shouldThrowWhenCancellingCompletedTask() {
            task.assignTasker(UUID.randomUUID());
            task.completeTask();

            assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
            assertThatThrownBy(task::cancel)
                    .isInstanceOf(DomainException.class)
                    .hasMessage("You cannot cancel a task that has already been completed");
        }

        @Test
        void shouldUpdateDetailsSuccessfully() {
            String newTitle = "Updated Title";
            String newDescription = "Updated Description";
            task.updateDetails(newTitle, newDescription);

            assertThat(task.getTitle()).isEqualTo(newTitle);
            assertThat(task.getDescription()).isEqualTo(newDescription);
        }

        @Test
        void shouldThrowWhenUpdatingDetailsOfCompletedTask() {
            task.assignTasker(UUID.randomUUID()); // A task must be assigned before it can be completed
            task.completeTask();
            assertThatThrownBy(() -> task.updateDetails("new", "new"))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Cannot update details of a completed task");
        }

        @Test
        void shouldAdjustBudgetSuccessfully() {
            task.adjustBudget(200);
            assertThat(task.getBudget()).isEqualTo(200);
        }

        @Test
        void shouldThrowWhenDecreasingBudgetWithOffers() {
            task.addOffer(new Offer(100, "offer", UUID.randomUUID(), task));
            assertThatThrownBy(() -> task.adjustBudget(50))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Cannot decrease budget when offers exist");
        }
    }

    @Nested
    @DisplayName("Helper Method Tests")
    class HelperMethodTests {
        private Task task;
        private Question question;
        private Offer offer;

        @BeforeEach
        void init() {
            task = new Task(validTitle, validDescription, validBudget, validLocation, validTaskDate, validPosterId);
            question = new Question("Is this a test?", UUID.randomUUID(), task);
            offer = new Offer(100, "Test offer", UUID.randomUUID(), task);
            task.addQuestion(question);
            task.addOffer(offer);
        }

        @Test
        void getQuestionByIdShouldReturnQuestion() {
            assertThat(task.getQuestionById(question.getId())).isEqualTo(question);
        }

        @Test
        void getQuestionByIdShouldThrowWhenNotFound() {
            UUID nonExistentQuestionId = UUID.randomUUID();
            assertThatThrownBy(() -> task.getQuestionById(nonExistentQuestionId))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("not found in this task");
        }

        @Test
        void getOfferByIdShouldReturnOffer() {
            assertThat(task.getOfferById(offer.getId())).isEqualTo(offer);
        }

        @Test
        void hasQuestionShouldReturnTrueForExisting() {
            assertThat(task.hasQuestion(question.getId())).isTrue();
        }

        @Test
        void hasQuestionShouldReturnFalseForNonExisting() {
            assertThat(task.hasQuestion(UUID.randomUUID())).isFalse();
        }

        @Test
        void hasOfferShouldReturnTrueForExisting() {
            assertThat(task.hasOffer(offer.getId())).isTrue();
        }
    }
}