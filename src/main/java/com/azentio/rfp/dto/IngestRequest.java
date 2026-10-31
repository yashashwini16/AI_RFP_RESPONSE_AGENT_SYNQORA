package com.azentio.rfp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestRequest {
    @NotBlank
    private String category;
    @NotBlank
    private String question;
    @NotBlank
    private String approvedAnswer;
    private String sourceDocument;
    private String sectionReference;
}
