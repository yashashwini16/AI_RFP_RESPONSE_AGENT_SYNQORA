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
    private String category;

    @Column(name = "requirement_type", length = 20)
    @Builder.Default
    private String requirementType = "MANDATORY"; // MANDATORY, OPTIONAL, INFO

    @Column(name = "compliance_status", length = 20)
    @Builder.Default
    private String complianceStatus = "COMPLIANT"; // COMPLIANT, PARTIALLY_COMPLIANT, NON_COMPLIANT, CLARIFICATION

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
