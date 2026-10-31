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
public class GapAnalysisReportDTO {
    private String rfpTitle;
    private String clientName;
    private int totalRequirements;
    
    // Gap counts
    private int confirmedProductGaps; // Real limitation in current release
    private int knowledgeGaps;        // Insufficient indexed documentation
    private int functionalityGaps;
    private int integrationDependencies;
    private int securityConcerns;
    private int missingDocumentation;

    // Escalation counts by Department
    private int productManagementEscalations;
    private int engineeringEscalations;
    private int architectureEscalations;
    private int shariahEscalations;
    private int commercialEscalations;

    // Detailed Gap Items
    private List<GapItemDTO> gapItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GapItemDTO {
        private Long queryId;
        private String requirementId;
        private String sectionName;
        private String requirementText;
        private String gapNature; // PRODUCT_GAP vs KNOWLEDGE_GAP
        private String gapType;   // FUNCTIONALITY_GAP, INTEGRATION_GAP, SECURITY_CONCERN, MISSING_DOCS
        private String gapExplanation;
        private String recommendedAction;
        private String assignedDepartment; // PRODUCT_MANAGEMENT, SOLUTION_ARCHITECTURE, CORE_ENGINEERING, SHARIAH_BOARD
        private String suggestedResponseCode; // e.g. D, T, N
        private Integer confidenceScore;
    }
}
