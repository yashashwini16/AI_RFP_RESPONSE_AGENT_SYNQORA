package com.azentio.rfp.controller;

import com.azentio.rfp.dto.*;
import com.azentio.rfp.model.RfpChatMessage;
import com.azentio.rfp.model.RfpDocument;
import com.azentio.rfp.model.RfpKnowledgeBase;
import com.azentio.rfp.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rfp")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RfpController {

    private final RfpAgentService agentService;
    private final RfpRetrievalService retrievalService;
    private final RfpKnowledgeService knowledgeService;
    private final RfpExportService exportService;
    private final RfpChatService chatService;
    private final RfpComplianceMatrixService complianceMatrixService;
    private final RfpClarificationService clarificationService;
    private final RfpParserService parserService;

    /**
     * System Health & Agent Capabilities Check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("agent", "Agent 2: Islamic Banking RFP Copilot");
        status.put("suite", "Azentio iMAL Core Banking & Islamic Lending");
        status.put("sources", List.of("Product Documentation", "Past Scopes of Work (SOWs)", "Past RFP Responses"));
        status.put("guardrailsActive", List.of("Commercial & Legal Triage", "Customer Redaction", "Source Quality Check", "Human SE in Control"));
        return ResponseEntity.ok(status);
    }

    /**
     * Triage & Answer an RFP Question (Workflow 3)
     */
    @PostMapping("/query")
    public ResponseEntity<RfpQueryResponseDTO> handleQuery(@Valid @RequestBody RfpQueryRequest request) {
        log.info("Received RFP Query: '{}'", request.getQuestion());
        return ResponseEntity.ok(agentService.processRfpQuery(request));
    }

    /**
     * List all RFP Questions & Requirements (Workflow 2)
     */
    @GetMapping("/queries")
    public ResponseEntity<List<RfpQueryResponseDTO>> listAllQueries(
            @RequestParam(value = "documentId", required = false) Long documentId) {
        return ResponseEntity.ok(agentService.getQueriesForDocumentOrLatest(documentId));
    }

    /**
     * Capability 4: SE Chat Collaboration & Memory (Workflow 4)
     * Converses with the agent to request rewrites, explanations, or updates.
     */
    @PostMapping("/chat")
    public ResponseEntity<RfpChatResponseDTO> chatWithAgent(@Valid @RequestBody RfpChatRequest request) {
        log.info("Chat message from SE (session: {}): '{}'", request.getSessionId(), request.getMessage());
        return ResponseEntity.ok(chatService.handleChat(request));
    }

    @GetMapping("/chat/history")
    public ResponseEntity<List<RfpChatMessage>> getChatHistory(@RequestParam(value = "sessionId", defaultValue = "default-session") String sessionId) {
        return ResponseEntity.ok(chatService.getSessionHistory(sessionId));
    }

    /**
     * Capability 5: Consolidated Customer Clarification Queries (Workflow 5)
     */
    @GetMapping("/clarifications")
    public ResponseEntity<List<ClarificationItemDTO>> getClarificationList(
            @RequestParam(value = "documentId", required = false) Long documentId) {
        return ResponseEntity.ok(clarificationService.getAllClarifications(documentId));
    }

    /**
     * Clear / Reset all uploaded RFPs and triaged data
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetAllData() {
        log.warn("REST endpoint /reset triggered - wiping all tender data");
        agentService.resetAllRfpData();
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "All RFP records and clarifications cleared. Ready for fresh upload."));
    }

    /**
     * Move a Requirement into the Customer Clarification List
     */
    @PostMapping("/clarifications/add")
    public ResponseEntity<ClarificationItemDTO> flagClarification(
            @RequestParam("queryId") Long queryId,
            @RequestParam(value = "reason", required = false) String reason) {
        log.info("Flagging query ID {} for customer clarification", queryId);
        return ResponseEntity.ok(clarificationService.flagQueryForClarification(queryId, reason));
    }

    /**
     * Delete/Resolve a Clarification Item
     */
    @DeleteMapping("/clarifications/{id}")
    public ResponseEntity<Map<String, String>> deleteClarification(@PathVariable("id") Long id) {
        log.info("Deleting clarification ID: {}", id);
        clarificationService.deleteClarification(id);
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Clarification item resolved"));
    }

    /**
     * Update Requirement Classification directly (SE workbench action)
     */
    @PostMapping("/queries/{id}/classify")
    public ResponseEntity<Map<String, String>> updateClassification(
            @PathVariable("id") Long queryId,
            @RequestParam("classification") String classification) {
        log.info("Updating query ID {} classification to: {}", queryId, classification);
        clarificationService.updateClassification(queryId, classification);
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "classification", classification));
    }

    /**
     * Nice to Have / Brownie Point: Compliance Matrix
     */
    @GetMapping("/compliance-matrix")
    public ResponseEntity<List<ComplianceMatrixRowDTO>> getComplianceMatrix() {
        return ResponseEntity.ok(complianceMatrixService.generateComplianceMatrix());
    }

    /**
     * Workflow 1 & 2 Demo Booster:
     * One-click loader that ingests and parses a complete 5-section Islamic Banking RFP!
     */
    @PostMapping("/load-sample-rfp")
    public ResponseEntity<Map<String, Object>> loadSampleRfp() {
        RfpDocument doc = parserService.loadSampleIslamicBankingRfp();
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "SUCCESS");
        resp.put("documentId", doc.getId());
        resp.put("rfpTitle", doc.getRfpTitle());
        resp.put("clientName", doc.getClientName());
        resp.put("submissionDeadline", doc.getSubmissionDeadline());
        resp.put("message", "Loaded and triaged Qatar Islamic Bank RFP with 5 sections and formatting rules.");
        return ResponseEntity.ok(resp);
    }

    /**
     * Level 1 Capability 1: Upload RFP Document (PDF, Excel, Word, CSV)
     */
    @PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadRfpFile(
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "rfpTitle", required = false) String rfpTitle,
            @RequestParam(value = "clientName", required = false) String clientName) {
        log.info("Received RFP upload request for file: {}", (file != null ? file.getOriginalFilename() : "sample"));
        Map<String, Object> result = parserService.parseUploadedRfp(file, rfpTitle, clientName);
        return ResponseEntity.ok(result);
    }

    /**
     * SE Approval & Continuous Learning (Workflow 4 & Guardrails)
     */
    @PostMapping("/responses/{id}/approve")
    public ResponseEntity<RfpQueryResponseDTO> approveResponse(
            @PathVariable("id") Long responseId,
            @RequestBody ApproveRequest request) {
        log.info("Approving response ID: {}", responseId);
        return ResponseEntity.ok(agentService.approveAndLearn(responseId, request));
    }

    /**
     * Re-index Knowledge Vault
     */
    @PostMapping("/knowledge/reindex")
    public ResponseEntity<Map<String, Object>> reindexKnowledge() {
        knowledgeService.vectorizeUnembeddedSeedData();
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "SUCCESS");
        resp.put("message", "All pending knowledge base items vectorized and indexed in PgVector");
        return ResponseEntity.ok(resp);
    }

    /**
     * Level 1 Capability 6: Export Consolidated Response to Word / Markdown Document
     */
    @GetMapping("/export/{rfpTitle}")
    public ResponseEntity<RfpExportDTO> exportProposal(@PathVariable("rfpTitle") String rfpTitle) {
        return ResponseEntity.ok(exportService.exportRfp(rfpTitle));
    }

    /**
     * Level 1 Capability 6: Export Consolidated Response to Excel Spreadsheet (.csv)
     */
    @GetMapping(value = "/export/excel", produces = "text/csv")
    public ResponseEntity<String> exportExcelSpreadsheet() {
        String csv = exportService.exportExcelCsv("all");
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"Azentio_RFP_Consolidated_Response.csv\"")
                .body(csv);
    }
}
