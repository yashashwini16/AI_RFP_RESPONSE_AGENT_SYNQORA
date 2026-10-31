package com.azentio.rfp.repository;

import com.azentio.rfp.model.RfpResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RfpResponseRepository extends JpaRepository<RfpResponse, Long> {
    Optional<RfpResponse> findByQueryId(Long queryId);
}
