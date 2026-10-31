package com.azentio.rfp.controller;

import com.azentio.rfp.dto.*;
import com.azentio.rfp.model.RfpKnowledgeBase;
import com.azentio.rfp.service.RfpAgentService;
import com.azentio.rfp.service.RfpExportService;
import com.azentio.rfp.service.RfpKnowledgeService;
import com.azentio.rfp.service.RfpRetrievalService;
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

    /**
     * System Health & Readiness Check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Azentio AI RFP Response Agent");
        status.put("vectorDb", "PostgreSQL + PgVector");
        status.put("milestone1Ready", true);
        status.put("milestone2Ready", true);
        status.put("milestone3Ready", true);
        return ResponseEntity.ok(status);
    }

    /**
     * Milestone 1 & 2 Core Endpoint:
     * Accepts an enterprise question, retrieves top historical answers with PgVector,
     * drafts a grounded proposal response, calculates confidence score, and flags gaps.
     *
     * Example: POST /api/v1/rfp/query
     * Body: { "question": "What encryption standards do you use?" }
     */
    @PostMapping("/query")
    public ResponseEntity<RfpQueryResponseDTO> handleQuery(@Valid @RequestBody RfpQueryRequest request) {
        log.info("Received RFP Query: '{}'", request.getQuestion());
        RfpQueryResponseDTO response = agentService.processRfpQuery(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Milestone 1 Mentor Verification Endpoint:
     * Direct semantic retrieval of top-K historical answers without drafting.
     */
    @GetMapping("/search")
    public ResponseEntity<List<HistoricalMatchDTO>> searchKnowledge(
            @RequestParam("q") String question,
            @RequestParam(value = "limit", defaultValue = "3") int limit) {
        log.info("Direct semantic search query: '{}'", question);
        List<HistoricalMatchDTO> matches = retrievalService.retrieveTopMatches(question, limit);
        return ResponseEntity.ok(matches);
    }

    /**
     * Retrieve all RFP questions for Frontend UI Left Column
     */
    @GetMapping("/queries")
    public ResponseEntity<List<RfpQueryResponseDTO>> listAllQueries() {
        return ResponseEntity.ok(agentService.getAllQueriesWithResponses());
    }

    /**
     * Milestone 3 Differentiator: Human Approval & Continuous Learning Loop.
     * Updates review status and saves approved answer into Knowledge Base.
     */
    @PostMapping("/responses/{id}/approve")
    public ResponseEntity<RfpQueryResponseDTO> approveResponse(
            @PathVariable("id") Long responseId,
            @RequestBody ApproveRequest request) {
        log.info("Approving response ID: {} (saveToKB={})", responseId, request.isSaveToKnowledgeBase());
        RfpQueryResponseDTO approved = agentService.approveAndLearn(responseId, request);
        return ResponseEntity.ok(approved);
    }

    /**
     * Re-index all un-vectorized knowledge base entries
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
     * Knowledge Vault Ingestion Endpoint
     */
    @PostMapping("/knowledge/ingest")
    public ResponseEntity<RfpKnowledgeBase> ingestKnowledge(@Valid @RequestBody IngestRequest request) {
        return ResponseEntity.ok(knowledgeService.ingest(request));
    }

    /**
     * View all verified Knowledge Base records
     */
    @GetMapping("/knowledge")
    public ResponseEntity<List<RfpKnowledgeBase>> getKnowledgeBase() {
        return ResponseEntity.ok(knowledgeService.getAllKnowledge());
    }

    /**
     * Milestone 3 Differentiator: One-Click Export
     * Exports entire answered questionnaire as structured JSON and Markdown proposal.
     */
    @GetMapping("/export/{rfpTitle}")
    public ResponseEntity<RfpExportDTO> exportProposal(
            @PathVariable("rfpTitle") String rfpTitle) {
        log.info("Exporting RFP proposal: '{}'", rfpTitle);
        RfpExportDTO export = exportService.exportRfp(rfpTitle);
        return ResponseEntity.ok(export);
    }
}
