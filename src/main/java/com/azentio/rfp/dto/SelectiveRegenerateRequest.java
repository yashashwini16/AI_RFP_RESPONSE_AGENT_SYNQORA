package com.azentio.rfp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectiveRegenerateRequest {
    private Long documentId;
    private List<Long> queryIds; // Optional: specific query IDs to regenerate
    private String targetSection; // Optional: regenerate only this section
    private boolean preserveApproved; // Defaults to true: never overwrite approved answers
    private String promptInstruction; // e.g. "Align with Oracle Cloud Infrastructure deployment topology"
}
