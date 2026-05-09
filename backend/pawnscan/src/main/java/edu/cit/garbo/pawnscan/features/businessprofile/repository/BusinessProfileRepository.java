package edu.cit.garbo.pawnscan.features.businessprofile.repository;

import edu.cit.garbo.pawnscan.features.businessprofile.entity.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    Optional<BusinessProfile> findByPermitNumber(String permitNumber);

    List<BusinessProfile> findByIsVerified(Boolean isVerified);
}







