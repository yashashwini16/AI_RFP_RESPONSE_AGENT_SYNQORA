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
    private String rfpTitle = "Azentio Core Banking Proposal 2026";

    @Builder.Default
    private String clientName = "Tier-1 Enterprise Bank";

    @Builder.Default
    private String category = "GENERAL";

    // Optional: if true, only retrieve top matches (Milestone 1), if false, generate agent response (Milestone 2)
    @Builder.Default
    private boolean retrievalOnly = false;
}
