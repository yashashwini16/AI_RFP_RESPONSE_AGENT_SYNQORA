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
    private String questionText;
    private String clarificationNeeded;
    private String reasonJustification;
    private String sectionName;
}
