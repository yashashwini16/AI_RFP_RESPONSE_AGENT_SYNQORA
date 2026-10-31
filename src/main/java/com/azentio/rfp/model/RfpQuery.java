package com.azentio.rfp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rfp_queries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "requirement_id", length = 50)
    private String requirementId; // e.g. REQ-FN-01, REQ-SEC-02

    @Column(name = "rfp_title", nullable = false, length = 100)
    private String rfpTitle;

    @Column(name = "client_name", length = 100)
    private String clientName;

    @Column(name = "section_name", length = 100)
    @Builder.Default
    private String sectionName = "General Requirements";

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(length = 50)
    private String category; // FUNCTIONAL, TECHNICAL, SECURITY, COMPLIANCE, INTEGRATION, COMMERCIAL

    @Column(name = "requirement_type", length = 20)
    @Builder.Default
    private String requirementType = "MANDATORY"; // MANDATORY, OPTIONAL

    @Column(name = "question_type", length = 30)
    @Builder.Default
    private String questionType = "EXPLANATION"; // FACTUAL, EXPLANATION, EVIDENCE, CONFIRMATION

    @Column(name = "compliance_status", length = 30)
    @Builder.Default
    private String complianceStatus = "Fully Addressed"; // Fully Addressed, Partially Addressed, Information Required, Not Supported

    @Column(name = "is_duplicate")
    @Builder.Default
    private Boolean isDuplicate = false;

    @Column(name = "duplicate_of_id")
    private Long duplicateOfId;

    @Column(name = "guardrail_flag", length = 50)
    @Builder.Default
    private String guardrailFlag = "TECHNICAL"; // TECHNICAL, LEGAL_REVIEW, PRICING_DESK, SLA_COMMITMENT

    @Column(name = "assigned_owner", length = 50)
    @Builder.Default
    private String assignedOwner = "SE_TECHNICAL"; // SE_TECHNICAL, LEGAL_COUNSEL, COMMERCIAL_DESK

    @Column(length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, DRAFTED, REVIEWED, APPROVED, NEEDS_REVIEW
}
