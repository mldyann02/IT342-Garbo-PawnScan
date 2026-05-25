package edu.cit.garbo.pawnscan.features.verification.repository;

import edu.cit.garbo.pawnscan.features.verification.entity.SearchLog;
import edu.cit.garbo.pawnscan.features.verification.entity.VerificationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    Page<SearchLog> findByBusinessUserUserId(Long businessUserId, Pageable pageable);

    Page<SearchLog> findByBusinessUserUserIdAndResult(Long businessUserId, VerificationResult result,
            Pageable pageable);

    @Query("""
            SELECT sl FROM SearchLog sl
            JOIN FETCH sl.matchedReport r
            JOIN FETCH sl.businessUser bu
            WHERE r.user.userId = :ownerUserId
            AND sl.result = :result
            ORDER BY sl.searchedAt DESC
            """)
    List<SearchLog> findMatchedReportsForOwner(
            @Param("ownerUserId") Long ownerUserId,
            @Param("result") VerificationResult result,
            Pageable pageable);
}







