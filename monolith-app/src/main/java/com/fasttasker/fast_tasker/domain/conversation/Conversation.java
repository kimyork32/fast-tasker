package com.fasttasker.fast_tasker.domain.conversation;

import com.fasttasker.common.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * one-to-one conversation (poster to tasker)
 */
@Entity
@Table(name = "conversation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(exclude = "messages")
@EqualsAndHashCode(exclude = "messages")
public class Conversation {

    @Id
    private UUID id;

    /**
     * task is the context for the conversation
     */
    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "participant_a_id", nullable = false)
    private UUID participantA;

    @Column(name = "participant_b_id", nullable = false)
    private UUID participantB;

    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Message> messages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConversationStatus status;

    // ============================================================================
    // CONSTRUCTOR with validations (DDD semantic constructor)
    // ============================================================================

    @Builder(toBuilder = true)
    public Conversation(UUID taskId, UUID participantA, UUID participantB) {
        validateParticipants(participantA, participantB);
        validateTaskId(taskId);

        this.id = UUID.randomUUID();
        this.taskId = taskId;
        this.participantA = participantA;
        this.participantB = participantB;
        this.status = ConversationStatus.ACTIVE;
    }

    // ============================================================================
    // ENCAPSULATION: Getter that returns unmodifiable collection
    // ============================================================================

    /**
     * Returns an unmodifiable view of the messages list.
     * External code cannot modify the internal state through this getter.
     *
     * @return unmodifiable list of messages
     */
    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    // ============================================================================
    // BUSINESS METHODS: Controlled manipulation of internal state
    // ============================================================================

    /**
     * Business method to send a message in this conversation.
     * Encapsulates all validation logic and state management.
     *
     * @param senderId ID of the sender
     * @param content content of the message
     * @throws DomainException if the conversation is closed or sender is not a participant
     */
    public void sendMessage(UUID senderId, MessageContent content) {
        if (content == null) {
            throw new DomainException("Message content cannot be null");
        }

        validateConversationIsActive();
        validateSenderIsParticipant(senderId);

        Message newMessage = new Message(this, senderId, content);
        this.messages.add(newMessage);
    }

    /**
     * Business method to add a message to the conversation.
     * This is useful when creating messages from the database or tests.
     *
     * @param message the message to add
     * @throws DomainException if the message is null or conversation is closed
     */
    public void addMessage(Message message) {
        if (message == null) {
            throw new DomainException("Message cannot be null");
        }

        validateConversationIsActive();

        this.messages.add(message);
    }

    /**
     * Business method to close this conversation.
     * Once closed, no more messages can be sent.
     *
     * @throws DomainException if the conversation is already closed
     */
    public void close() {
        if (this.status == ConversationStatus.CLOSED) {
            throw new DomainException("Conversation is already closed");
        }
        this.status = ConversationStatus.CLOSED;
    }

    /**
     * Business method to reopen a closed conversation.
     *
     * @throws DomainException if the conversation is already active
     */
    public void reopen() {
        if (this.status == ConversationStatus.ACTIVE) {
            throw new DomainException("Conversation is already active");
        }
        this.status = ConversationStatus.ACTIVE;
    }

    /**
     * Returns the ID of the other participant in the conversation.
     *
     * @param userId ID of one participant
     * @return ID of the other participant
     * @throws DomainException if the userId is not a participant
     */
    public UUID otherParticipantId(UUID userId) {
        if (userId == null) {
            throw new DomainException("User ID cannot be null");
        }

        if (participantA.equals(userId)) {
            return participantB;
        }
        if (participantB.equals(userId)) {
            return participantA;
        }

        throw new DomainException("User is not a participant in this conversation");
    }

    /**
     * Checks if a user is a participant in this conversation.
     *
     * @param userId the user ID to check
     * @return true if the user is a participant, false otherwise
     */
    public boolean isParticipant(UUID userId) {
        if (userId == null) {
            return false;
        }
        return participantA.equals(userId) || participantB.equals(userId);
    }

    /**
     * Gets the total number of messages in this conversation.
     *
     * @return the message count
     */
    public int getMessageCount() {
        return this.messages.size();
    }

    // ============================================================================
    // PRIVATE VALIDATION METHODS
    // ============================================================================

    private void validateParticipants(UUID participantA, UUID participantB) {
        if (participantA == null || participantB == null) {
            throw new DomainException("Participants cannot be null");
        }
        if (participantA.equals(participantB)) {
            throw new DomainException("Participants must be different");
        }
    }

    private void validateTaskId(UUID taskId) {
        if (taskId == null) {
            throw new DomainException("Task ID cannot be null");
        }
    }

    private void validateConversationIsActive() {
        if (this.status == ConversationStatus.CLOSED) {
            throw new DomainException("Cannot send messages in a closed conversation");
        }
    }

    private void validateSenderIsParticipant(UUID senderId) {
        if (!isParticipant(senderId)) {
            throw new DomainException(
                    "User " + senderId + " is not a participant of this conversation"
            );
        }
    }
}