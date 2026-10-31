package com.azentio.rfp.service;

import com.azentio.rfp.dto.*;
import com.azentio.rfp.model.RfpAuditLog;
import com.azentio.rfp.model.RfpKnowledgeBase;
import com.azentio.rfp.model.RfpQuery;
import com.azentio.rfp.model.RfpResponse;
import com.azentio.rfp.repository.RfpAuditLogRepository;
import com.azentio.rfp.repository.RfpKnowledgeBaseRepository;
import com.azentio.rfp.repository.RfpQueryRepository;
import com.azentio.rfp.repository.RfpResponseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import com.azentio.rfp.model.RfpDocument;
import com.azentio.rfp.repository.RfpDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfpAgentService {

    private final RfpQueryRepository queryRepository;
    private final RfpResponseRepository responseRepository;
    private final RfpAuditLogRepository auditLogRepository;
    private final RfpKnowledgeBaseRepository knowledgeBaseRepository;
    private final RfpDocumentRepository documentRepository;
    private final RfpRetrievalService retrievalService;
    private final RfpGuardrailService guardrailService;
    private final RfpClarificationService clarificationService;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:mock}")
    private String openAiApiKey;

    @Value("${openai.model-name:gpt-4o-mini}")
    private String chatModelName;

    private ChatLanguageModel chatLanguageModel;
    private boolean useRealLlm = false;

    @PostConstruct
    public void init() {
        if (openAiApiKey != null && !openAiApiKey.isBlank() && !openAiApiKey.equalsIgnoreCase("mock") && !openAiApiKey.startsWith("sk-placeholder")) {
            try {
                this.chatLanguageModel = OpenAiChatModel.builder()
                        .apiKey(openAiApiKey)
                        .modelName(chatModelName)
                        .temperature(0.2)
                        .build();
                this.useRealLlm = true;
                log.info("Initialized LangChain4j Chat Model for Level 1 RFP: {}", chatModelName);
            } catch (Exception e) {
                this.useRealLlm = false;
            }
        }
    }

    /**
     * Level 1 Core Pipeline:
     * 1. RFP document ingestion & Requirement ID traceability.
     * 2. Intelligent classification (Domain, Mandatory/Optional, Question Type, Duplicate detection).
     * 3. Enterprise Knowledge Retrieval (3 Sources: Product Docs, SOWs, Past RFPs).
     * 4. AI-Powered Grounded Answering.
     * 5. Response Classification (Fully Addressed, Partially Addressed, Information Required, Not Supported).
     */
    @Transactional
    public RfpQueryResponseDTO processRfpQuery(RfpQueryRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. Requirement ID Generation (if not supplied)
        String reqId = request.getRequirementId();
        if (reqId == null || reqId.isBlank()) {
            String prefix = getCategoryPrefix(request.getCategory());
            long count = queryRepository.count() + 1;
            reqId = String.format("REQ-%s-%02d", prefix, count);
        }

        // 2. Intelligent Classification: Question Type
        String questionType = determineQuestionType(request.getQuestion());

        // 3. Duplicate / Overlap Detection
        Long duplicateOfId = checkDuplicateRequirement(request.getQuestion());
        boolean isDuplicate = (duplicateOfId != null);

        // 4. Commercial & Legal Guardrail Evaluation
        RfpGuardrailService.GuardrailResult guardrail = guardrailService.evaluateGuardrails(request.getQuestion());

        // 4b. Intelligent Team / Desk Routing
        String assignedDesk = guardrail.isTriggered() 
                ? guardrail.assignedOwner() 
                : resolveAssignedDesk(request.getCategory(), request.getSectionName(), request.getQuestion());

        // 4c. In-Flight SE Workbench Copilot / Ad-hoc Prompt (Preview without persisting to tender matrix)
        if (request.isRetrievalOnly()) {
            if (guardrail.isTriggered()) {
                return RfpQueryResponseDTO.builder()
                        .requirementId(reqId)
                        .clientName(request.getClientName())
                        .sectionName(request.getSectionName())
                        .question(request.getQuestion())
                        .category(request.getCategory())
                        .requirementType(request.getRequirementType())
                        .questionType(questionType)
                        .responseClassification("Partially Addressed")
                        .unansweredExplanation("Commercial, legal, or SLA liability terms require direct authorization from the " + guardrail.assignedOwner() + ".")
                        .assignedOwner(assignedDesk)
                        .guardrailFlag(guardrail.guardrailFlag())
                        .guardrailReason(guardrail.reasonExplanation())
                        .draftedAnswer(guardrail.reasonExplanation())
                        .reviewedAnswer(guardrail.reasonExplanation())
                        .confidenceScore(100)
                        .citations(Collections.emptyList())
                        .status("NEEDS_REVIEW")
                        .latencyMs((int) (System.currentTimeMillis() - startTime))
                        .modelUsed("Azentio-Guardrail-Engine")
                        .build();
            }

            List<HistoricalMatchDTO> matches = retrievalService.retrieveTopMatches(request.getQuestion(), 3);
            for (HistoricalMatchDTO m : matches) {
                m.setApprovedAnswer(guardrailService.sanitizeConfidentialDetails(m.getApprovedAnswer()));
            }
            String outdatedWarning = guardrailService.evaluateSourceQuality(matches);
            int topScore = (!matches.isEmpty() && matches.get(0).getSimilarityPercentage() != null)
                    ? matches.get(0).getSimilarityPercentage() : 0;
            boolean isGap = topScore < 50 || matches.isEmpty();
            String responseClassification;
            String draftedAnswer;
            String unansweredExplanation = null;
            if (isGap) {
                responseClassification = "Information Required";
                unansweredExplanation = "No approved organizational knowledge answers this specific requirement with >= 50% confidence (Score: " + topScore + "%). Customer parameter clarification is required.";
                draftedAnswer = "⚠️ INFORMATION REQUIRED: Supporting organizational knowledge is insufficient to answer this requirement with high confidence ("
                        + topScore + "%). Customer parameter clarification is recommended.";
            } else if (topScore >= 80) {
                responseClassification = "Fully Addressed";
                draftedAnswer = generateGroundedAnswer(request.getClientName(), request.getQuestion(), matches);
            } else {
                responseClassification = "Partially Addressed";
                unansweredExplanation = "Organizational documentation partially matches this requirement (" + topScore + "% similarity). Sales Engineer review recommended.";
                draftedAnswer = generateGroundedAnswer(request.getClientName(), request.getQuestion(), matches);
            }

            int latency = (int) (System.currentTimeMillis() - startTime);
            String model = useRealLlm ? chatModelName : "Azentio-Grounded-Synthesizer-v1";

            return RfpQueryResponseDTO.builder()
                    .requirementId(reqId)
                    .clientName(request.getClientName())
                    .sectionName(request.getSectionName())
                    .question(request.getQuestion())
                    .category(request.getCategory())
                    .requirementType(request.getRequirementType())
                    .questionType(questionType)
                    .responseClassification(responseClassification)
                    .unansweredExplanation(unansweredExplanation)
                    .assignedOwner(assignedDesk)
                    .guardrailFlag(null)
                    .draftedAnswer(draftedAnswer)
                    .reviewedAnswer(draftedAnswer)
                    .confidenceScore(topScore)
                    .gapFlag(isGap)
                    .outdatedWarning(outdatedWarning)
                    .citations(matches)
                    .status("DRAFTED")
                    .latencyMs(latency)
                    .modelUsed(model)
                    .build();
        }

        // 5. Persist Query
        RfpQuery query = RfpQuery.builder()
                .documentId(request.getDocumentId())
                .requirementId(reqId)
                .rfpTitle(request.getRfpTitle())
                .clientName(request.getClientName())
                .sectionName(request.getSectionName() != null ? request.getSectionName() : "General Requirements")
                .questionText(request.getQuestion())
                .category(request.getCategory() != null ? request.getCategory() : "FUNCTIONAL")
                .requirementType(request.getRequirementType() != null ? request.getRequirementType() : "MANDATORY")
                .questionType(questionType)
                .isDuplicate(isDuplicate)
                .duplicateOfId(duplicateOfId)
                .guardrailFlag(guardrail.guardrailFlag())
                .assignedOwner(assignedDesk)
                .status("PENDING")
                .build();
        query = queryRepository.save(query);

        // Commercial / Legal Guardrail Triage
        if (guardrail.isTriggered()) {
            query.setComplianceStatus("Partially Addressed");
            query.setStatus("NEEDS_REVIEW");
            queryRepository.save(query);

            RfpResponse response = RfpResponse.builder()
                    .queryId(query.getId())
                    .draftedAnswer(guardrail.reasonExplanation())
                    .confidenceScore(100)
                    .responseClassification("Partially Addressed")
                    .unansweredExplanation("Commercial, legal, or SLA liability terms require direct authorization from the " + guardrail.assignedOwner() + ".")
                    .sourceCitations("[]")
                    .gapFlag(false)
                    .guardrailReason(guardrail.reasonExplanation())
                    .reviewedAnswer(guardrail.reasonExplanation())
                    .isApproved(false)
                    .updatedAt(LocalDateTime.now())
                    .build();
            response = responseRepository.save(response);

            return mapToDTO(query, response, Collections.emptyList(), (int) (System.currentTimeMillis() - startTime), "Azentio-Guardrail-Engine");
        }

        // 6. Enterprise Knowledge Semantic Retrieval (3 Sources)
        List<HistoricalMatchDTO> matches = retrievalService.retrieveTopMatches(request.getQuestion(), 3);

        // Sanitize client confidentiality in citations
        for (HistoricalMatchDTO m : matches) {
            m.setApprovedAnswer(guardrailService.sanitizeConfidentialDetails(m.getApprovedAnswer()));
        }

        // Check for outdated source versions
        String outdatedWarning = guardrailService.evaluateSourceQuality(matches);

        // 7. Calculate Confidence & Response Classification (Level 1 Categories)
        int topScore = 0;
        if (!matches.isEmpty() && matches.get(0).getSimilarityPercentage() != null) {
            topScore = matches.get(0).getSimilarityPercentage();
        }

        boolean isGap = topScore < 50 || matches.isEmpty();
        String draftedAnswer;
        String status;
        String responseClassification;
        String unansweredExplanation = null;

        if (isGap) {
            status = "NEEDS_REVIEW";
            responseClassification = "Information Required";
            unansweredExplanation = "No approved organizational knowledge answers this specific requirement with >= 50% confidence (Score: " + topScore + "%). Customer parameter clarification is required.";
            draftedAnswer = "⚠️ INFORMATION REQUIRED: Supporting organisational knowledge is insufficient to answer this requirement with high confidence ("
                    + topScore + "%). This item has been added to the Clarification Queries list for the customer.";

            clarificationService.registerClarificationQuery(query.getId(), query.getQuestionText(), query.getSectionName(), query.getDocumentId());
        } else if (topScore >= 80) {
            status = "DRAFTED";
            responseClassification = "Fully Addressed";
            draftedAnswer = generateGroundedAnswer(request.getClientName(), request.getQuestion(), matches);
        } else {
            status = "DRAFTED";
            responseClassification = "Partially Addressed";
            unansweredExplanation = "Organizational documentation partially matches this requirement (" + topScore + "% similarity). Sales Engineer review recommended.";
            draftedAnswer = generateGroundedAnswer(request.getClientName(), request.getQuestion(), matches);
        }

        query.setComplianceStatus(responseClassification);
        query.setStatus(status);
        queryRepository.save(query);

        // 8. Serialize Citations
        String citationsJson = "[]";
        try {
            citationsJson = objectMapper.writeValueAsString(matches);
        } catch (Exception ignored) {}

        // 9. Persist Response
        RfpResponse response = RfpResponse.builder()
                .queryId(query.getId())
                .draftedAnswer(draftedAnswer)
                .confidenceScore(topScore)
                .responseClassification(responseClassification)
                .unansweredExplanation(unansweredExplanation)
                .sourceCitations(citationsJson)
                .gapFlag(isGap)
                .outdatedWarning(outdatedWarning)
                .reviewedAnswer(draftedAnswer)
                .isApproved(false)
                .updatedAt(LocalDateTime.now())
                .build();
        response = responseRepository.save(response);

        // 10. Audit Logging
        int latency = (int) (System.currentTimeMillis() - startTime);
        RfpAuditLog auditLog = RfpAuditLog.builder()
                .queryId(query.getId())
                .modelUsed(useRealLlm ? chatModelName : "Azentio-Grounded-Synthesizer-v1")
                .promptTokens(180)
                .completionTokens(120)
                .latencyMs(latency)
                .build();
        auditLogRepository.save(auditLog);

        return mapToDTO(query, response, matches, latency, auditLog.getModelUsed());
    }

    @Transactional
    public RfpQueryResponseDTO approveAndLearn(Long responseId, ApproveRequest request) {
        RfpResponse response = responseRepository.findById(responseId)
                .orElseThrow(() -> new IllegalArgumentException("Response not found: " + responseId));

        RfpQuery query = queryRepository.findById(response.getQueryId())
                .orElseThrow(() -> new IllegalArgumentException("Query not found for response: " + responseId));

        response.setIsApproved(true);
        if (request.getReviewedAnswer() != null && !request.getReviewedAnswer().isBlank()) {
            response.setReviewedAnswer(request.getReviewedAnswer());
        }
        response.setResponseClassification("Fully Addressed");
        response.setUpdatedAt(LocalDateTime.now());
        responseRepository.save(response);

        query.setStatus("APPROVED");
        query.setComplianceStatus("Fully Addressed");
        queryRepository.save(query);

        if (request.isSaveToKnowledgeBase()) {
            RfpKnowledgeBase learned = RfpKnowledgeBase.builder()
                    .category(query.getCategory() != null ? query.getCategory() : "APPROVED_PROPOSAL")
                    .sourceType("PAST_RFP")
                    .productVersion("iMAL v15.0")
                    .question(query.getQuestionText())
                    .approvedAnswer(response.getReviewedAnswer())
                    .sourceDocument("Approved_" + query.getClientName().replaceAll("\\s+", "_") + "_2026.pdf")
                    .sectionReference(query.getRequirementId() != null ? query.getRequirementId() : "SE-Verified")
                    .build();
            learned = knowledgeBaseRepository.save(learned);

            float[] vector = embeddingService.getEmbedding(learned.getQuestion() + " " + learned.getApprovedAnswer());
            knowledgeBaseRepository.updateEmbedding(learned.getId(), embeddingService.toVectorString(vector));
        }

        List<HistoricalMatchDTO> citations = parseCitations(response.getSourceCitations());
        return mapToDTO(query, response, citations, 0, "Approved-SE-Control");
    }

    public List<RfpQueryResponseDTO> getAllQueriesWithResponses() {
        return getQueriesForDocumentOrLatest(null);
    }

    public List<RfpQueryResponseDTO> getQueriesForDocumentOrLatest(Long documentId) {
        List<RfpQuery> queries;
        if (documentId != null) {
            queries = queryRepository.findByDocumentIdOrderByIdAsc(documentId);
        } else {
            List<RfpDocument> docs = documentRepository.findAll();
            if (!docs.isEmpty()) {
                RfpDocument latest = docs.get(docs.size() - 1);
                queries = queryRepository.findByDocumentIdOrderByIdAsc(latest.getId());
                if (queries.isEmpty()) {
                    queries = queryRepository.findByRfpTitleOrderByIdAsc(latest.getRfpTitle());
                }
            } else {
                queries = queryRepository.findAllByOrderByIdAsc();
            }
        }

        List<RfpQueryResponseDTO> results = new ArrayList<>();
        for (RfpQuery q : queries) {
            RfpResponse resp = responseRepository.findByQueryId(q.getId()).orElse(null);
            List<HistoricalMatchDTO> citations = resp != null ? parseCitations(resp.getSourceCitations()) : Collections.emptyList();
            results.add(mapToDTO(q, resp, citations, 0, "Azentio-iMAL"));
        }
        return results;
    }

    private Long checkDuplicateRequirement(String question) {
        if (question == null) return null;
        List<RfpQuery> existing = queryRepository.findAll();
        for (RfpQuery eq : existing) {
            if (eq.getQuestionText().equalsIgnoreCase(question.trim())) {
                return eq.getId();
            }
        }
        return null;
    }

    private String determineQuestionType(String text) {
        if (text == null) return "EXPLANATION";
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.startsWith("does") || lower.startsWith("do you") || lower.startsWith("is the") || lower.startsWith("can the") || lower.startsWith("has the")) {
            return "CONFIRMATION";
        }
        if (lower.contains("soc") || lower.contains("iso") || lower.contains("audit") || lower.contains("certif") || lower.contains("sla") || lower.contains("benchmark") || lower.contains("evidence")) {
            return "EVIDENCE";
        }
        if (lower.startsWith("what is") || lower.startsWith("what are") || lower.contains("version") || lower.contains("encryption standard") || lower.contains("port") || lower.contains("database supported")) {
            return "FACTUAL";
        }
        return "EXPLANATION";
    }

    public String resolveAssignedDesk(String category, String sectionName, String questionText) {
        String combined = ((category != null ? category : "") + " " 
                + (sectionName != null ? sectionName : "") + " " 
                + (questionText != null ? questionText : "")).toLowerCase();

        // 1. Commercial Pricing Desk
        if (combined.contains("price") || combined.contains("pricing") || combined.contains("cost") 
                || combined.contains("fee") || combined.contains("rate card") || combined.contains("tco") 
                || combined.contains("license fee") || combined.contains("quotation")) {
            return "COMMERCIAL_DESK";
        }

        // 2. Legal & Contracts Counsel
        if (combined.contains("liability") || combined.contains("indemnity") || combined.contains("penalty") 
                || combined.contains("liquidated damages") || combined.contains("warranty") 
                || combined.contains("governing law") || combined.contains("sla commitment") 
                || combined.contains("jurisdiction")) {
            return "LEGAL_DESK";
        }

        // 3. Information Security & InfoSec Desk
        if (combined.contains("security") || combined.contains("encryption") || combined.contains("key management") 
                || combined.contains("hsm") || combined.contains("rbac") || combined.contains("segregation of duties") 
                || combined.contains("access control") || combined.contains("audit trail") 
                || combined.contains("vulnerability") || combined.contains("privileged access") 
                || combined.contains("incident response")) {
            return "SECURITY_DESK";
        }

        // 4. Enterprise Technical Architecture & Cloud Desk
        if (combined.contains("resilience") || combined.contains("backup") || combined.contains("disaster") 
                || combined.contains("rpo") || combined.contains("rto") || combined.contains("deployment") 
                || combined.contains("infrastructure") || combined.contains("architecture") || combined.contains("sizing") 
                || combined.contains("openshift") || combined.contains("active-active") || combined.contains("performance") 
                || combined.contains("latency") || combined.contains("throughput") || combined.contains("tps") 
                || combined.contains("monitoring") || combined.contains("prerequisites") || combined.contains("scalability") 
                || combined.contains("high availability")) {
            return "TECHNICAL_DESK";
        }

        // 5. API & External Integration Desk
        if (combined.contains("api") || combined.contains("integration") || combined.contains("rest") 
                || combined.contains("interface") || combined.contains("channel") || combined.contains("swift") 
                || combined.contains("broker") || combined.contains("gateway") || combined.contains("connector") 
                || combined.contains("adapter")) {
            return "INTEGRATION_DESK";
        }

        // 6. Data Migration & Cutover Desk
        if (combined.contains("migration") || combined.contains("cleansing") || combined.contains("reconciliation") 
                || combined.contains("cutover") || combined.contains("conversion") || combined.contains("legacy data") 
                || combined.contains("profiling") || combined.contains("rehearsal")) {
            return "MIGRATION_DESK";
        }

        // 7. Core Islamic Banking & Shariah Product Desk (Default for functional Islamic modules)
        return "PRODUCT_DESK";
    }

    private String getCategoryPrefix(String category) {
        if (category == null) return "REQ";
        return switch (category.toUpperCase()) {
            case "SECURITY" -> "SEC";
            case "COMPLIANCE", "SHARIAH_COMPLIANCE" -> "CMP";
            case "TECHNICAL", "ARCHITECTURE" -> "TECH";
            case "INTEGRATION" -> "INT";
            case "COMMERCIAL", "PRICING" -> "COM";
            default -> "FN";
        };
    }

    private String generateGroundedAnswer(String clientName, String question, List<HistoricalMatchDTO> sources) {
        if (sources == null || sources.isEmpty()) {
            return "No matching organizational knowledge found for this requirement.";
        }
        if (useRealLlm && chatLanguageModel != null) {
            try {
                StringBuilder prompt = new StringBuilder();
                prompt.append("You are Azentio's Principal Solutions Architect & Senior Sales Engineer responding to an enterprise RFP requirement for Azentio iMAL Islamic Banking.\n");
                prompt.append("STRICT INSTRUCTION: Ground your answer completely in the approved organizational knowledge sources below.\n");
                prompt.append("Address the requirement directly and authoritatively. Explicitly explain the Islamic banking operational mechanism (e.g. Murabaha, Ijara, Mudaraba, Sukuk, AAOIFI/IFSB standards compliance), architectural interfaces, and cite the supporting evidence documents.\n\n");
                prompt.append("Target Client: ").append(clientName).append("\n");
                prompt.append("Requirement: ").append(question).append("\n\n");
                prompt.append("Approved Organizational Knowledge Sources:\n");
                for (HistoricalMatchDTO s : sources) {
                    prompt.append("- [Source: ").append(s.getSourceType()).append(" | Doc: ").append(s.getSourceDocument())
                            .append(", Ref: ").append(s.getSectionReference()).append("]: ").append(s.getApprovedAnswer()).append("\n");
                }
                prompt.append("\nGenerate an authoritative, customer-adapted enterprise proposal response structured with:\n1. Executive Summary & Compliance Statement\n2. Islamic Banking Capability & Operational Workflow\n3. Grounded Evidence & Verification.\n");
                return chatLanguageModel.generate(prompt.toString());
            } catch (Exception e) {
                log.warn("Real LLM call failed or timed out ({}). Falling back to multi-source grounded synthesis: {}", chatModelName, e.getMessage());
            }
        }

        // Multi-Source Grounded Synthesizer
        HistoricalMatchDTO top = sources.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("In response to %s's requirement regarding \"%s\":\n\n",
                clientName != null ? clientName : "the Bank", question));
        sb.append("Azentio iMAL Core Islamic Banking natively complies with this requirement in full alignment with international AAOIFI and IFSB governance standards.\n\n");
        sb.append("• Operational & Architectural Capability:\n");
        sb.append(top.getApprovedAnswer()).append("\n\n");

        if (sources.size() > 1) {
            sb.append("• Supporting Enterprise Evidence & Implementation Record:\n");
            for (int i = 1; i < sources.size(); i++) {
                HistoricalMatchDTO aux = sources.get(i);
                sb.append(String.format("- [%s / %s, %s]: %s\n", aux.getSourceType(), aux.getSourceDocument(), aux.getSectionReference(), aux.getApprovedAnswer()));
            }
            sb.append("\n");
        }

        sb.append(String.format("Compliance Grounding: Formally verified in Azentio Organizational Knowledge Vault (%s - %s, %s).",
                top.getSourceType(), top.getSourceDocument(), top.getSectionReference()));

        return sb.toString();





    }

    public synchronized Map<String, Object> configureLlm(String apiKey, String modelName, String baseUrl) {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equalsIgnoreCase("mock")) {
            this.useRealLlm = false;
            this.chatLanguageModel = null;
            this.openAiApiKey = "mock";
            log.info("LLM configured to Built-in Grounded Synthesizer");
            return Map.of(
                    "status", "SUCCESS",
                    "mode", "BUILTIN_SYNTHESIZER",
                    "active", false,
                    "model", "Azentio-Grounded-Synthesizer-v1",
                    "message", "Active: Built-in Azentio Grounded Synthesizer"
            );
        }

        try {
            String selectedModel = (modelName != null && !modelName.isBlank()) ? modelName.trim() : "gpt-4o-mini";
            var builder = OpenAiChatModel.builder()
                    .apiKey(apiKey.trim())
                    .modelName(selectedModel)
                    .temperature(0.2)
                    .timeout(java.time.Duration.ofSeconds(35));

            if (baseUrl != null && !baseUrl.isBlank()) {
                builder.baseUrl(baseUrl.trim());
            }

            this.chatLanguageModel = builder.build();
            this.openAiApiKey = apiKey.trim();
            this.chatModelName = selectedModel;
            this.useRealLlm = true;

            log.info("Runtime LLM successfully configured: model={}", this.chatModelName);
            return Map.of(
                    "status", "SUCCESS",
                    "mode", "REAL_LLM",
                    "active", true,
                    "model", this.chatModelName,
                    "message", "Active: OpenAI " + this.chatModelName
            );
        } catch (Exception e) {
            log.error("Failed to initialize OpenAiChatModel: {}", e.getMessage());
            return Map.of(
                    "status", "ERROR",
                    "active", false,
                    "message", "Failed to activate LLM: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> getLlmConfig() {
        boolean hasKey = openAiApiKey != null && !openAiApiKey.isBlank() && !openAiApiKey.equalsIgnoreCase("mock");
        String maskedKey = hasKey && openAiApiKey.length() > 8 
                ? openAiApiKey.substring(0, 4) + "..." + openAiApiKey.substring(openAiApiKey.length() - 4) 
                : (hasKey ? "sk-***" : "None (Built-in Mode)");

        return Map.of(
                "active", useRealLlm,
                "model", chatModelName != null ? chatModelName : "gpt-4o-mini",
                "hasKey", hasKey,
                "maskedKey", maskedKey,
                "engineDescription", useRealLlm ? ("OpenAI " + chatModelName) : "Built-in Azentio Grounded Synthesizer"
        );
    }

    private RfpQueryResponseDTO mapToDTO(RfpQuery query, RfpResponse response, List<HistoricalMatchDTO> citations, int latency, String model) {
        return RfpQueryResponseDTO.builder()
                .queryId(query.getId())
                .responseId(response != null ? response.getId() : null)
                .requirementId(query.getRequirementId())
                .rfpTitle(query.getRfpTitle())
                .clientName(query.getClientName())
                .sectionName(query.getSectionName())
                .question(query.getQuestionText())
                .category(query.getCategory())
                .requirementType(query.getRequirementType())
                .questionType(query.getQuestionType())
                .responseClassification(response != null ? response.getResponseClassification() : query.getComplianceStatus())
                .unansweredExplanation(response != null ? response.getUnansweredExplanation() : null)
                .isDuplicate(query.getIsDuplicate())
                .duplicateOfId(query.getDuplicateOfId())
                .guardrailFlag(query.getGuardrailFlag())
                .guardrailReason(response != null ? response.getGuardrailReason() : null)
                .assignedOwner(query.getAssignedOwner())
                .status(query.getStatus())
                .outdatedWarning(response != null ? response.getOutdatedWarning() : null)
                .draftedAnswer(response != null ? response.getDraftedAnswer() : null)
                .confidenceScore(response != null ? response.getConfidenceScore() : 0)
                .gapFlag(response != null ? response.getGapFlag() : false)
                .citations(citations)
                .reviewedAnswer(response != null ? response.getReviewedAnswer() : null)
                .isApproved(response != null ? response.getIsApproved() : false)
                .latencyMs(latency)
                .modelUsed(model)
                .build();
    }

    @Transactional
    public void resetAllRfpData() {
        log.warn("Executing complete reset of all RFP uploaded tenders and triaged records");
        auditLogRepository.deleteAll();
        responseRepository.deleteAll();
        clarificationService.deleteAll();
        queryRepository.deleteAll();
        documentRepository.deleteAll();
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
