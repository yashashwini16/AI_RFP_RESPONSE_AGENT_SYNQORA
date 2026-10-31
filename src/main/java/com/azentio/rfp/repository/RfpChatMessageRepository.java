package com.azentio.rfp.repository;

import com.azentio.rfp.model.RfpChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RfpChatMessageRepository extends JpaRepository<RfpChatMessage, Long> {
    List<RfpChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    List<RfpChatMessage> findTop20BySessionIdOrderByCreatedAtDesc(String sessionId);
}
