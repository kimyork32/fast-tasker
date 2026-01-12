package com.fasttasker.fast_tasker.persistence.repository;

import com.fasttasker.fast_tasker.domain.conversation.Conversation;
import com.fasttasker.fast_tasker.domain.conversation.IConversationRepository;
import com.fasttasker.fast_tasker.persistence.jpa.JpaConversationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ConversationRepositoryImpl implements IConversationRepository {

    private final JpaConversationRepository jpa;

    public ConversationRepositoryImpl(JpaConversationRepository jpaConversationRepository) {
        this.jpa = jpaConversationRepository;
    }

    @Override
    public Conversation save(Conversation conversation) {
        return jpa.save(conversation);
    }

    @Override
    public Optional<Conversation> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Conversation> findByTaskId(UUID taskId) {
        return  jpa.findByTaskId(taskId);
    }

    
    @Override
    public List<Conversation> findByAnyParticipantId(UUID userId) { 
        return jpa.findByAnyParticipantId(userId);
    }

    @Override
    public boolean existsByTaskId(UUID taskId){
        return jpa.existsBytaskId(taskId);
    }
}
