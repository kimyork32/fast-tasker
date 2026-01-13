package com.fasttasker.fast_tasker.domain.conversation;

import com.fasttasker.common.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversationTest {

    private UUID validTaskId;
    private UUID participantA;
    private UUID participantB;

    @BeforeEach
    void setUp() {
        validTaskId = UUID.randomUUID();
        participantA = UUID.randomUUID();
        participantB = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Constructor and Validation Tests")
    class ConstructorTests {

        @Test
        void shouldCreateConversationSuccessfully() {
            Conversation conversation = new Conversation(validTaskId, participantA, participantB);

            assertThat(conversation).isNotNull();
            assertThat(conversation.getId()).isNotNull();
            assertThat(conversation.getTaskId()).isEqualTo(validTaskId);
            assertThat(conversation.getParticipantA()).isEqualTo(participantA);
            assertThat(conversation.getParticipantB()).isEqualTo(participantB);
            assertThat(conversation.getStatus()).isEqualTo(ConversationStatus.ACTIVE);
            assertThat(conversation.getMessages()).isEmpty();
        }

        @Test
        void shouldThrowWhenParticipantAIsNull() {
            assertThatThrownBy(() -> new Conversation(validTaskId, null, participantB))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Participants cannot be null");
        }

        @Test
        void shouldThrowWhenParticipantBIsNull() {
            assertThatThrownBy(() -> new Conversation(validTaskId, participantA, null))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Participants cannot be null");
        }

        @Test
        void shouldThrowWhenParticipantsAreTheSame() {
            assertThatThrownBy(() -> new Conversation(validTaskId, participantA, participantA))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Participants must be different");
        }

        @Test
        void shouldThrowWhenTaskIdIsNull() {
            assertThatThrownBy(() -> new Conversation(null, participantA, participantB))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Task ID cannot be null");
        }
    }

    @Nested
    @DisplayName("Encapsulation Tests")
    class EncapsulationTests {
        @Test
        void getMessagesShouldReturnUnmodifiableList() {
            Conversation conversation = new Conversation(validTaskId, participantA, participantB);
            var messages = conversation.getMessages();
            var mockMessage = Mockito.mock(Message.class);
            assertThatThrownBy(() -> messages.add(mockMessage))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Business Method Tests")
    class BusinessMethodTests {

        private Conversation conversation;
        private MessageContent mockContent;

        @BeforeEach
        void init() {
            conversation = new Conversation(validTaskId, participantA, participantB);
            mockContent = Mockito.mock(MessageContent.class);
        }

        @Test
        void shouldSendMessageSuccessfully() {
            conversation.sendMessage(participantA, mockContent);

            assertThat(conversation.getMessages()).hasSize(1);
            assertThat(conversation.getMessages().getFirst().getSenderId()).isEqualTo(participantA);
            assertThat(conversation.getMessages().getFirst().getContent()).isEqualTo(mockContent);
        }

        @Test
        void shouldThrowWhenSendingMessageWithNullContent() {
            assertThatThrownBy(() -> conversation.sendMessage(participantA, null))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Message content cannot be null");
        }

        @Test
        void shouldThrowWhenSendingMessageInClosedConversation() {
            conversation.close();
            assertThatThrownBy(() -> conversation.sendMessage(participantA, mockContent))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Cannot send messages in a closed conversation");
        }

        @Test
        void shouldThrowWhenSenderIsNotParticipant() {
            UUID nonParticipantId = UUID.randomUUID();
            assertThatThrownBy(() -> conversation.sendMessage(nonParticipantId, mockContent))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("User " + nonParticipantId + " is not a participant of this conversation");
        }

        @Test
        void shouldAddMessageSuccessfully() {
            Message mockMessage = Mockito.mock(Message.class);
            conversation.addMessage(mockMessage);

            assertThat(conversation.getMessages()).contains(mockMessage);
        }

        @Test
        void shouldThrowWhenAddingNullMessage() {
            assertThatThrownBy(() -> conversation.addMessage(null))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Message cannot be null");
        }

        @Test
        void shouldThrowWhenAddingMessageToClosedConversation() {
            conversation.close();
            Message mockMessage = Mockito.mock(Message.class);
            assertThatThrownBy(() -> conversation.addMessage(mockMessage))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Cannot send messages in a closed conversation");
        }

        @Test
        void shouldCloseConversationSuccessfully() {
            conversation.close();
            assertThat(conversation.getStatus()).isEqualTo(ConversationStatus.CLOSED);
        }

        @Test
        void shouldThrowWhenClosingAlreadyClosedConversation() {
            conversation.close();
            assertThatThrownBy(conversation::close)
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Conversation is already closed");
        }

        @Test
        void shouldReopenConversationSuccessfully() {
            conversation.close(); // Must be closed to reopen
            conversation.reopen();
            assertThat(conversation.getStatus()).isEqualTo(ConversationStatus.ACTIVE);
        }

        @Test
        void shouldThrowWhenReopeningAlreadyActiveConversation() {
            assertThatThrownBy(conversation::reopen)
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Conversation is already active");
        }

        @Test
        void otherParticipantIdShouldReturnCorrectIdForParticipantA() {
            assertThat(conversation.otherParticipantId(participantA)).isEqualTo(participantB);
        }

        @Test
        void otherParticipantIdShouldReturnCorrectIdForParticipantB() {
            assertThat(conversation.otherParticipantId(participantB)).isEqualTo(participantA);
        }

        @Test
        void otherParticipantIdShouldThrowWhenUserIdIsNull() {
            assertThatThrownBy(() -> conversation.otherParticipantId(null))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("User ID cannot be null");
        }

        @Test
        void otherParticipantIdShouldThrowWhenUserIsNotParticipant() {
            UUID nonParticipantId = UUID.randomUUID();
            assertThatThrownBy(() -> conversation.otherParticipantId(nonParticipantId))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("User is not a participant in this conversation");
        }

        @Test
        void isParticipantShouldReturnTrueForParticipantA() {
            assertThat(conversation.isParticipant(participantA)).isTrue();
        }

        @Test
        void isParticipantShouldReturnTrueForParticipantB() {
            assertThat(conversation.isParticipant(participantB)).isTrue();
        }

        @Test
        void isParticipantShouldReturnFalseForNonParticipant() {
            assertThat(conversation.isParticipant(UUID.randomUUID())).isFalse();
        }

        @Test
        void isParticipantShouldReturnFalseForNullUserId() {
            assertThat(conversation.isParticipant(null)).isFalse();
        }

        @Test
        void getMessageCountShouldReturnCorrectSize() {
            assertThat(conversation.getMessageCount()).isZero();
            conversation.sendMessage(participantA, mockContent);
            assertThat(conversation.getMessageCount()).isEqualTo(1);
            conversation.sendMessage(participantB, mockContent);
            assertThat(conversation.getMessageCount()).isEqualTo(2);
        }
    }
}