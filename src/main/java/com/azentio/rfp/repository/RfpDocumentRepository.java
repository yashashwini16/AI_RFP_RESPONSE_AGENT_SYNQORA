package com.azentio.rfp.repository;

import com.azentio.rfp.model.RfpDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RfpDocumentRepository extends JpaRepository<RfpDocument, Long> {
    Optional<RfpDocument> findTopByOrderByIdDesc();
}
