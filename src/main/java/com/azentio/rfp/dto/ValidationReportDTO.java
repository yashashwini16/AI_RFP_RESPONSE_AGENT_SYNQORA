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
public class ValidationReportDTO {
    private String overallStatus; // READY_TO_SUBMIT, HAS_BLOCKING_ISSUES, NEEDS_REVIEW
    private int totalRequirements;
    private int passedCount;
    private int warningCount;
    private int blockingCount;

    // Specific issue counters
    private int codeNarrativeMismatches;
    private int unsupportedClaims;
    private int crossClauseContradictions;
    private int missingMandatoryAnswers;
    private int outdatedVersionWarnings;

    private List<ValidationIssueDTO> issues;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationIssueDTO {
        private Long queryId;
        private String requirementId;
        private String sectionName;
        private String requirementText;
        private String issueType; // CODE_NARRATIVE_MISMATCH, UNSUPPORTED_CLAIM, CROSS_CONTRADICTION, MISSING_MANDATORY, OUTDATED_SOURCE
        private String severity;  // BLOCKING, WARNING, INFO
        private String codeAssigned;
        private String description;
        private String flaggedSnippet;
        private String remediationRecommendation;
    }
}
