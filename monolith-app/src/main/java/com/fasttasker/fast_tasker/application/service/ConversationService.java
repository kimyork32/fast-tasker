package com.fasttasker.fast_tasker.application.service;

import com.fasttasker.fast_tasker.application.dto.conversation.ConversationRequest;
import com.fasttasker.fast_tasker.application.dto.conversation.ConversationSummary;
import com.fasttasker.fast_tasker.application.dto.conversation.MessageRequest;
import com.fasttasker.fast_tasker.application.dto.conversation.MessageResponse;
import com.fasttasker.fast_tasker.application.dto.tasker.ChatProfileResponse;
import com.fasttasker.fast_tasker.application.exception.ConversationNotFoundException;
import com.fasttasker.fast_tasker.application.exception.TaskerNotFoundException;
import com.fasttasker.fast_tasker.application.mapper.ConversationMapper;
import com.fasttasker.fast_tasker.application.mapper.TaskerMapper;
import com.fasttasker.fast_tasker.domain.conversation.Conversation;
import com.fasttasker.fast_tasker.domain.conversation.IConversationRepository;
import com.fasttasker.fast_tasker.domain.conversation.Message;
import com.fasttasker.fast_tasker.domain.conversation.MessageContent;
import com.fasttasker.fast_tasker.domain.tasker.ITaskerRepository;
import com.fasttasker.fast_tasker.domain.tasker.Tasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ConversationService {
    private final IConversationRepository conversationRepository;
    private final ConversationMapper conversationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final TaskerMapper taskerMapper;
    private final ITaskerRepository taskerRepository;

    public ConversationService(IConversationRepository conversationRepository, ConversationMapper conversationMapper,
                               SimpMessagingTemplate messagingTemplate, TaskerMapper taskerMapper, ITaskerRepository taskerRepository) {
        this.conversationRepository = conversationRepository;
        this.conversationMapper = conversationMapper;
        this.messagingTemplate = messagingTemplate;
        this.taskerMapper = taskerMapper;
        this.taskerRepository = taskerRepository;
    }

    /**
     * initialize a new conversation
     * @param conversationRequest request fron frontend
     * @return conversation id
     */
    @Transactional
    public UUID startConversation(ConversationRequest conversationRequest) {
        // find conversation by taskId. If it exists, return its ID
        // If it doesn't exist, create a new one, save it, and return the new ID
        return conversationRepository.findByTaskId(conversationRequest.taskId())
                .map(Conversation::getId) // if present, get the existing ID
                .orElseGet(() -> { // if not present, create a new conversation
                    Conversation newConversation = conversationMapper.toConversationEntity(conversationRequest);
                    return conversationRepository.save(newConversation).getId();
                });
    }

    /**
     * get tasker's inbox
     * @param accountId user id
     * @return list of conversations
     */
    @Transactional(readOnly = true)
    public List<ConversationSummary> getUserInbox(UUID taskerId) {

        log.info("taskerId: {}", taskerId);

        // Refactoring: Use optimized repository query (Move Method) and Extract Method for mapping
        return conversationRepository.findByAnyParticipantId(taskerId).stream()
                .map(c -> buildConversationSummary(c, taskerId))
                .toList();
    }

    /**
     * Helper method extracted to map conversation to summary (Extract Method)
     * and handle potential empty message lists (Simplify Conditional Expressions).
     */
    private ConversationSummary buildConversationSummary(Conversation c, UUID taskerId) {
        // calculate other id
        UUID otherId = c.otherParticipantId(taskerId);

        // get last messageContent with robustness check (Simplify Conditional)
        String snippet = c.getMessages().isEmpty() 
                ? "No messages yet" 
                : c.getMessages().getLast().getContent().snippet();

        // build profile of the tasker for chat
        Tasker tasker = taskerRepository.findById(otherId);
        ChatProfileResponse profile = taskerMapper.toChatProfileResponse(tasker);

        return ConversationSummary.builder()
                .conversationId(c.getId())
                .taskId(c.getTaskId())
                .otherParticipantId(otherId)
                .lastMessageSnippet(snippet)
                .profile(profile)
                .build();
    }

    /**
     * get message history
     * @param conversationId conversation id
     * @return list of messages
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getHistory(UUID conversationId, UUID requesterId) {
        Conversation c = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation Not Found"));
        log.info("conversation: {}", c.toString());
        // verify that the user requesting the history is a participant in the conversation
        boolean isParticipant = c.getParticipantA().equals(requesterId) || c.getParticipantB().equals(requesterId);
        if (!isParticipant) {
            // throw an exception that will be handled to return a 403 Forbidden or 404 Not Found
            // using ConversationNotFoundException prevents leaking information that the conversation exists
            throw new ConversationNotFoundException("Conversation not found or access denied");
        }

        return c.getMessages().stream()
                .map(conversationMapper::toMessageResponse)
                .toList();
    }

    /**
     * processes an incoming message and sends it to subscribed WebSocket clients.
     * @param messageRequest messageRequest the incoming message data containing conversation ID and content
     * @param senderIdFromToken senderIdFromToken the ID of the authenticated user sending the message
     */
    @Transactional
    public void processAndSendMessage(MessageRequest messageRequest, UUID senderIdFromToken) {
        Conversation conversation = conversationRepository.findById(messageRequest.conversationId())
                .orElseThrow(() -> new ConversationNotFoundException("Conversation Not Found"));

        MessageContent messageContent = conversationMapper.toMessageContentEntity(messageRequest.content());

        // save message into the conversation
        conversation.sendMessage(senderIdFromToken, messageContent);

        // save conversation in the db
        Conversation savedConversation = conversationRepository.save(conversation);

        // get the last message was created
        Message newMessage = savedConversation.getMessages().getLast();

        // convert last message to DTO
        MessageResponse messageResponse = conversationMapper.toMessageResponse(newMessage);

        // WEBSOCKET is used
        // Only users subscribed to this specific ID will receive the message
        String destination = "/topic/conversation." + messageRequest.conversationId();

        // send JSON to connected clients
        messagingTemplate.convertAndSend(destination, messageResponse);
    }
}
