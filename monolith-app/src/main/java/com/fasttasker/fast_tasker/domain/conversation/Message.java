package com.fasttasker.fast_tasker.domain.conversation;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString(exclude = "conversation")
@Builder(toBuilder = true)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * add conversation attribute for bidirectional relation with the conversation (father)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;


    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "text", column = @Column(name = "text")),
            @AttributeOverride(name = "attachmentUrl", column = @Column(name = "attachmentUrl"))
    })
    private MessageContent content;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    /**
     * moment in that is read
     */
    @Column(name = "read_at")
    private Instant readAt;

    /**
     * semantic construct
     */
    public Message(Conversation conversation, UUID senderId, MessageContent content) {
        if (conversation == null) {
            throw new IllegalArgumentException("conversation cannot be null");
        }
        if (senderId == null) {
            throw new IllegalArgumentException("senderId cannot be null");
        }
        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }

        this.id = UUID.randomUUID();
        this.conversation = conversation;
        this.senderId = senderId;
        this.content = content;
        this.sentAt = Instant.now();
    }

    /**
     * mark as read in this time
     */
    public void markAsRead() {
        if (readAt == null) {
            readAt = Instant.now();
        }
    }

    public boolean isRead() {
        return readAt != null;
    }
}
