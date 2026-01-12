package com.fasttasker.fast_tasker.persistence.jpa;

import com.fasttasker.fast_tasker.domain.conversation.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaConversationRepository extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByTaskId(UUID taskId);

    boolean existsBytaskId(UUID taskId);
    /**
     * Retrieves all conversations for a specific participant.
     * Refactoring (Move Method): Implements 'LEFT JOIN FETCH' to resolve the N+1 select problem
     * by eagerly loading the messages collection in a single database round-trip.
     */ 
    
    @Query("SELECT DISTINCT c FROM Conversation c LEFT JOIN FETCH c.messages WHERE c.participantA = :userId OR c.participantB = :userId")
    List<Conversation> findByAnyParticipantId(@Param("userId") UUID userId);
}
