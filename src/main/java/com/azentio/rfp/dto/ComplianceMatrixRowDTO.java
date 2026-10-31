package com.azentio.rfp.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceMatrixRowDTO {
    private Long queryId;
    private String clauseNumber;
    private String sectionName;
    private String requirementText;
    private String requirementType; // MANDATORY, OPTIONAL
    private String complianceStatus; // COMPLIANT, PARTIALLY_COMPLIANT, NON_COMPLIANT, CLARIFICATION
    private String responseSummary;
    private String assignedOwner;
    private List<HistoricalMatchDTO> citations;
}
