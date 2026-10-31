package com.azentio.rfp.repository;

import com.azentio.rfp.model.RfpQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RfpQueryRepository extends JpaRepository<RfpQuery, Long> {
    List<RfpQuery> findByRfpTitleOrderByIdAsc(String rfpTitle);
    List<RfpQuery> findAllByOrderByIdAsc();
    List<RfpQuery> findAllByOrderByIdDesc();
}
