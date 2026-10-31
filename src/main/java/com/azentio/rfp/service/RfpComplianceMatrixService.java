package com.azentio.rfp.service;

import com.azentio.rfp.dto.ComplianceMatrixRowDTO;
import com.azentio.rfp.dto.HistoricalMatchDTO;
import com.azentio.rfp.model.RfpQuery;
import com.azentio.rfp.model.RfpResponse;
import com.azentio.rfp.repository.RfpQueryRepository;
import com.azentio.rfp.repository.RfpResponseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RfpComplianceMatrixService {

    private final RfpQueryRepository queryRepository;
    private final RfpResponseRepository responseRepository;
    private final ObjectMapper objectMapper;

    /**
     * Nice to Have / Winning Differentiator:
     * Generates a comprehensive Compliance Matrix for the RFP response.
     */
    public List<ComplianceMatrixRowDTO> generateComplianceMatrix() {
        List<RfpQuery> queries = queryRepository.findAllByOrderByIdAsc();
        List<ComplianceMatrixRowDTO> rows = new ArrayList<>();

        int index = 1;
        for (RfpQuery q : queries) {
            RfpResponse resp = responseRepository.findByQueryId(q.getId()).orElse(null);
            List<HistoricalMatchDTO> citations = parseCitations(resp != null ? resp.getSourceCitations() : null);

            String status = q.getComplianceStatus();
            if (status == null || status.isBlank()) {
                if (resp != null && Boolean.TRUE.equals(resp.getGapFlag())) {
                    status = "CLARIFICATION";
                } else if (q.getGuardrailFlag() != null && !q.getGuardrailFlag().equals("TECHNICAL")) {
                    status = "PARTIALLY_COMPLIANT";
                } else if (resp != null && resp.getConfidenceScore() >= 75) {
                    status = "COMPLIANT";
                } else {
                    status = "PARTIALLY_COMPLIANT";
                }
            }

            String summary = resp != null ? (resp.getReviewedAnswer() != null ? resp.getReviewedAnswer() : resp.getDraftedAnswer()) : "Draft in progress";
            if (summary.length() > 180) {
                summary = summary.substring(0, 177) + "...";
            }

            rows.add(ComplianceMatrixRowDTO.builder()
                    .queryId(q.getId())
                    .clauseNumber(String.format("SEC-%d.%d", (q.getId() % 5) + 1, index++))
                    .sectionName(q.getSectionName() != null ? q.getSectionName() : "Islamic Banking Core")
                    .requirementText(q.getQuestionText())
                    .requirementType(q.getRequirementType() != null ? q.getRequirementType() : "MANDATORY")
                    .complianceStatus(status)
                    .responseSummary(summary)
                    .assignedOwner(q.getAssignedOwner() != null ? q.getAssignedOwner() : "SE_TECHNICAL")
                    .citations(citations)
                    .build());
        }
        return rows;
    }

    private List<HistoricalMatchDTO> parseCitations(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<HistoricalMatchDTO>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
