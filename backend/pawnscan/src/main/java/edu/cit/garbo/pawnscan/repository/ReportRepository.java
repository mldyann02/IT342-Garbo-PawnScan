package edu.cit.garbo.pawnscan.repository;

import edu.cit.garbo.pawnscan.entity.Report;
import edu.cit.garbo.pawnscan.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("SELECT r FROM Report r WHERE r.user.userId = :userId ORDER BY r.createdAt DESC")
    List<Report> findByUserId(@Param("userId") Long userId);

    boolean existsBySerialNumberIgnoreCase(String serialNumber);

    boolean existsBySerialNumberIgnoreCaseAndIdNot(String serialNumber, Long id);

    Optional<Report> findFirstBySerialNumberIgnoreCaseAndStatus(String serialNumber, ReportStatus status);
}