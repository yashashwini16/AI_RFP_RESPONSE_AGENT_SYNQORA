package com.azentio.rfp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rfp_clarifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpClarification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "query_id")
    private Long queryId;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "clarification_needed", nullable = false, columnDefinition = "TEXT")
    private String clarificationNeeded;

    @Column(name = "reason_justification", nullable = false, columnDefinition = "TEXT")
    private String reasonJustification;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
