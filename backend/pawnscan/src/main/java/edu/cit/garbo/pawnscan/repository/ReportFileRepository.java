package edu.cit.garbo.pawnscan.repository;

import edu.cit.garbo.pawnscan.entity.ReportFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportFileRepository extends JpaRepository<ReportFile, Long> {
}