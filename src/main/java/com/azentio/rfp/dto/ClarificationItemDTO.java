package com.azentio.rfp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClarificationItemDTO {
    private Long id;
    private Long queryId;
    private String requirementId;
    private String sectionName;
    private String rfpTitle;
    private String status;
    private String questionText;
    private String clarificationNeeded;
    private String reasonJustification;
}
