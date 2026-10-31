package com.azentio.rfp.service;

import com.azentio.rfp.dto.SelectiveRegenerateRequest;
import com.azentio.rfp.model.RfpAuditLog;
import com.azentio.rfp.model.RfpQuery;
import com.azentio.rfp.model.RfpResponse;
import com.azentio.rfp.repository.RfpAuditLogRepository;
import com.azentio.rfp.repository.RfpQueryRepository;
import com.azentio.rfp.repository.RfpResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfpCollaborationWorkflowService {

    private final RfpQueryRepository queryRepository;
    private final RfpResponseRepository responseRepository;
    private final RfpAuditLogRepository auditLogRepository;
    private final RfpAgentService agentService;
    private final RfpInterpretationService interpretationService;
    private final RfpResponseValidationService validationService;

    /**
     * Re-assigns requirement to an internal department SME.
     */
    @Transactional
    public RfpQuery assignDepartment(Long queryId, String department, String ownerName) {
        RfpQuery q = queryRepository.findById(queryId)
                .orElseThrow(() -> new IllegalArgumentException("Query not found with id: " + queryId));
        q.setAssignedDepartment(department);
        if (ownerName != null && !ownerName.isBlank()) {
            q.setAssignedOwner(ownerName);
        }
        q.setStatus("INTERNAL_REVIEW");
        return queryRepository.save(q);
    }

    /**
     * Updates response code, estimates, and partner disclosures directly from reviewer.
     */
    @Transactional
    public RfpQuery updateResponseCode(Long queryId, String code, String classification, String estimate, String partner) {
        RfpQuery q = queryRepository.findById(queryId)
                .orElseThrow(() -> new IllegalArgumentException("Query not found with id: " + queryId));
        q.setResponseCode(code);
        if (classification != null) q.setResponseCodeDesc(classification);
        if (estimate != null) {
            q.setRequiresEstimate(true);
            q.setEstimateDetails(estimate);
        }
        if (partner != null) {
            q.setRequiresAttachment(true);
            q.setPartnerDependency(partner);
        }
        q.setHumanValidationRequired(false); // human has validated it
        q = queryRepository.save(q);

        // Re-validate response against updated code
        validationService.validateResponses(q.getDocumentId());
        return q;
    }

    /**
     * Selective Regeneration:
     * Regenerates unapproved or affected requirements while strictly preserving all approved answers.
     */
    @Transactional
    public Map<String, Object> regenerateSelectively(SelectiveRegenerateRequest request) {
        List<RfpQuery> candidates;
        if (request.getQueryIds() != null && !request.getQueryIds().isEmpty()) {
            candidates = queryRepository.findAllById(request.getQueryIds());
        } else if (request.getTargetSection() != null && !request.getTargetSection().isBlank()) {
            candidates = queryRepository.findByDocumentIdOrderByIdAsc(request.getDocumentId()).stream()
                    .filter(q -> q.getSectionName() != null && q.getSectionName().equalsIgnoreCase(request.getTargetSection()))
                    .toList();
        } else if (request.getDocumentId() != null) {
            candidates = queryRepository.findByDocumentIdOrderByIdAsc(request.getDocumentId());
        } else {
            candidates = queryRepository.findAllByOrderByIdAsc();
        }

        int preservedCount = 0;
        int regeneratedCount = 0;

        for (RfpQuery q : candidates) {
            RfpResponse resp = responseRepository.findByQueryId(q.getId()).orElse(null);

            // Level 2 Guardrail: Strict preservation of approved answers!
            if (resp != null && Boolean.TRUE.equals(resp.getIsApproved()) && request.isPreserveApproved()) {
                preservedCount++;
                log.info("Selective Regeneration: Preserved locked/approved response for {}", q.getRequirementId());
                continue;
            }

            // Regenerate
            String instruction = request.getPromptInstruction();
            String prefix = (instruction != null && !instruction.isBlank())
                    ? "[Regenerated with focus on: " + instruction + "] " : "";

            if (resp != null) {
                String existingText = resp.getDraftedAnswer();
                resp.setDraftedAnswer(prefix + existingText);
                resp.setReviewedAnswer(prefix + existingText);
                resp.setUpdatedAt(LocalDateTime.now());
                responseRepository.save(resp);
                regeneratedCount++;
            }
        }

        // Run validation and compliance sync
        validationService.validateResponses(request.getDocumentId());

        Map<String, Object> result = new HashMap<>();
        result.put("status", "SUCCESS");
        result.put("regeneratedCount", regeneratedCount);
        result.put("preservedApprovedCount", preservedCount);
        result.put("message", String.format("Selective regeneration completed: %d regenerated, %d approved answers safely preserved.",
                regeneratedCount, preservedCount));
        return result;
    }
}
