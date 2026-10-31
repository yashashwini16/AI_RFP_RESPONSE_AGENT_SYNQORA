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

    // =========================================================================
    // LEVEL 2 FIELDS
    // =========================================================================

    @Column(name = "response_code", length = 10)
    @Builder.Default
    private String responseCode = "S"; // S, C, D, T, N (or customer specific)

    @Column(name = "response_code_desc", length = 150)
    @Builder.Default
    private String responseCodeDesc = "Standard capability";

    @Column(name = "multi_product_components", columnDefinition = "TEXT")
    private String multiProductComponents; // e.g. "iMAL Core Banking, Azentio Digital Banking"

    @Column(name = "dependencies", columnDefinition = "TEXT")
    private String dependencies; // e.g. "REQ-CORE-01"

    @Column(name = "ambiguity_flag")
    @Builder.Default
    private Boolean ambiguityFlag = false;

    @Column(name = "ambiguity_notes", columnDefinition = "TEXT")
    private String ambiguityNotes;

    @Column(name = "gap_type", length = 50)
    @Builder.Default
    private String gapType = "NONE"; // NONE, FUNCTIONALITY_GAP, INTEGRATION_GAP, SECURITY_CONCERN, MISSING_DOCS

    @Column(name = "gap_nature", length = 50)
    @Builder.Default
    private String gapNature = "NONE"; // NONE, PRODUCT_GAP, KNOWLEDGE_GAP

    @Column(name = "assigned_department", length = 50)
    @Builder.Default
    private String assignedDepartment = "SALES_ENGINEERING"; // PRODUCT_MANAGEMENT, SOLUTION_ARCHITECTURE, CORE_ENGINEERING, SHARIAH_BOARD, COMMERCIAL_LEGAL, SALES_ENGINEERING

    @Column(name = "requires_estimate")
    @Builder.Default
    private Boolean requiresEstimate = false;

    @Column(name = "estimate_details", length = 100)
    private String estimateDetails; // e.g. "15 Man-Days"

    @Column(name = "requires_attachment")
    @Builder.Default
    private Boolean requiresAttachment = false;

    @Column(name = "partner_dependency", length = 150)
    private String partnerDependency; // e.g. "Bursa Malaysia FIX Gateway"

    @Column(name = "validation_status", length = 50)
    @Builder.Default
    private String validationStatus = "PASSED"; // PASSED, WARNING, FAILED_MISMATCH, FAILED_UNSUPPORTED

    @Column(name = "validation_issues", columnDefinition = "TEXT")
    private String validationIssues;

    @Column(name = "human_validation_required")
    @Builder.Default
    private Boolean humanValidationRequired = false;
}
