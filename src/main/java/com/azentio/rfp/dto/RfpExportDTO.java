package com.azentio.rfp.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpExportDTO {
    private String rfpTitle;
    private String clientName;
    private int totalQuestions;
    private int approvedCount;
    private int draftedCount;
    private int gapCount;
    private double averageConfidence;
    private String exportedAt;
    private List<RfpQueryResponseDTO> items;
    private String markdownReport;
}
