package com.azentio.rfp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpQueryRequest {

    private String requirementId; // e.g. REQ-FN-01
    private Long documentId;

    @NotBlank(message = "Question text cannot be blank")
    private String question;

    @Builder.Default
    private String rfpTitle = "Azentio Core Banking & Islamic Lending RFP";

    @Builder.Default
    private String clientName = "Tier-1 Enterprise Bank";

    @Builder.Default
    private String sectionName = "Functional Requirements";

    @Builder.Default
    private String category = "FUNCTIONAL"; // FUNCTIONAL, TECHNICAL, SECURITY, COMPLIANCE, INTEGRATION, COMMERCIAL

    @Builder.Default
    private String requirementType = "MANDATORY"; // MANDATORY, OPTIONAL

    @Builder.Default
    private String questionType = "EXPLANATION"; // FACTUAL, EXPLANATION, EVIDENCE, CONFIRMATION

    @Builder.Default
    private boolean retrievalOnly = false;
}
