package com.fasttasker.fast_tasker.domain.conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IConversationRepository {

    Conversation save(Conversation conversation);

    Optional<Conversation> findById(UUID id);

    Optional<Conversation> findByTaskId(UUID taskId);

    List<Conversation> findByAnyParticipantId(UUID userId);

    boolean existsByTaskId(UUID taskId);
}
