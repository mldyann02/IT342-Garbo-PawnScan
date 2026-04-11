package edu.cit.garbo.pawnscan.repository;

import edu.cit.garbo.pawnscan.entity.SearchLog;
import edu.cit.garbo.pawnscan.entity.VerificationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    Page<SearchLog> findByBusinessUserUserId(Long businessUserId, Pageable pageable);

    Page<SearchLog> findByBusinessUserUserIdAndResult(Long businessUserId, VerificationResult result,
            Pageable pageable);
}