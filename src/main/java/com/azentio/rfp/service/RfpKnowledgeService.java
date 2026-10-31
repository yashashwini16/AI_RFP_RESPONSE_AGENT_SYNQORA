package com.azentio.rfp.service;

import com.azentio.rfp.dto.IngestRequest;
import com.azentio.rfp.model.RfpKnowledgeBase;
import com.azentio.rfp.repository.KnowledgeMatchProjection;
import com.azentio.rfp.repository.RfpKnowledgeBaseRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfpKnowledgeService {

    private final RfpKnowledgeBaseRepository repository;
    private final EmbeddingService embeddingService;

    /**
     * Ingest a new verified proposal Q&A into the Knowledge Vault
     */
    @Transactional
    public RfpKnowledgeBase ingest(IngestRequest request) {
        log.info("Ingesting new proposal Q&A into Knowledge Vault: category={}", request.getCategory());

        RfpKnowledgeBase kb = RfpKnowledgeBase.builder()
                .category(request.getCategory().toUpperCase())
                .question(request.getQuestion())
                .approvedAnswer(request.getApprovedAnswer())
                .sourceDocument(request.getSourceDocument() != null ? request.getSourceDocument() : "Manual_Ingest_2026.pdf")
                .sectionReference(request.getSectionReference() != null ? request.getSectionReference() : "Sec 1.0")
                .build();

        RfpKnowledgeBase saved = repository.save(kb);

        // Vectorize and persist embedding into Postgres
        float[] vector = embeddingService.getEmbedding(saved.getQuestion() + " " + saved.getApprovedAnswer());
        String vectorStr = embeddingService.toVectorString(vector);
        repository.updateEmbedding(saved.getId(), vectorStr);

        return saved;
    }

    public List<RfpKnowledgeBase> getAllKnowledge() {
        return repository.findAll();
    }

    /**
     * Startup vectorizer: ensures all seed data in PostgreSQL has genuine PgVector embeddings.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void vectorizeUnembeddedSeedData() {
        try {
            List<KnowledgeMatchProjection> unembedded = repository.findUnvectorized();
            if (!unembedded.isEmpty()) {
                log.info("Found {} unembedded seed items in Knowledge Vault. Generating PgVector embeddings...", unembedded.size());
                for (KnowledgeMatchProjection item : unembedded) {
                    float[] vector = embeddingService.getEmbedding(item.getQuestion() + " " + item.getApprovedAnswer());
                    repository.updateEmbedding(item.getId(), embeddingService.toVectorString(vector));
                }
                log.info("Successfully vectorized all seed knowledge items in PostgreSQL!");
            }
        } catch (Exception e) {
            log.warn("Notice: Vectorizing seed data deferred or schema still creating: {}", e.getMessage());
        }
    }
}
