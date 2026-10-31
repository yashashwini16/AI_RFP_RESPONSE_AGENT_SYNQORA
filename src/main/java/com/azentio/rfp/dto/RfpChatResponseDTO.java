package com.azentio.rfp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpChatResponseDTO {
    private String sessionId;
    private Long queryId;
    private String agentReply;
    private String updatedDraft; // If rewrite or edit was performed
    private String actionTaken;
}
