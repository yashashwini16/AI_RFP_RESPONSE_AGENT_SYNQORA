package com.azentio.rfp.controller;

import com.azentio.rfp.dto.*;
import com.azentio.rfp.model.RfpQuery;
import com.azentio.rfp.model.RfpResponse;
import com.azentio.rfp.repository.RfpQueryRepository;
import com.azentio.rfp.repository.RfpResponseRepository;
import com.azentio.rfp.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/rfp")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class Level2RfpController {

    private final RfpQueryRepository queryRepository;
    private final RfpResponseRepository responseRepository;
    private final RfpInterpretationService interpretationService;
    private final RfpComplianceMatrixService complianceMatrixService;
    private final RfpGapAnalysisService gapAnalysisService;
    private final RfpResponseValidationService validationService;
    private final RfpClarificationManagerService clarificationManagerService;
    private final RfpProposalAssemblyService proposalAssemblyService;
    private final RfpCollaborationWorkflowService collaborationWorkflowService;

    /**
     * Level 2 Execution Pipeline:
     * Triggers interpretation, code mapping (S/C/D/T/N), gap analysis, and forensic validation across all requirements.
     */
    @PostMapping("/analyze-level2")
    public ResponseEntity<Map<String, Object>> runLevel2Analysis(
            @RequestParam(value = "documentId", required = false) Long documentId) {
        log.info("Executing Level 2 Intelligent Gap Analysis & Compliance pipeline (docId: {})", documentId);

        List<RfpQuery> queries = (documentId != null)
                ? queryRepository.findByDocumentIdOrderByIdAsc(documentId)
                : queryRepository.findAllByOrderByIdAsc();

        for (RfpQuery q : queries) {
            RfpResponse resp = responseRepository.findByQueryId(q.getId()).orElse(null);
            int score = (resp != null && resp.getConfidenceScore() != null) ? resp.getConfidenceScore() : 50;
            String narrative = (resp != null) 
                    ? (resp.getReviewedAnswer() != null ? resp.getReviewedAnswer() : resp.getDraftedAnswer())
                    : "";
            interpretationService.interpretRequirement(q, score, narrative);
            queryRepository.save(q);
        }

        // Run gap analysis & forensic validation
        GapAnalysisReportDTO gapReport = gapAnalysisService.generateGapReport(documentId);
        ValidationReportDTO validationReport = validationService.validateResponses(documentId);
        SubmissionChecklistDTO checklist = proposalAssemblyService.generateSubmissionChecklist(documentId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "SUCCESS");
        resp.put("analyzedCount", queries.size());
        resp.put("readinessScore", checklist.getReadinessScore());
        resp.put("overallStatus", checklist.getOverallStatus());
        resp.put("confirmedProductGaps", gapReport.getConfirmedProductGaps());
        resp.put("knowledgeGaps", gapReport.getKnowledgeGaps());
        resp.put("blockingValidationIssues", validationReport.getBlockingCount());
        resp.put("message", "Level 2 compliance matrix, gap analysis, and validation audit executed successfully.");
        return ResponseEntity.ok(resp);
    }

    /**
     * Capability 2: Intelligent Customer-Adaptive Compliance Matrix (S / C / D / T / N)
     */
    @GetMapping("/compliance-matrix")
    public ResponseEntity<List<Level2ComplianceRowDTO>> getLevel2ComplianceMatrix(
            @RequestParam(value = "documentId", required = false) Long documentId) {
        return ResponseEntity.ok(complianceMatrixService.generateLevel2ComplianceMatrix(documentId));
    }

    /**
     * Capability 3: AI-Driven Gap Analysis (Confirmed Product Gap vs Knowledge Gap)
     */
    @GetMapping("/gap-analysis")
    public ResponseEntity<GapAnalysisReportDTO> getGapAnalysis(
            @RequestParam(value = "documentId", required = false) Long documentId) {
        return ResponseEntity.ok(gapAnalysisService.generateGapReport(documentId));
    }

    /**
     * Capability 4: Prioritized, Deduplicated Customer Clarification Pack
     */
    @GetMapping("/clarification-pack")
    public ResponseEntity<ClarificationPackDTO> getClarificationPack(
            @RequestParam(value = "documentId", required = false) Long documentId) {
        return ResponseEntity.ok(clarificationManagerService.generateClarificationPack(documentId));
    }

    /**
     * Capability 5: Forensic Response Validation (Hallucination & Code Mismatch Gate)
     */
    @GetMapping("/validation-report")
    public ResponseEntity<ValidationReportDTO> getValidationReport(
            @RequestParam(value = "documentId", required = false) Long documentId) {
        return ResponseEntity.ok(validationService.validateResponses(documentId));
    }

    /**
     * Capability 6: Submission Readiness Checklist & Assembly Gate
     */
    @GetMapping("/submission-checklist")
    public ResponseEntity<SubmissionChecklistDTO> getSubmissionChecklist(
            @RequestParam(value = "documentId", required = false) Long documentId) {
        return ResponseEntity.ok(proposalAssemblyService.generateSubmissionChecklist(documentId));
    }

    /**
     * Capability 6: Export Populated Customer Excel Response Template
     */
    @GetMapping(value = "/export/customer-template", produces = "text/csv; charset=UTF-8")
    public ResponseEntity<String> exportCustomerTemplate(
            @RequestParam(value = "documentId", required = false) Long documentId) {
        String csv = proposalAssemblyService.generateCustomerTemplateCsv(documentId);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"Azentio_QIB_Customer_Response_Template.csv\"")
                .body(csv);
    }

    /**
     * Capability 7: Collaborative Department Assignment (PM, SA, Engineering, Legal, Shariah, SE)
     */
    @PostMapping("/requirements/{id}/assign")
    public ResponseEntity<RfpQuery> assignDepartment(
            @PathVariable("id") Long queryId,
            @RequestParam("department") String department,
            @RequestParam(value = "ownerName", required = false) String ownerName) {
        log.info("Routing query ID {} to department: {} (owner: {})", queryId, department, ownerName);
        return ResponseEntity.ok(collaborationWorkflowService.assignDepartment(queryId, department, ownerName));
    }

    /**
     * Capability 2 & 7: Update Compliance Code, Estimate, or Partner Disclosures
     */
    @PostMapping("/requirements/{id}/update-code")
    public ResponseEntity<RfpQuery> updateResponseCode(
            @PathVariable("id") Long queryId,
            @RequestParam("code") String code,
            @RequestParam(value = "classification", required = false) String classification,
            @RequestParam(value = "estimate", required = false) String estimate,
            @RequestParam(value = "partner", required = false) String partner) {
        log.info("Updating query ID {} to Code {}: estimate='{}', partner='{}'", queryId, code, estimate, partner);
        return ResponseEntity.ok(collaborationWorkflowService.updateResponseCode(queryId, code, classification, estimate, partner));
    }

    /**
     * Capability 7: Selective Section Regeneration (Preserves Approved Answers)
     */
    @PostMapping("/regenerate-selective")
    public ResponseEntity<Map<String, Object>> regenerateSelectively(
            @RequestBody SelectiveRegenerateRequest request) {
        log.info("Selective regeneration request: preserveApproved={}", request.isPreserveApproved());
        return ResponseEntity.ok(collaborationWorkflowService.regenerateSelectively(request));
    }
}
