package edu.cit.garbo.pawnscan.shared.email;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, Long> {
    
    Optional<OtpEntity> findTopByEmailAndUsedFalseOrderByExpiresAtDesc(String email);
}
