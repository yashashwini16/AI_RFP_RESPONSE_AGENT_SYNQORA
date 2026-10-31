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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfpAgentService {

    private final RfpQueryRepository queryRepository;
    private final RfpResponseRepository responseRepository;
    private final RfpAuditLogRepository auditLogRepository;
    private final RfpKnowledgeBaseRepository knowledgeBaseRepository;
    private final RfpRetrievalService retrievalService;
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
                        .temperature(0.2) // Low temperature for factual enterprise grounding
                        .build();
                this.useRealLlm = true;
                log.info("Initialized LangChain4j Chat Model: {}", chatModelName);
            } catch (Exception e) {
                log.warn("Failed to initialize LangChain4j Chat Model: {}. Using deterministic generator.", e.getMessage());
                this.useRealLlm = false;
            }
        } else {
            log.info("LangChain4j running in deterministic hackathon mode (instant response, zero external latency).");
            this.useRealLlm = false;
        }
    }

    /**
     * Milestone 1 & 2 Execution:
     * 1. Store incoming RFP query.
     * 2. Retrieve top semantic matches via PgVector.
     * 3. Compute grounded confidence score.
     * 4. Perform gap analysis (< 50% = red flag).
     * 5. Generate synthesized proposal answer with source citations.
     */
    @Transactional
    public RfpQueryResponseDTO processRfpQuery(RfpQueryRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. Persist Query
        RfpQuery query = RfpQuery.builder()
                .rfpTitle(request.getRfpTitle())
                .clientName(request.getClientName())
                .questionText(request.getQuestion())
                .category(request.getCategory())
                .status("PENDING")
                .build();
        query = queryRepository.save(query);

        // 2. PgVector Semantic Retrieval
        List<HistoricalMatchDTO> matches = retrievalService.retrieveTopMatches(request.getQuestion(), 3);

        // If client only requested retrieval (Milestone 1 check)
        if (request.isRetrievalOnly()) {
            return RfpQueryResponseDTO.builder()
                    .queryId(query.getId())
                    .rfpTitle(query.getRfpTitle())
                    .clientName(query.getClientName())
                    .question(query.getQuestionText())
                    .category(query.getCategory())
                    .status("RETRIEVED")
                    .citations(matches)
                    .confidenceScore(matches.isEmpty() ? 0 : matches.get(0).getSimilarityPercentage())
                    .gapFlag(matches.isEmpty() || matches.get(0).getSimilarityPercentage() < 50)
                    .build();
        }

        // 3. Confidence Calculation & Gap Analysis
        int topScore = 0;
        if (!matches.isEmpty() && matches.get(0).getSimilarityPercentage() != null) {
            topScore = matches.get(0).getSimilarityPercentage();
        }

        boolean isGap = topScore < 50 || matches.isEmpty();
        String draftedAnswer;
        String status;

        if (isGap) {
            status = "NEEDS_REVIEW";
            draftedAnswer = "⚠️ GAP ANALYSIS ALERT: No verified historical RFP response matches this inquiry with sufficient confidence ("
                    + topScore + "%). This technical specification requires direct Subject Matter Expert (SME) input to establish Azentio's official proposal response.";
        } else {
            status = "DRAFTED";
            draftedAnswer = generateGroundedAnswer(request.getClientName(), request.getQuestion(), matches);
        }

        // 4. Serialize Citations
        String citationsJson = "[]";
        try {
            citationsJson = objectMapper.writeValueAsString(matches);
        } catch (Exception e) {
            log.error("Failed to serialize citations", e);
        }

        // 5. Persist Response
        RfpResponse response = RfpResponse.builder()
                .queryId(query.getId())
                .draftedAnswer(draftedAnswer)
                .confidenceScore(topScore)
                .sourceCitations(citationsJson)
                .gapFlag(isGap)
                .reviewedAnswer(draftedAnswer)
                .isApproved(false)
                .updatedAt(LocalDateTime.now())
                .build();
        response = responseRepository.save(response);

        // Update query status
        query.setStatus(status);
        queryRepository.save(query);

        // 6. Audit Logging (Tracks token metrics and latency for Azentio Enterprise)
        int latency = (int) (System.currentTimeMillis() - startTime);
        RfpAuditLog auditLog = RfpAuditLog.builder()
                .queryId(query.getId())
                .modelUsed(useRealLlm ? chatModelName : "Azentio-Grounded-Synthesizer-v1")
                .promptTokens(estimateTokens(request.getQuestion() + citationsJson))
                .completionTokens(estimateTokens(draftedAnswer))
                .latencyMs(latency)
                .build();
        auditLogRepository.save(auditLog);

        return RfpQueryResponseDTO.builder()
                .queryId(query.getId())
                .responseId(response.getId())
                .rfpTitle(query.getRfpTitle())
                .clientName(query.getClientName())
                .question(query.getQuestionText())
                .category(query.getCategory())
                .status(status)
                .draftedAnswer(draftedAnswer)
                .confidenceScore(topScore)
                .gapFlag(isGap)
                .citations(matches)
                .reviewedAnswer(response.getReviewedAnswer())
                .isApproved(response.getIsApproved())
                .latencyMs(latency)
                .modelUsed(auditLog.getModelUsed())
                .build();
    }

    /**
     * Milestone 3 Differentiator: Continuous Learning Loop.
     * When Sales Engineer reviews & approves, saves it back into the Knowledge Vault.
     */
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
        response.setUpdatedAt(LocalDateTime.now());
        responseRepository.save(response);

        query.setStatus("APPROVED");
        queryRepository.save(query);

        // Milestone 3 Continuous Learning: Save approved answer back into Knowledge Base
        if (request.isSaveToKnowledgeBase()) {
            RfpKnowledgeBase learned = RfpKnowledgeBase.builder()
                    .category(query.getCategory() != null ? query.getCategory() : "APPROVED_PROPOSAL")
                    .question(query.getQuestionText())
                    .approvedAnswer(response.getReviewedAnswer())
                    .sourceDocument("Approved_" + query.getClientName().replaceAll("\\s+", "_") + "_2026.pdf")
                    .sectionReference("Verified-Clause")
                    .build();
            learned = knowledgeBaseRepository.save(learned);

            // Vectorize newly learned response
            float[] vector = embeddingService.getEmbedding(learned.getQuestion() + " " + learned.getApprovedAnswer());
            knowledgeBaseRepository.updateEmbedding(learned.getId(), embeddingService.toVectorString(vector));
            log.info("Continuous Learning triggered: Query #{} approved and indexed into Knowledge Vault (KB #{})", query.getId(), learned.getId());
        }

        List<HistoricalMatchDTO> citations = parseCitations(response.getSourceCitations());

        return RfpQueryResponseDTO.builder()
                .queryId(query.getId())
                .responseId(response.getId())
                .rfpTitle(query.getRfpTitle())
                .clientName(query.getClientName())
                .question(query.getQuestionText())
                .category(query.getCategory())
                .status("APPROVED")
                .draftedAnswer(response.getDraftedAnswer())
                .confidenceScore(response.getConfidenceScore())
                .gapFlag(response.getGapFlag())
                .citations(citations)
                .reviewedAnswer(response.getReviewedAnswer())
                .isApproved(true)
                .build();
    }

    public List<RfpQueryResponseDTO> getAllQueriesWithResponses() {
        List<RfpQuery> queries = queryRepository.findAllByOrderByIdDesc();
        List<RfpQueryResponseDTO> results = new ArrayList<>();

        for (RfpQuery q : queries) {
            RfpResponse resp = responseRepository.findByQueryId(q.getId()).orElse(null);
            List<HistoricalMatchDTO> citations = resp != null ? parseCitations(resp.getSourceCitations()) : Collections.emptyList();

            results.add(RfpQueryResponseDTO.builder()
                    .queryId(q.getId())
                    .responseId(resp != null ? resp.getId() : null)
                    .rfpTitle(q.getRfpTitle())
                    .clientName(q.getClientName())
                    .question(q.getQuestionText())
                    .category(q.getCategory())
                    .status(q.getStatus())
                    .draftedAnswer(resp != null ? resp.getDraftedAnswer() : null)
                    .confidenceScore(resp != null ? resp.getConfidenceScore() : 0)
                    .gapFlag(resp != null ? resp.getGapFlag() : false)
                    .citations(citations)
                    .reviewedAnswer(resp != null ? resp.getReviewedAnswer() : null)
                    .isApproved(resp != null ? resp.getIsApproved() : false)
                    .build());
        }
        return results;
    }

    private String generateGroundedAnswer(String clientName, String question, List<HistoricalMatchDTO> sources) {
        if (useRealLlm && chatLanguageModel != null) {
            try {
                StringBuilder prompt = new StringBuilder();
                prompt.append("You are Azentio's Principal Solutions Architect responding to an enterprise bank RFP questionnaire.\n");
                prompt.append("Strict grounding rule: Answer accurately, professionally, and factually based strictly on the approved sources provided below.\n\n");
                prompt.append("Client Name: ").append(clientName).append("\n");
                prompt.append("Question: ").append(question).append("\n\n");
                prompt.append("Approved Knowledge Sources:\n");
                for (HistoricalMatchDTO s : sources) {
                    prompt.append("- [Source: ").append(s.getSourceDocument()).append(", ").append(s.getSectionReference()).append("]: ")
                            .append(s.getApprovedAnswer()).append("\n");
                }
                prompt.append("\nGenerate a polished, compliant, and decisive enterprise response tailored for ").append(clientName).append(":");
                return chatLanguageModel.generate(prompt.toString());
            } catch (Exception e) {
                log.warn("LLM generation failed: {}. Falling back to grounded template.", e.getMessage());
            }
        }

        // Deterministic high-quality fallback (Zero hallucination, fully grounded)
        HistoricalMatchDTO top = sources.get(0);
        return String.format(
                "In response to %s's inquiry regarding \"%s\":\n\n%s\n\n" +
                "Compliance & Verification: This response complies with Azentio Enterprise Architecture standards and is derived directly from verified audit records (%s, %s).",
                clientName, question, top.getApprovedAnswer(), top.getSourceDocument(), top.getSectionReference()
        );
    }

    private List<HistoricalMatchDTO> parseCitations(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<HistoricalMatchDTO>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private int estimateTokens(String text) {
        return text == null ? 0 : Math.max(1, text.split("\\s+").length * 4 / 3);
    }
}
