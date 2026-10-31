package com.azentio.rfp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricalMatchDTO {
    private Long id;
    private String category;
    private String question;
    private String approvedAnswer;
    private String sourceDocument;
    private String sectionReference;
    private Double similarityScore; // 0.00 to 1.00
    private Integer similarityPercentage; // 0 to 100
}
