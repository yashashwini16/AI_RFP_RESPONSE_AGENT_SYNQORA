package com.azentio.rfp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpQueryRequest {

    @NotBlank(message = "Question text cannot be blank")
    private String question;

    @Builder.Default
    private String rfpTitle = "Qatar Islamic Bank Core Modernization RFP";

    @Builder.Default
    private String clientName = "Qatar Islamic Bank";

    @Builder.Default
    private String sectionName = "Islamic Financing Modules";

    @Builder.Default
    private String category = "MURABAHA";

    @Builder.Default
    private String requirementType = "MANDATORY"; // MANDATORY, OPTIONAL, INFO

    @Builder.Default
    private boolean retrievalOnly = false;
}
