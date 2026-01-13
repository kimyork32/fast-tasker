package com.fasttasker.fast_tasker.domain.task;

import com.fasttasker.common.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;
/**
 * answer of the question
 */
@Entity
@Table(name = "answer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode
public class Answer {

    @Id
    private UUID id;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    @Column(name = "responder_id", nullable = false)
    private UUID responderId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @ToString.Exclude
    private Question question;

    @Builder(toBuilder = true)
    public Answer(String description, UUID responderId, Question question) {
        if (description == null || description.trim().isEmpty()) {
            throw new DomainException("Description cannot be null or empty");
        }
        if (description.length() > 500) {
            throw new DomainException("Description exceeds 500 characters");
        }
        if (responderId == null) {
            throw new DomainException("AnsweredId cannot be null");
        }
        if (question == null) {
            throw new DomainException("Question cannot be null");
        }
        this.id = UUID.randomUUID();
        this.description = description;
        this.responderId = responderId;
        this.createdAt = Instant.now();
        this.question = question;
    }

    public void editDescription(String newDescription) {
        validateDescription(newDescription);
        this.description = newDescription;
    }

    private void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new DomainException("Answer description cannot be empty");
        }
        if (description.length() > 500) {
            throw new DomainException("Answer description exceeds 500 characters");
        }
    }
}