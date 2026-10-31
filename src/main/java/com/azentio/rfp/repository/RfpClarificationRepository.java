package com.azentio.rfp.repository;

import com.azentio.rfp.model.RfpClarification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RfpClarificationRepository extends JpaRepository<RfpClarification, Long> {
    List<RfpClarification> findAllByOrderByIdAsc();
    List<RfpClarification> findByDocumentIdOrderByIdAsc(Long documentId);
    void deleteByQueryId(Long queryId);
    boolean existsByQueryId(Long queryId);
}
