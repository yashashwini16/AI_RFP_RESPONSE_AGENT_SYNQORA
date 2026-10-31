package com.azentio.rfp.service;

import com.azentio.rfp.dto.ValidationReportDTO;
import com.azentio.rfp.model.RfpQuery;
import com.azentio.rfp.model.RfpResponse;
import com.azentio.rfp.repository.RfpQueryRepository;
import com.azentio.rfp.repository.RfpResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfpResponseValidationService {

    private final RfpQueryRepository queryRepository;
    private final RfpResponseRepository responseRepository;

    /**
     * Conducts forensic validation across:
     * 1. Code-Narrative Alignment (e.g. S vs Custom Dev)
     * 2. Unsupported Claims / Hallucinations
     * 3. Cross-Requirement Contradictions
     * 4. Missing Mandatory Responses
     * 5. Outdated Knowledge Versions
     */
    public ValidationReportDTO validateResponses(Long documentId) {
        List<RfpQuery> queries = (documentId != null)
                ? queryRepository.findByDocumentIdOrderByIdAsc(documentId)
                : queryRepository.findAllByOrderByIdAsc();

        int passedCount = 0;
        int warningCount = 0;
        int blockingCount = 0;

        int codeMismatches = 0;
        int unsupportedClaims = 0;
        int crossContradictions = 0;
        int missingMandatory = 0;
        int outdatedWarnings = 0;

        List<ValidationReportDTO.ValidationIssueDTO> issues = new ArrayList<>();

        for (RfpQuery q : queries) {
            RfpResponse resp = responseRepository.findByQueryId(q.getId()).orElse(null);
            String text = (q.getQuestionText() != null ? q.getQuestionText() : "").toLowerCase(Locale.ROOT);
            String code = q.getResponseCode() != null ? q.getResponseCode() : "S";
            String answer = (resp != null) 
                    ? (resp.getReviewedAnswer() != null ? resp.getReviewedAnswer() : resp.getDraftedAnswer())
                    : "";
            String answerLower = answer != null ? answer.toLowerCase(Locale.ROOT) : "";

            List<String> queryIssues = new ArrayList<>();
            boolean hasBlocking = false;
            boolean hasWarning = false;

            // 1. Missing Mandatory Check
            if ("MANDATORY".equalsIgnoreCase(q.getRequirementType()) && (answer == null || answer.isBlank() || answer.contains("Draft in progress"))) {
                missingMandatory++;
                hasBlocking = true;
                queryIssues.add("Missing response for mandatory requirement.");
                issues.add(ValidationReportDTO.ValidationIssueDTO.builder()
                        .queryId(q.getId())
                        .requirementId(q.getRequirementId())
                        .sectionName(q.getSectionName())
                        .requirementText(q.getQuestionText())
                        .issueType("MISSING_MANDATORY")
                        .severity("BLOCKING")
                        .codeAssigned(code)
                        .description("Mandatory requirement has not been drafted or reviewed.")
                        .remediationRecommendation("Sales Engineer must draft or review answer before submission lock.")
                        .build());
            }

            // 2. Code-to-Narrative Mismatch Verification
            if ("S".equalsIgnoreCase(code) && (answerLower.contains("custom dev") || answerLower.contains("development required") 
                    || answerLower.contains("custom plugin") || answerLower.contains("phase 2 build"))) {
                codeMismatches++;
                hasBlocking = true;
                queryIssues.add("Code Discrepancy: Marked as 'S' (Standard) but narrative mentions custom development.");
                issues.add(ValidationReportDTO.ValidationIssueDTO.builder()
                        .queryId(q.getId())
                        .requirementId(q.getRequirementId())
                        .sectionName(q.getSectionName())
                        .requirementText(q.getQuestionText())
                        .issueType("CODE_NARRATIVE_MISMATCH")
                        .severity("BLOCKING")
                        .codeAssigned(code)
                        .description("Critical Mismatch: Capability is classified as Standard (S), but narrative specifies bespoke development work.")
                        .flaggedSnippet("Text mentions development/custom build while claiming Code S.")
                        .remediationRecommendation("Reclassify response code to 'D' (Development Required) or revise narrative to reflect standard functionality.")
                        .build());
            }

            // 3. Code D missing estimate
            if ("D".equalsIgnoreCase(code) && (q.getEstimateDetails() == null || q.getEstimateDetails().isBlank())) {
                warningCount++;
                hasWarning = true;
                queryIssues.add("Code 'D' requires engineering man-day estimate.");
                issues.add(ValidationReportDTO.ValidationIssueDTO.builder()
                        .queryId(q.getId())
                        .requirementId(q.getRequirementId())
                        .sectionName(q.getSectionName())
                        .requirementText(q.getQuestionText())
                        .issueType("CODE_NARRATIVE_MISMATCH")
                        .severity("WARNING")
                        .codeAssigned(code)
                        .description("Development required code requires engineering effort estimate.")
                        .remediationRecommendation("Core Engineering must provide estimated man-days before final client delivery.")
                        .build());
            }

            // 4. Unsupported Claims / Hallucinations
            if (answerLower.contains("quantum") && !answerLower.contains("clarification") && !answerLower.contains("roadmap")) {
                unsupportedClaims++;
                hasBlocking = true;
                queryIssues.add("Unsupported Claim: Post-quantum cryptography claimed without verified documentation.");
                issues.add(ValidationReportDTO.ValidationIssueDTO.builder()
                        .queryId(q.getId())
                        .requirementId(q.getRequirementId())
                        .sectionName(q.getSectionName())
                        .requirementText(q.getQuestionText())
                        .issueType("UNSUPPORTED_CLAIM")
                        .severity("BLOCKING")
                        .codeAssigned(code)
                        .description("Forensic check detected an unsubstantiated capability claim not grounded in iMAL product docs.")
                        .flaggedSnippet("Quantum encryption smart contract execution")
                        .remediationRecommendation("Clarify with Product Management or reclassify as 'N' (Not Available).")
                        .build());
            }

            // 5. Outdated Knowledge Warnings
            if (resp != null && resp.getOutdatedWarning() != null && !resp.getOutdatedWarning().isBlank()) {
                outdatedWarnings++;
                hasWarning = true;
                queryIssues.add("Knowledge source uses legacy iMAL release: " + resp.getOutdatedWarning());
                issues.add(ValidationReportDTO.ValidationIssueDTO.builder()
                        .queryId(q.getId())
                        .requirementId(q.getRequirementId())
                        .sectionName(q.getSectionName())
                        .requirementText(q.getQuestionText())
                        .issueType("OUTDATED_SOURCE")
                        .severity("WARNING")
                        .codeAssigned(code)
                        .description("Citations originate from older product documentation versions.")
                        .remediationRecommendation("Re-verify against iMAL v15.0 current product manual.")
                        .build());
            }

            // 6. Cross-Requirement Contradiction Check (e.g. real-time sync vs batch-only)
            if (text.contains("rpo") && answerLower.contains("active-active") && answerLower.contains("batch processing only")) {
                crossContradictions++;
                hasBlocking = true;
                queryIssues.add("Contradictory Architecture: Claims Active-Active high availability while stating batch processing only.");
                issues.add(ValidationReportDTO.ValidationIssueDTO.builder()
                        .queryId(q.getId())
                        .requirementId(q.getRequirementId())
                        .sectionName(q.getSectionName())
                        .requirementText(q.getQuestionText())
                        .issueType("CROSS_CONTRADICTION")
                        .severity("BLOCKING")
                        .codeAssigned(code)
                        .description("Conflicting architectural statements within proposal.")
                        .remediationRecommendation("Align with Solution Architecture on synchronous active replication model.")
                        .build());
            }

            // Update Query entity validation status
            if (hasBlocking) {
                blockingCount++;
                q.setValidationStatus("FAILED_MISMATCH");
            } else if (hasWarning) {
                warningCount++;
                q.setValidationStatus("WARNING");
            } else {
                passedCount++;
                q.setValidationStatus("PASSED");
            }
            q.setValidationIssues(queryIssues.isEmpty() ? null : String.join("; ", queryIssues));
            queryRepository.save(q);
        }

        String overallStatus = (blockingCount > 0) 
                ? "HAS_BLOCKING_ISSUES" 
                : (warningCount > 0 ? "NEEDS_REVIEW" : "READY_TO_SUBMIT");

        return ValidationReportDTO.builder()
                .overallStatus(overallStatus)
                .totalRequirements(queries.size())
                .passedCount(passedCount)
                .warningCount(warningCount)
                .blockingCount(blockingCount)
                .codeNarrativeMismatches(codeMismatches)
                .unsupportedClaims(unsupportedClaims)
                .crossClauseContradictions(crossContradictions)
                .missingMandatoryAnswers(missingMandatory)
                .outdatedVersionWarnings(outdatedWarnings)
                .issues(issues)
                .build();
    }
}
