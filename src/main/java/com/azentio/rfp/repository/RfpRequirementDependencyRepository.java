package com.azentio.rfp.repository;

import com.azentio.rfp.model.RfpRequirementDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RfpRequirementDependencyRepository extends JpaRepository<RfpRequirementDependency, Long> {
    List<RfpRequirementDependency> findByDocumentIdOrderByIdAsc(Long documentId);
    List<RfpRequirementDependency> findBySourceRequirementId(String sourceRequirementId);
    List<RfpRequirementDependency> findByTargetRequirementId(String targetRequirementId);
    void deleteByDocumentId(Long documentId);
}
