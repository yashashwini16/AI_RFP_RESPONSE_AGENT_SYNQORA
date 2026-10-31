package com.azentio.rfp.repository;

import com.azentio.rfp.model.ComplianceCodeScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplianceCodeSchemeRepository extends JpaRepository<ComplianceCodeScheme, Long> {
    List<ComplianceCodeScheme> findBySchemeKeyOrderByIdAsc(String schemeKey);
    Optional<ComplianceCodeScheme> findBySchemeKeyAndCode(String schemeKey, String code);
}
