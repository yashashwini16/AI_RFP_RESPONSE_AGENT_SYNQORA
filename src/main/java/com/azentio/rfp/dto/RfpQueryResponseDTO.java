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
    private String rfpTitle;
    private String clientName;
    private String sectionName;
    private String question;
    private String category;
    private String status;

    // Challenge B Triage & Guardrail Fields
    private String requirementType; // MANDATORY, OPTIONAL, INFO
    private String complianceStatus; // COMPLIANT, PARTIALLY_COMPLIANT, NON_COMPLIANT, CLARIFICATION
    private String guardrailFlag; // TECHNICAL, LEGAL_REVIEW, PRICING_DESK, SLA_COMMITMENT
    private String guardrailReason;
    private String assignedOwner; // SE_TECHNICAL, LEGAL_COUNSEL, COMMERCIAL_DESK
    private String outdatedWarning;

    // Grounded Generation & Citations
    private String draftedAnswer;
    private Integer confidenceScore; // 0 to 100
    private Boolean gapFlag; // true if confidence < 50%
    private List<HistoricalMatchDTO> citations; // Top retrieved sources across 3 categories

    // Collaboration & Approval State
    private String reviewedAnswer;
    private Boolean isApproved;

    // Latency & Token Metrics
    private Integer latencyMs;
    private String modelUsed;
}
