package com.azentio.rfp.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpQueryResponseDTO {
    private Long queryId;
    private Long responseId;
    private String requirementId; // Level 1 Traceability ID: REQ-FN-01
    private String rfpTitle;
    private String clientName;
    private String sectionName;
    private String question;
    private String category; // FUNCTIONAL, TECHNICAL, SECURITY, COMPLIANCE, INTEGRATION, COMMERCIAL
    private String status; // PENDING, DRAFTED, REVIEWED, APPROVED

    // Level 1 Classification Fields
    private String requirementType; // MANDATORY, OPTIONAL
    private String questionType; // FACTUAL, EXPLANATION, EVIDENCE, CONFIRMATION
    private String responseClassification; // Fully Addressed, Partially Addressed, Information Required, Not Supported
    private String unansweredExplanation; // Explain why requirement cannot be answered completely
    private Boolean isDuplicate; // Duplicate detection
    private Long duplicateOfId;

    // Guardrail Fields
    private String guardrailFlag; // TECHNICAL, LEGAL_REVIEW, PRICING_DESK, SLA_COMMITMENT
    private String guardrailReason;
    private String assignedOwner; // SE_TECHNICAL, LEGAL_COUNSEL, COMMERCIAL_DESK
    private String outdatedWarning;

    // Grounded Generation & Citations
    private String draftedAnswer;
    private Integer confidenceScore; // 0 to 100
    private Boolean gapFlag; // true if confidence < 50%
    private List<HistoricalMatchDTO> citations; // Exact source references used

    // Collaboration & Approval State
    private String reviewedAnswer;
    private Boolean isApproved;

    // Latency & Token Metrics
    private Integer latencyMs;
    private String modelUsed;
}
