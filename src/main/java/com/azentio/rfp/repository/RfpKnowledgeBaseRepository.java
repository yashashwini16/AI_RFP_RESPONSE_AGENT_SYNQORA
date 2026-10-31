package com.azentio.rfp.repository;

import com.azentio.rfp.model.RfpKnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RfpKnowledgeBaseRepository extends JpaRepository<RfpKnowledgeBase, Long> {

    /**
     * Native PgVector Cosine Similarity Semantic Retrieval
     * Cosine distance: <=> returns 0 for identical, 2 for opposite.
     * Cosine similarity: 1 - (embedding <=> :queryVector)
     */
    @Query(value = """
            SELECT id, category, question, approved_answer AS approvedAnswer, 
                   source_document AS sourceDocument, section_reference AS sectionReference,
                   (1.0 - (embedding <=> cast(:queryVector as vector))) AS similarityScore
            FROM rfp_knowledge_base
            WHERE embedding IS NOT NULL
            ORDER BY (embedding <=> cast(:queryVector as vector)) ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<KnowledgeMatchProjection> searchByVector(
            @Param("queryVector") String queryVector,
            @Param("limit") int limit
    );

    /**
     * Fallback full-text keyword search if embeddings are still initializing
     */
    @Query(value = """
            SELECT id, category, question, approved_answer AS approvedAnswer, 
                   source_document AS sourceDocument, section_reference AS sectionReference,
                   0.75 AS similarityScore
            FROM rfp_knowledge_base
            WHERE LOWER(question) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(approved_answer) LIKE LOWER(CONCAT('%', :keyword, '%'))
            LIMIT :limit
            """, nativeQuery = true)
    List<KnowledgeMatchProjection> searchByKeyword(
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );

    @Modifying
    @Transactional
    @Query(value = "UPDATE rfp_knowledge_base SET embedding = cast(:embedding as vector) WHERE id = :id", nativeQuery = true)
    void updateEmbedding(@Param("id") Long id, @Param("embedding") String embedding);

    @Query(value = """
            SELECT id, category, question, approved_answer AS approvedAnswer, 
                   source_document AS sourceDocument, section_reference AS sectionReference,
                   0.0 AS similarityScore
            FROM rfp_knowledge_base 
            WHERE embedding IS NULL
            """, nativeQuery = true)
    List<KnowledgeMatchProjection> findUnvectorized();
}
