package com.azentio.rfp.service;

import com.azentio.rfp.dto.ClarificationItemDTO;
import com.azentio.rfp.model.RfpClarification;
import com.azentio.rfp.model.RfpQuery;
import com.azentio.rfp.model.RfpResponse;
import com.azentio.rfp.repository.RfpClarificationRepository;
import com.azentio.rfp.repository.RfpQueryRepository;
import com.azentio.rfp.repository.RfpResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfpClarificationService {

    private final RfpClarificationRepository clarificationRepository;
    private final RfpQueryRepository queryRepository;
    private final RfpResponseRepository responseRepository;

    /**
     * Capability 5: Clarification Query Generation
     * When knowledge is missing, instead of assuming, the agent compiles a formal clarification query.
     */
    @Transactional
    public void registerClarificationQuery(Long queryId, String questionText, String sectionName, Long documentId) {
        String needed = "Detailed technical and operational specification regarding " + extractTopic(questionText) + " architecture and integration standards.";
        String reason = "Azentio iMAL supports multiple market-standard implementations (e.g. Bursa Suq Al-Sila vs. DMCC, direct API vs. SWIFT). Clarification ensures accurate scope and Shariah alignment.";

        RfpClarification c = RfpClarification.builder()
                .documentId(documentId)
                .queryId(queryId)
                .questionText(questionText)
                .clarificationNeeded(needed)
                .reasonJustification(reason)
                .sectionName(sectionName != null ? sectionName : "General Specifications")
                .status("PENDING_CUSTOMER_FEEDBACK")
                .build();
        clarificationRepository.save(c);
        log.info("Registered customer clarification query for Query #{} (docId: {})", queryId, documentId);
    }

    @Transactional
    public ClarificationItemDTO flagQueryForClarification(Long queryId, String customReason) {
        RfpQuery q = queryRepository.findById(queryId)
                .orElseThrow(() -> new IllegalArgumentException("Query not found: " + queryId));

        q.setComplianceStatus("Information Required");
        q.setStatus("NEEDS_REVIEW");
        queryRepository.save(q);

        // Also update response classification and explanation
        responseRepository.findByQueryId(queryId).ifPresent(resp -> {
            resp.setResponseClassification("Information Required");
            resp.setConfidenceScore(45);
            resp.setGapFlag(true);
            resp.setUnansweredExplanation(customReason != null && !customReason.isBlank() 
                    ? customReason 
                    : "Sales Engineer flagged: Customer specification required to confirm exact Shariah and architectural parameters.");
            resp.setUpdatedAt(LocalDateTime.now());
            responseRepository.save(resp);
        });

        String topic = extractTopic(q.getQuestionText());
        String needed = "Customer parameter clarification required for " + topic + ": " + q.getQuestionText();
        String justification = (customReason != null && !customReason.isBlank()) 
                ? customReason 
                : "Azentio Sales Engineering flagged this requirement: Customer parameter specification needed to confirm exact Shariah and architectural compliance.";

        // Delete any existing entry to avoid duplicates
        clarificationRepository.deleteByQueryId(q.getId());

        RfpClarification c = RfpClarification.builder()
                .documentId(q.getDocumentId())
                .queryId(q.getId())
                .requirementId(q.getRequirementId())
                .sectionName(q.getSectionName())
                .rfpTitle(q.getRfpTitle())
                .questionText(q.getQuestionText())
                .clarificationNeeded(needed)
                .reasonJustification(justification)
                .status("PENDING_CUSTOMER_FEEDBACK")
                .build();
        c = clarificationRepository.save(c);

        return ClarificationItemDTO.builder()
                .id(c.getId())
                .queryId(q.getId())
                .requirementId(q.getRequirementId())
                .sectionName(q.getSectionName())
                .rfpTitle(q.getRfpTitle())
                .status(c.getStatus())
                .questionText(c.getQuestionText())
                .clarificationNeeded(c.getClarificationNeeded())
                .reasonJustification(c.getReasonJustification())
                .build();
    }

    @Transactional
    public void updateClassification(Long queryId, String newClassification) {
        RfpQuery q = queryRepository.findById(queryId)
                .orElseThrow(() -> new IllegalArgumentException("Query not found: " + queryId));

        q.setComplianceStatus(newClassification);
        queryRepository.save(q);

        responseRepository.findByQueryId(queryId).ifPresent(resp -> {
            resp.setResponseClassification(newClassification);
            if ("Fully Addressed".equalsIgnoreCase(newClassification)) {
                resp.setConfidenceScore(Math.max(resp.getConfidenceScore(), 95));
                resp.setGapFlag(false);
            } else if ("Partially Addressed".equalsIgnoreCase(newClassification)) {
                resp.setConfidenceScore(Math.max(resp.getConfidenceScore(), 75));
            } else if ("Information Required".equalsIgnoreCase(newClassification)) {
                resp.setConfidenceScore(45);
                resp.setGapFlag(true);
            }
            resp.setUpdatedAt(LocalDateTime.now());
            responseRepository.save(resp);
        });

        if ("Information Required".equalsIgnoreCase(newClassification)) {
            flagQueryForClarification(queryId, "Sales Engineer updated classification to Information Required.");
        } else {
            clarificationRepository.deleteByQueryId(queryId);
        }
    }

    @Transactional
    public void deleteClarification(Long clarificationId) {
        clarificationRepository.findById(clarificationId).ifPresent(c -> {
            if (c.getQueryId() != null) {
                queryRepository.findById(c.getQueryId()).ifPresent(q -> {
                    q.setComplianceStatus("Partially Addressed");
                    queryRepository.save(q);
                });
                responseRepository.findByQueryId(c.getQueryId()).ifPresent(resp -> {
                    resp.setResponseClassification("Partially Addressed");
                    resp.setConfidenceScore(75);
                    resp.setGapFlag(false);
                    responseRepository.save(resp);
                });
            }
            clarificationRepository.delete(c);
        });
    }

    @Transactional
    public void deleteAll() {
        clarificationRepository.deleteAll();
    }

    public List<ClarificationItemDTO> getAllClarifications(Long documentId) {
        List<RfpClarification> list;
        if (documentId != null) {
            list = clarificationRepository.findByDocumentIdOrderByIdAsc(documentId);
        } else {
            list = clarificationRepository.findAllByOrderByIdAsc();
        }
        List<ClarificationItemDTO> dtos = new ArrayList<>();

        for (RfpClarification c : list) {
            String section = c.getSectionName() != null ? c.getSectionName() : "Islamic Banking Specifications";
            String reqId = c.getRequirementId();
            String rfpTitle = c.getRfpTitle() != null ? c.getRfpTitle() : "Amanah Islamic RFP";

            if (c.getQueryId() != null) {
                RfpQuery q = queryRepository.findById(c.getQueryId()).orElse(null);
                if (q != null) {
                    if (q.getSectionName() != null) section = q.getSectionName();
                    if (q.getRequirementId() != null) reqId = q.getRequirementId();
                    if (q.getRfpTitle() != null) rfpTitle = q.getRfpTitle();
                }
            }

            dtos.add(ClarificationItemDTO.builder()
                    .id(c.getId())
                    .queryId(c.getQueryId())
                    .requirementId(reqId != null ? reqId : "CLAR-" + c.getId())
                    .sectionName(section)
                    .rfpTitle(rfpTitle)
                    .status(c.getStatus() != null ? c.getStatus() : "LOGGED")
                    .questionText(c.getQuestionText())
                    .clarificationNeeded(c.getClarificationNeeded())
                    .reasonJustification(c.getReasonJustification())
                    .build());
        }
        return dtos;
    }

    private String extractTopic(String text) {
        if (text == null) return "feature";
        String lower = text.toLowerCase();
        if (lower.contains("murabaha")) return "Murabaha commodity trade";
        if (lower.contains("ijara")) return "Ijara lease asset management";
        if (lower.contains("sukuk")) return "Sukuk issuance & registry";
        if (lower.contains("shariah")) return "Shariah board audit workflow";
        if (lower.contains("mudaraba")) return "Mudaraba profit distribution pooling";
        if (lower.contains("rpo") || lower.contains("rto") || lower.contains("disaster")) return "Disaster recovery RPO/RTO";
        if (lower.contains("tps") || lower.contains("volume")) return "Transaction throughput and peak TPS";
        return "system requirement";
    }
}
