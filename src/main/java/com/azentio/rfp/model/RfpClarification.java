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

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "query_id")
    private Long queryId;

    @Column(name = "requirement_id")
    private String requirementId;

    @Column(name = "section_name")
    private String sectionName;

    @Column(name = "rfp_title")
    private String rfpTitle;

    @Column(name = "status")
    @Builder.Default
    private String status = "PENDING_CUSTOMER_FEEDBACK";

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
