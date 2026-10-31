package com.azentio.rfp.repository;

import com.azentio.rfp.model.RfpAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RfpAuditLogRepository extends JpaRepository<RfpAuditLog, Long> {
    List<RfpAuditLog> findByQueryId(Long queryId);
}
