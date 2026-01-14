package com.fasttasker.fast_tasker.domain.task;

import com.fasttasker.common.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * it represents a question (Entity) asked about a specific task.
 * it is property of the Task AR
 */
@Entity
@Table(name = "question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(exclude = "answers")
public class Question {

    @Id
    private UUID id;

    /**
     * content of the question
     * max size of 500 characters
     */
    @Column(name = "description", length = 500, nullable = false)
    private String description;

    /**
     * status of the question
     * { PENDING, ANSWERED, DELETED }
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QuestionStatus status;

    /**
     * the id of the tasker that asked the question
     */
    @Column(name = "asked_by_id", nullable = false)
    private UUID askedById;

    /**
     * date and time the question was created
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @ToString.Exclude
    private Task task;

    /**
     * accepts multiple answers
     */
    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private final List<Answer> answers = new ArrayList<>();

    @Builder(toBuilder = true)
    public Question(String description, UUID askedById, Task task) {
        validateDescription(description);
        if (askedById == null) {
            throw new IllegalArgumentException("AskedById cannot be null");
        }
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        this.id = UUID.randomUUID();
        this.description = description;
        this.askedById = askedById;
        this.task = task;
        this.status = QuestionStatus.PENDING;
        this.createdAt = Instant.now();
    }

    // ============================================================================
    // ENCAPSULATION: Getter that returns unmodifiable collection
    // ============================================================================

    /**
     * Returns an unmodifiable view of the answers list.
     * External code cannot modify the internal state through this getter.
     *
     * @return unmodifiable list of answers
     */
    public List<Answer> getAnswers() {
        return Collections.unmodifiableList(answers);
    }

    // ============================================================================
    // BUSINESS METHODS
    // ============================================================================

    /**
     * Business method to add an answer to this question.
     * This is the method that Task.answerQuestion() calls.
     *
     * @param answer the answer to add
     * @throws DomainException if the answer is null or question is already answered
     */
    public void addAnswer(Answer answer) {
        if (answer == null) {
            throw new DomainException("Answer cannot be null");
        }
        if (this.status == QuestionStatus.DELETED) {
            throw new DomainException("Cannot answer a deleted question");
        }

        this.answers.add(answer);
        this.status = QuestionStatus.ANSWERED;
    }

    public void editDescription(String newDescription) {
        validateDescription(newDescription);

        if (this.status == QuestionStatus.ANSWERED) {
            throw new DomainException("Cannot edit a question that has already been answered");
        }

        this.description = newDescription;
    }

    /**
     * Legacy method - kept for backward compatibility.
     * Consider using addAnswer() instead for better encapsulation.
     */
    public void postAnswer(String answerContent, UUID responderId) {
        var newAnswer = Answer.builder()
                .description(answerContent)
                .responderId(responderId)
                .question(this)
                .build();

        this.addAnswer(newAnswer);
    }

    public void markAsDeleted() {
        this.status = QuestionStatus.DELETED;
    }

    private void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new DomainException("Question description cannot be empty");
        }
        if (description.length() > 500) {
            throw new DomainException("Question description exceeds 500 characters");
        }
    }
}