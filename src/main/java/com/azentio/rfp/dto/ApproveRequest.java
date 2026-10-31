package com.azentio.rfp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveRequest {
    private String reviewedAnswer; // Edited answer from the sales engineer
    @Builder.Default
    private boolean saveToKnowledgeBase = true; // Milestone 3: Continuous Learning flag
}
