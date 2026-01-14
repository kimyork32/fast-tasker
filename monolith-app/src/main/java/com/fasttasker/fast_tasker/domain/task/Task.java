package com.fasttasker.fast_tasker.domain.task;

import com.fasttasker.common.exception.DomainException;
import com.fasttasker.fast_tasker.domain.tasker.Location;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * AR for task.
 * This entity encapsulates all information about a posted job
 */
@Entity
@Table(name = "task")
@NoArgsConstructor(access =  AccessLevel.PROTECTED)
@Getter
@ToString
@EqualsAndHashCode(exclude = {"questions", "offers"})
public class Task {

    @Id
    private UUID id;

    /**
     * Short and descriptive title for the task.
     * Limited to 100 characters.
     */
    @Column(name = "title", length = 100, nullable = false)
    private String title;

    /**
     * Detailed description of the task requirements.
     * Limited to 500 characters.
     */
    @Column(name = "description", length = 500, nullable = false)
    private String description;

    /**
     * The offered budget or amount for the task.
     * might allow tasks without an initial budget.
     * budget must be a positive integer
     * greater than 5 and less than 999
     */
    @Column(name = "budget", nullable = false)
    private int budget;

    /**
     * Physical location where the task must be performed.
     * Mapped as an Embedded Value Object.
     */
    @Embedded
    private Location location;

    /**
     * Specific date on which the task must be carried out.
     */
    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;

    /**
     * Current status of the task lifecycle (e.g., OPEN, IN_PROGRESS).
     * Stored as String in the DB for better readability.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status;

    /**
     * the ID of the account that posted this task.
     * ForeignKey that links it to the Account.
     */
    @Column(name = "poster_id", nullable = false)
    private UUID posterId;

    /**
     * the ID of the tasker assigned to complete this task.
     * This is null when the task is created (OPEN).
     * it gets populated when and Offer is accepted
     */
    @Column(name = "assigned_tasker_id")
    private UUID assignedTaskerId;

    /**
     * List of questions posted by Taskers regarding this task.
     * This is the "One" side of the relationship. The 'Question'
     * entity manages the relationship (mappedBy = "task").
     * FetchType.LAZY: Questions are not loaded unless requested.
     * CascadeType.ALL: If this Task is deleted, its Questions are deleted too.
     */
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private final List<Question> questions = new ArrayList<>();

    /**
     * List of offers made by Taskers for this task.
     * The 'Offer' entity manages the relationship (mappedBy = "task").
     * FetchType.LAZY: Offers are not loaded unless requested.
     * CascadeType.ALL: If this Task is deleted, its Offers are deleted too.
     */
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private final List<Offer> offers = new ArrayList<>();

    @Builder(toBuilder = true)
    public Task(String title, String description, int budget, Location location, LocalDate taskDate, UUID posterId) {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        validateBudget(budget);
        validateTaskDate(taskDate);

        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.location = location;
        this.taskDate = taskDate;
        this.status = TaskStatus.ACTIVE;
        this.posterId = posterId;
        this.assignedTaskerId = null;
    }

    // ============================================================================
    // ENCAPSULATION: Getters that return unmodifiable collections
    // ============================================================================

    /**
     * Returns an unmodifiable view of the questions list.
     * External code cannot modify the internal state through this getter.
     *
     * @return unmodifiable list of questions
     */
    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    /**
     * Returns an unmodifiable view of the offers list.
     * External code cannot modify the internal state through this getter.
     *
     * @return unmodifiable list of offers
     */
    public List<Offer> getOffers() {
        return Collections.unmodifiableList(offers);
    }

    // ============================================================================
    // BUSINESS METHODS: Controlled manipulation of internal state
    // ============================================================================

    /**
     * Business method to add a question to this task.
     * Encapsulates the logic and validations for adding questions.
     *
     * @param question the question to add
     * @throws DomainException if the task is not in a valid state to receive questions
     */
    public void addQuestion(Question question) {
        if (question == null) {
            throw new DomainException("Question cannot be null");
        }
        if (this.status == TaskStatus.COMPLETED || this.status == TaskStatus.CANCELLED) {
            throw new DomainException("Cannot add questions to a completed or cancelled task");
        }
        this.questions.add(question);
    }

    /**
     * Business method to add an offer to this task.
     * Encapsulates the logic and validations for adding offers.
     *
     * @param offer the offer to add
     * @throws DomainException if the task is not in a valid state to receive offers
     */
    public void addOffer(Offer offer) {
        if (offer == null) {
            throw new DomainException("Offer cannot be null");
        }
        if (this.status != TaskStatus.ACTIVE) {
            throw new DomainException("Can only add offers to active tasks");
        }
        if (this.assignedTaskerId != null) {
            throw new DomainException("Cannot add offers to an already assigned task");
        }
        this.offers.add(offer);
    }

