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

        List<RfpQueryResponseDTO> filtered = (rfpTitle != null && !rfpTitle.isBlank() && !rfpTitle.equalsIgnoreCase("all"))
                ? allItems.stream().filter(i -> rfpTitle.equalsIgnoreCase(i.getRfpTitle())).toList()
                : allItems;

        int total = filtered.size();
        int approved = (int) filtered.stream().filter(i -> Boolean.TRUE.equals(i.getIsApproved())).count();
        int gaps = (int) filtered.stream().filter(i -> "Information Required".equalsIgnoreCase(i.getResponseClassification()) && !Boolean.TRUE.equals(i.getIsApproved())).count();
        int drafted = total - approved - gaps;

        double avgConfidence = filtered.stream()
                .mapToInt(i -> i.getConfidenceScore() != null ? i.getConfidenceScore() : 0)
                .average()
                .orElse(0.0);

        String client = filtered.isEmpty() ? "Enterprise Client" : filtered.get(0).getClientName();
        String title = (rfpTitle != null && !rfpTitle.isBlank()) ? rfpTitle : "Azentio Enterprise Proposal";
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Generate Consolidated Proposal Document
        StringBuilder md = new StringBuilder();
        md.append("# AZENTIO ENTERPRISE RFP RESPONSE DOCUMENT\n\n");
        md.append("**RFP Title:** ").append(title).append("\n");
        md.append("**Target Institution:** ").append(client).append("\n");
        md.append("**Export Timestamp:** ").append(dateStr).append("\n");
        md.append("**Audit Summary:** Total: ").append(total)
          .append(" | Approved: ").append(approved)
          .append(" | Drafted: ").append(drafted)
          .append(" | Info Required: ").append(gaps)
          .append(" | Avg Confidence: ").append(String.format("%.1f%%", avgConfidence)).append("\n\n");
        md.append("---\n\n");

        md.append("## Requirement-Level Traceability & Responses\n\n");

        for (RfpQueryResponseDTO item : filtered) {
            md.append("### [").append(item.getRequirementId() != null ? item.getRequirementId() : "REQ").append("] ")
              .append(item.getQuestion()).append("\n");
            md.append("- **Section:** ").append(item.getSectionName()).append("\n");
            md.append("- **Category:** `").append(item.getCategory()).append("` | **Type:** `").append(item.getRequirementType()).append("`\n");
            md.append("- **Level 1 Classification:** `").append(item.getResponseClassification()).append("`\n");
            md.append("- **Confidence Score:** ").append(item.getConfidenceScore()).append("%\n");
            md.append("- **Approval Status:** `").append(Boolean.TRUE.equals(item.getIsApproved()) ? "APPROVED" : "DRAFT").append("`\n");

            if ("Information Required".equalsIgnoreCase(item.getResponseClassification()) && !Boolean.TRUE.equals(item.getIsApproved())) {
                md.append("\n> ⚠️ **INFORMATION REQUIRED:** ").append(item.getUnansweredExplanation() != null ? item.getUnansweredExplanation() : "Customer clarification needed.").append("\n\n");
            } else {
                md.append("\n**Grounded Proposal Response:**\n\n");
                String finalAns = item.getReviewedAnswer() != null ? item.getReviewedAnswer() : item.getDraftedAnswer();
                md.append(finalAns != null ? finalAns : "Pending draft generation.").append("\n\n");
            }

            if (item.getCitations() != null && !item.getCitations().isEmpty()) {
                md.append("**Supporting Evidential Sources:**\n");
                for (HistoricalMatchDTO c : item.getCitations()) {
                    md.append("- [").append(c.getSourceType()).append("] ").append(c.getSourceDocument())
                      .append(" (").append(c.getSectionReference()).append(") - Match: ")
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

    /**
     * Level 1 Capability 6: Export to Excel Spreadsheet (CSV)
     */
    public String exportExcelCsv(String rfpTitle) {
        List<RfpQueryResponseDTO> allItems = agentService.getAllQueriesWithResponses();

        StringBuilder csv = new StringBuilder();
        csv.append("Requirement ID,Section Name,Category,Requirement Text,Requirement Type,Question Type,Classification,Confidence Score,Status,Assigned Owner,Drafted / Reviewed Answer,Primary Source\n");

        for (RfpQueryResponseDTO item : allItems) {
            String ans = item.getReviewedAnswer() != null ? item.getReviewedAnswer() : item.getDraftedAnswer();
            if (ans != null) {
                ans = ans.replace("\"", "\"\"").replace("\n", " ");
            } else {
                ans = "";
            }

            String primarySource = (item.getCitations() != null && !item.getCitations().isEmpty())
                    ? item.getCitations().get(0).getSourceDocument() + " (" + item.getCitations().get(0).getSectionReference() + ")"
                    : "None";

            csv.append("\"").append(item.getRequirementId() != null ? item.getRequirementId() : "").append("\",")
               .append("\"").append(escapeCsv(item.getSectionName())).append("\",")
               .append("\"").append(escapeCsv(item.getCategory())).append("\",")
               .append("\"").append(escapeCsv(item.getQuestion())).append("\",")
               .append("\"").append(escapeCsv(item.getRequirementType())).append("\",")
               .append("\"").append(escapeCsv(item.getQuestionType())).append("\",")
               .append("\"").append(escapeCsv(item.getResponseClassification())).append("\",")
               .append(item.getConfidenceScore() != null ? item.getConfidenceScore() : 0).append(",")
               .append("\"").append(escapeCsv(item.getStatus())).append("\",")
               .append("\"").append(escapeCsv(item.getAssignedOwner())).append("\",")
               .append("\"").append(ans).append("\",")
               .append("\"").append(escapeCsv(primarySource)).append("\"\n");
        }
        return csv.toString();
    }

    private String escapeCsv(String val) {
        return val == null ? "" : val.replace("\"", "\"\"");
    }
}
