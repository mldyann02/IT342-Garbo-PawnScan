package edu.cit.garbo.pawnscan.features.verification.repository;

import edu.cit.garbo.pawnscan.features.verification.entity.SearchLog;
import edu.cit.garbo.pawnscan.features.verification.entity.VerificationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    Page<SearchLog> findByBusinessUserUserId(Long businessUserId, Pageable pageable);

    Page<SearchLog> findByBusinessUserUserIdAndResult(Long businessUserId, VerificationResult result,
            Pageable pageable);
}







