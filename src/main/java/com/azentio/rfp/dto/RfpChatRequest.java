package com.azentio.rfp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfpChatRequest {
    @Builder.Default
    private String sessionId = "default-session";
    private Long queryId; // Optional: context for a specific question being edited
    @NotBlank(message = "Message cannot be blank")
    private String message;
    @Builder.Default
    private String actionType = "CHAT"; // REWRITE, APPROVE, CLARIFY, CHAT
}
