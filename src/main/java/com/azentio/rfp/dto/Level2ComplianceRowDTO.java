package com.azentio.rfp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Level2ComplianceRowDTO {
    private Long queryId;
    private String requirementId;
    private String clauseNumber;
    private String sectionName;
    private String requirementText;
    private String category;
    private String requirementType; // MANDATORY, OPTIONAL

    // Customer Compliance Code & Classification
    private String responseCode; // S, C, D, T, N
    private String classification; // Standard capability, Configuration required, etc.
    private Integer confidenceScore;
    private Boolean humanValidationRequired;

    // Narrative & Sizing
    private String responseSummary;
    private String fullAnswer;
    private String assignedDepartment; // PRODUCT_MANAGEMENT, SOLUTION_ARCHITECTURE, CORE_ENGINEERING, SHARIAH_BOARD, SALES_ENGINEERING, COMMERCIAL_LEGAL
    private String multiProductComponents;
    private String dependencies;

    // Disclosures & Estimates
    private Boolean requiresEstimate;
    private String estimateDetails;
    private Boolean requiresAttachment;
    private String partnerDependency;

    // Forensic Validation Status
    private String validationStatus; // PASSED, WARNING, FAILED_MISMATCH, FAILED_UNSUPPORTED
    private String validationIssues;

    private List<HistoricalMatchDTO> citations;
}
