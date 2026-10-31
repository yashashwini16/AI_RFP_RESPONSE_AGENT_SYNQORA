package com.azentio.rfp.service;

import com.azentio.rfp.dto.HistoricalMatchDTO;
import com.azentio.rfp.repository.KnowledgeMatchProjection;
import com.azentio.rfp.repository.RfpKnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfpRetrievalService {

    private final RfpKnowledgeBaseRepository knowledgeBaseRepository;
    private final EmbeddingService embeddingService;

    /**
     * Milestone 1 Core Deliverable:
     * Semantically retrieves the top-K past RFP answers matching the enterprise question.
     */
    public List<HistoricalMatchDTO> retrieveTopMatches(String question, int limit) {
        log.info("Performing PgVector semantic retrieval for question: '{}' (limit: {})", question, limit);

        try {
            float[] queryEmbedding = embeddingService.getEmbedding(question);
            String vectorString = embeddingService.toVectorString(queryEmbedding);

            List<KnowledgeMatchProjection> projections = knowledgeBaseRepository.searchByVector(vectorString, limit);

            if (projections != null && !projections.isEmpty()) {
                return mapToDTOList(projections);
            }
        } catch (Exception e) {
            log.warn("Vector search encountered error, falling back to keyword search: {}", e.getMessage());
        }

        // Graceful keyword fallback
        List<KnowledgeMatchProjection> fallback = knowledgeBaseRepository.searchByKeyword(question, limit);
        return mapToDTOList(fallback);
    }

    private List<HistoricalMatchDTO> mapToDTOList(List<KnowledgeMatchProjection> list) {
        List<HistoricalMatchDTO> results = new ArrayList<>();
        if (list == null) return results;

        for (KnowledgeMatchProjection p : list) {
            double rawScore = p.getSimilarityScore() != null ? p.getSimilarityScore() : 0.0;
            // Calibrate raw cosine score for enterprise presentation:
            // High matches (>= 0.28 in hash space or >= 0.70 in neural embeddings) map to 80-97%
            int percentage;
            if (rawScore >= 0.25) {
                percentage = Math.min(97, (int) Math.round(75 + (rawScore - 0.25) * 160));
            } else if (rawScore >= 0.15) {
                percentage = Math.min(74, (int) Math.round(50 + (rawScore - 0.15) * 240));
            } else {
                percentage = Math.max(10, (int) Math.round(rawScore * 250));
            }

            results.add(HistoricalMatchDTO.builder()
                    .id(p.getId())
                    .category(p.getCategory())
                    .question(p.getQuestion())
                    .approvedAnswer(p.getApprovedAnswer())
                    .sourceDocument(p.getSourceDocument())
                    .sectionReference(p.getSectionReference())
                    .similarityScore(rawScore)
                    .similarityPercentage(percentage)
                    .build());
        }
        return results;
    }
}
