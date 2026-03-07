package edu.cit.garbo.pawnscan.repository;

import edu.cit.garbo.pawnscan.entity.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    Optional<BusinessProfile> findByPermitNumber(String permitNumber);

    List<BusinessProfile> findByIsVerified(Boolean isVerified);
}