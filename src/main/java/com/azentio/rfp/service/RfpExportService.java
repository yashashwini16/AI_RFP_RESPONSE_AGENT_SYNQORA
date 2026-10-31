package com.azentio.rfp.service;

import com.azentio.rfp.dto.HistoricalMatchDTO;
import com.azentio.rfp.dto.RfpExportDTO;
import com.azentio.rfp.dto.RfpQueryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RfpExportService {

    private final RfpAgentService agentService;

    public RfpExportDTO exportRfp(String rfpTitle) {
        List<RfpQueryResponseDTO> allItems = agentService.getAllQueriesWithResponses();

        // Filter by title if specified
        List<RfpQueryResponseDTO> filtered = (rfpTitle != null && !rfpTitle.isBlank() && !rfpTitle.equalsIgnoreCase("all"))
                ? allItems.stream().filter(i -> rfpTitle.equalsIgnoreCase(i.getRfpTitle())).toList()
                : allItems;

        int total = filtered.size();
        int approved = (int) filtered.stream().filter(i -> Boolean.TRUE.equals(i.getIsApproved())).count();
        int gaps = (int) filtered.stream().filter(i -> Boolean.TRUE.equals(i.getGapFlag()) && !Boolean.TRUE.equals(i.getIsApproved())).count();
        int drafted = (int) filtered.stream().filter(i -> "DRAFTED".equalsIgnoreCase(i.getStatus())).count();

        double avgConfidence = filtered.stream()
                .mapToInt(i -> i.getConfidenceScore() != null ? i.getConfidenceScore() : 0)
                .average()
                .orElse(0.0);

        String client = filtered.isEmpty() ? "Enterprise Client" : filtered.get(0).getClientName();
        String title = (rfpTitle != null && !rfpTitle.isBlank()) ? rfpTitle : "Azentio Enterprise Banking Proposal";
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Generate Markdown Proposal Document
        StringBuilder md = new StringBuilder();
        md.append("# AZENTIO ENTERPRISE RFP RESPONSE DOCUMENT\n\n");
        md.append("**RFP Title:** ").append(title).append("\n");
        md.append("**Target Institution:** ").append(client).append("\n");
        md.append("**Export Timestamp:** ").append(dateStr).append("\n");
        md.append("**Audit Summary:** Total: ").append(total)
          .append(" | Approved: ").append(approved)
          .append(" | Drafted: ").append(drafted)
          .append(" | Gaps Flagged: ").append(gaps)
          .append(" | Avg Confidence: ").append(String.format("%.1f%%", avgConfidence)).append("\n\n");
        md.append("---\n\n");

        md.append("## Executive Compliance & Technical Responses\n\n");

        int index = 1;
        for (RfpQueryResponseDTO item : filtered) {
            md.append("### Question ").append(index++).append(": ").append(item.getQuestion()).append("\n");
            md.append("- **Category:** ").append(item.getCategory()).append("\n");
            md.append("- **Status:** `").append(item.getStatus()).append("`\n");
            md.append("- **Confidence Score:** ").append(item.getConfidenceScore()).append("%\n");

            if (Boolean.TRUE.equals(item.getGapFlag()) && !Boolean.TRUE.equals(item.getIsApproved())) {
                md.append("\n> ⚠️ **GAP ANALYSIS ALERT:** Requires manual Subject Matter Expert (SME) sign-off.\n\n");
            } else {
                md.append("\n**Official Response:**\n\n");
                String finalAns = item.getReviewedAnswer() != null ? item.getReviewedAnswer() : item.getDraftedAnswer();
                md.append(finalAns != null ? finalAns : "Pending draft generation.").append("\n\n");
            }

            if (item.getCitations() != null && !item.getCitations().isEmpty()) {
                md.append("**Verified Citations:**\n");
                for (HistoricalMatchDTO c : item.getCitations()) {
                    md.append("- ").append(c.getSourceDocument())
                      .append(" (").append(c.getSectionReference()).append(") - Similarity: ")
                      .append(c.getSimilarityPercentage()).append("%\n");
                }
                md.append("\n");
            }
            md.append("---\n\n");
        }

        return RfpExportDTO.builder()
                .rfpTitle(title)
                .clientName(client)
                .totalQuestions(total)
                .approvedCount(approved)
                .draftedCount(drafted)
                .gapCount(gaps)
                .averageConfidence(Math.round(avgConfidence * 10.0) / 10.0)
                .exportedAt(dateStr)
                .items(filtered)
                .markdownReport(md.toString())
                .build();
    }
}
