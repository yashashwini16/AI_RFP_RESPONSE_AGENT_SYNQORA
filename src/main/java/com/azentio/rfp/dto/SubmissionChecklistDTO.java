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
public class SubmissionChecklistDTO {
    private String rfpTitle;
    private String clientName;
    private String submissionDeadline;
    private int readinessScore; // 0 to 100%
    private String overallStatus; // READY_FOR_SUBMISSION, BLOCKED, ACTION_REQUIRED
    
    private int totalClauses;
    private int completedClauses;
    private int approvedClauses;
    private int pendingReviewClauses;
    private int blockingIssuesCount;

    private List<ChecklistItemDTO> checklistItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChecklistItemDTO {
        private String category; // MANDATORY_COVERAGE, CODE_ALIGNMENT, SIZING_ESTIMATES, PARTNER_DISCLOSURES, CLARIFICATIONS
        private String title;
        private String status; // PASS, FAIL, WARNING
        private String detail;
        private boolean isBlocking;
    }
}