    /**
     * Business method to answer a question.
     * This ensures the question belongs to this task and the task state allows it.
     *
     * @param questionId the ID of the question to answer
     * @param answer the answer text
     * @throws DomainException if the question doesn't exist or task state is invalid
     */
    public void answerQuestion(UUID questionId, Answer answer) {
        if (answer == null) {
            throw new DomainException("Answer cannot be null");
        }

        Question question = this.questions.stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Question not found in this task"));

        if (this.status == TaskStatus.COMPLETED || this.status == TaskStatus.CANCELLED) {
            throw new DomainException("Cannot answer questions on a completed or cancelled task");
        }

        question.addAnswer(answer);
    }

    /**
     * Business method to accept an offer and assign the task.
     *
     * @param offerId the ID of the offer to accept
     * @throws DomainException if the offer doesn't exist or task state is invalid
     */
    public void acceptOffer(UUID offerId) {
        Offer offer = this.offers.stream()
                .filter(o -> o.getId().equals(offerId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Offer not found in this task"));

        if (this.status != TaskStatus.ACTIVE) {
            throw new DomainException("Only active tasks can have offers accepted");
        }

        // Mark the offer as accepted
        offer.accept();

        // Assign the tasker
        this.assignTasker(offer.getTaskerId());

        // Reject all other offers
        this.offers.stream()
                .filter(o -> !o.getId().equals(offerId))
                .forEach(Offer::reject);
    }

    // ============================================================================
    // EXISTING BUSINESS METHODS
    // ============================================================================

    public void updateDetails(String newTitle, String newDescription) {
        if (newTitle == null || newTitle.isEmpty()) {
            throw new DomainException("Title cannot be null or empty");
        }
        if (newDescription == null || newDescription.isEmpty()) {
            throw new DomainException("Description cannot be null or empty");
        }
        if (this.status == TaskStatus.COMPLETED) {
            throw new DomainException("Cannot update details of a completed task");
        }

        this.title = newTitle;
        this.description = newDescription;
    }

    public void adjustBudget(int newBudget) {
        if (newBudget < 5 || newBudget > 999) {
            throw new DomainException("Budget must be between 5 and 999");
        }
        if (!this.offers.isEmpty() && newBudget < this.budget) {
            throw new DomainException("Cannot decrease budget when offers exist");
        }

        this.budget = newBudget;
    }

    public void assignTasker(UUID taskerId) {
        if (taskerId == null) {
            throw new DomainException("Tasker ID cannot be null");
        }
        if (this.status != TaskStatus.ACTIVE) {
            throw new DomainException("Only active tasks can be assigned");
        }

        this.assignedTaskerId = taskerId;
        this.status = TaskStatus.ASSIGNED;
    }

    public void startWork() {
        if (this.status != TaskStatus.ASSIGNED) {
            throw new DomainException("The task must be assigned in order to begin");
        }
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void completeTask() {
        if (this.status != TaskStatus.ASSIGNED && this.status != TaskStatus.IN_PROGRESS) {
            throw new DomainException("You cannot complete a task that is not in progress or assigned");
        }
        this.status = TaskStatus.COMPLETED;
    }

    public void cancel() {
        if (this.status == TaskStatus.COMPLETED) {
            throw new DomainException("You cannot cancel a task that has already been completed");
        }
        this.status = TaskStatus.CANCELLED;
    }

    // ============================================================================
    // PRIVATE VALIDATION METHODS
    // ============================================================================

    private void validateBudget(int budget) {
        if (budget < 5 || budget > 999) {
            throw new DomainException("Budget must be between 5 and 999");
        }
    }

    private void validateTaskDate(LocalDate date) {
        if (date == null) {
            throw new DomainException("Task date cannot be null");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new DomainException("Task date cannot be in the past");
        }
    }
    /**
     * Retrieves a question by its ID from this task.
     *
     * @param questionId the ID of the question to find
     * @return the Question if found
     * @throws DomainException if the question is not found in this task
     */
    public Question getQuestionById(UUID questionId) {
        if (questionId == null) {
            throw new DomainException("Question ID cannot be null");
        }

        return this.questions.stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "Question with ID " + questionId + " not found in this task"
                ));
    }

    /**
     * Retrieves an offer by its ID from this task.
     *
     * @param offerId the ID of the offer to find
     * @return the Offer if found
     * @throws DomainException if the offer is not found in this task
     */
    public Offer getOfferById(UUID offerId) {
        if (offerId == null) {
            throw new DomainException("Offer ID cannot be null");
        }

        return this.offers.stream()
                .filter(o -> o.getId().equals(offerId))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        "Offer with ID " + offerId + " not found in this task"
                ));
    }

    /**
     * Checks if a question with the given ID exists in this task.
     *
     * @param questionId the ID of the question to check
     * @return true if the question exists, false otherwise
     */
    public boolean hasQuestion(UUID questionId) {
        if (questionId == null) {
            return false;
        }
        return this.questions.stream()
                .anyMatch(q -> q.getId().equals(questionId));
    }

    /**
     * Checks if an offer with the given ID exists in this task.
     *
     * @param offerId the ID of the offer to check
     * @return true if the offer exists, false otherwise
     */
    public boolean hasOffer(UUID offerId) {
        if (offerId == null) {
            return false;
        }
        return this.offers.stream()
                .anyMatch(o -> o.getId().equals(offerId));
    }



}
