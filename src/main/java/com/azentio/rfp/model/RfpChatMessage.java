package com.azentio.rfp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rfp_chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 50)
    private String sessionId;

    @Column(name = "query_id")
    private Long queryId;

    @Column(nullable = false, length = 20)
    private String sender; // SE, AGENT

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "action_type", length = 50)
    private String actionType; // REWRITE, APPROVAL, CLARIFICATION, EXPLANATION, GENERAL_CHAT

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
