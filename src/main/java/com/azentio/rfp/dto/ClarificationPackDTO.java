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
public class ClarificationPackDTO {
    private String rfpTitle;
    private String clientName;
    private int totalClarifications;
    private int highPriorityCount;
    private int solutionDesignImpactCount;
    private int deliveryTimelineImpactCount;
    private int commercialScopeImpactCount;
    
    // Grouped and deduplicated clarification clusters
    private List<ClarificationClusterDTO> clusters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClarificationClusterDTO {
        private String clusterName; // e.g. "Payment Gateway & Network Protocol"
        private String impactPriority; // HIGH, MEDIUM, LOW
        private String impactArea;     // SOLUTION_DESIGN, DELIVERY_TIMELINE, COMMERCIAL_SCOPE
        private String consolidatedClarificationQuestion;
        private String technicalJustification;
        private String workingAssumption;
        private List<String> affectedRequirementIds;
    }
}
