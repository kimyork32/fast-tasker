package com.fasttasker.fast_tasker.application.service;

import com.fasttasker.fast_tasker.application.dto.conversation.*;
import com.fasttasker.fast_tasker.application.exception.ConversationNotFoundException;
import com.fasttasker.fast_tasker.application.mapper.ConversationMapper;
import com.fasttasker.fast_tasker.application.mapper.TaskerMapper;
import com.fasttasker.fast_tasker.domain.conversation.Conversation;
import com.fasttasker.fast_tasker.domain.conversation.IConversationRepository;
import com.fasttasker.fast_tasker.domain.conversation.Message;
import com.fasttasker.fast_tasker.domain.conversation.MessageContent;
import com.fasttasker.fast_tasker.domain.tasker.ITaskerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private IConversationRepository conversationRepository;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private TaskerMapper taskerMapper;

    @Mock
    private ITaskerRepository taskerRepository;

    @InjectMocks
    private ConversationService conversationService;

    private UUID taskId;
    private UUID participantA;
    private UUID participantB;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        participantA = UUID.randomUUID();
        participantB = UUID.randomUUID();
        // Use builder as per user preference for "what I have so far"
        conversation = Conversation.builder()
                .taskId(taskId)
                .participantA(participantA)
                .participantB(participantB)
                .build();
    }

    @Nested
    @DisplayName("Start Conversation Tests")
    class StartConversationTests {

        @Test
        void shouldReturnExistingConversationIdWhenFound() {
            // GIVEN
            ConversationRequest request = new ConversationRequest(taskId, participantA, participantB);
            when(conversationRepository.findByTaskId(taskId)).thenReturn(Optional.of(conversation));

            // WHEN
            UUID resultId = conversationService.startConversation(request);

            // THEN
            assertThat(resultId).isEqualTo(conversation.getId());
            verify(conversationRepository, never()).save(any(Conversation.class));
        }

        @Test
        void shouldCreateNewConversationWhenNotFound() {
            // GIVEN
            ConversationRequest request = new ConversationRequest(taskId, participantA, participantB);
            when(conversationRepository.findByTaskId(taskId)).thenReturn(Optional.empty());
            when(conversationMapper.toConversationEntity(request)).thenReturn(conversation);
            when(conversationRepository.save(conversation)).thenReturn(conversation);

            // WHEN
            UUID resultId = conversationService.startConversation(request);

            // THEN
            assertThat(resultId).isEqualTo(conversation.getId());
            verify(conversationRepository).save(conversation);
        }
    }

    @Nested
    @DisplayName("Get History Tests")
    class GetHistoryTests {

        @Test
        void shouldReturnMessageHistoryForParticipant() {
            // GIVEN
            UUID conversationId = conversation.getId();
            UUID requesterId = participantA;

            // Add a message
            MessageContent content = new MessageContent("Test message", null);
            conversation.sendMessage(participantA, content);
            Message message = conversation.getMessages().getFirst();

            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
            
            MessageResponse mockResponse = mock(MessageResponse.class);
            when(conversationMapper.toMessageResponse(message)).thenReturn(mockResponse);

            // WHEN
            List<MessageResponse> history = conversationService.getHistory(conversationId, requesterId);

            // THEN
            assertThat(history).hasSize(1);
            assertThat(history.getFirst()).isEqualTo(mockResponse);
        }

        @Test
        void shouldThrowWhenConversationNotFound() {
            // GIVEN
            UUID conversationId = UUID.randomUUID();
            UUID requesterId = participantA;
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> conversationService.getHistory(conversationId, requesterId))
                    .isInstanceOf(ConversationNotFoundException.class)
                    .hasMessage("Conversation Not Found");
        }

        @Test
        void shouldThrowWhenRequesterIsNotParticipant() {
            // GIVEN
            UUID conversationId = conversation.getId();
            UUID nonParticipantId = UUID.randomUUID();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

            // WHEN & THEN
            assertThatThrownBy(() -> conversationService.getHistory(conversationId, nonParticipantId))
                    .isInstanceOf(ConversationNotFoundException.class)
                    .hasMessage("Conversation not found or access denied");
        }
    }

    @Nested
    @DisplayName("Process and Send Message Tests")
    class ProcessAndSendMessageTests {

        @Test
        void shouldProcessAndSendMessageSuccessfully() {
            // GIVEN
            UUID conversationId = conversation.getId();
            UUID senderId = participantA;
            String text = "New message";
            
            MessageContentRequest contentRequest = new MessageContentRequest(text, null);
            MessageRequest messageRequest = new MessageRequest(conversationId, contentRequest);
            
            MessageContent messageContent = new MessageContent(text, null);

            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
            when(conversationMapper.toMessageContentEntity(contentRequest)).thenReturn(messageContent);
            when(conversationRepository.save(conversation)).thenReturn(conversation);
            
            MessageResponse mockResponse = mock(MessageResponse.class);
            // We need to match any Message because the exact instance is created inside the service
            when(conversationMapper.toMessageResponse(any(Message.class))).thenReturn(mockResponse);

            // WHEN
            conversationService.processAndSendMessage(messageRequest, senderId);

            // THEN
            // Verify message was added to conversation
            assertThat(conversation.getMessages()).hasSize(1);
            assertThat(conversation.getMessages().getFirst().getContent().getText()).isEqualTo(text);
            
            // Verify repository save
            verify(conversationRepository).save(conversation);
            
            // Verify WebSocket message sent
            String expectedDestination = "/topic/conversation." + conversationId;
            verify(messagingTemplate).convertAndSend(expectedDestination, mockResponse);
        }

        @Test
        void shouldThrowWhenConversationNotFoundForSending() {
            // GIVEN
            UUID conversationId = UUID.randomUUID();
            MessageRequest messageRequest = new MessageRequest(conversationId, new MessageContentRequest("text", null));

            when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> conversationService.processAndSendMessage(messageRequest, participantA))
                    .isInstanceOf(ConversationNotFoundException.class)
                    .hasMessage("Conversation Not Found");
        }
    }
}
