package com.azentio.rfp.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpQueryResponseDTO {
    private Long queryId;
    private Long responseId;
    private String rfpTitle;
    private String clientName;
    private String question;
    private String category;
    private String status;

    // Milestone 1 & 2 outputs
    private String draftedAnswer;
    private Integer confidenceScore; // 0 to 100
    private Boolean gapFlag; // true if confidence < 50%
    private List<HistoricalMatchDTO> citations; // Top retrieved sources

    // Milestone 3 editable & approval state
    private String reviewedAnswer;
    private Boolean isApproved;

    // Latency & Token metrics for enterprise credibility
    private Integer latencyMs;
    private String modelUsed;
}
