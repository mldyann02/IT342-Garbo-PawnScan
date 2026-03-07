package edu.cit.garbo.pawnscan.service;

import edu.cit.garbo.pawnscan.dto.BusinessProfileRequest;
import edu.cit.garbo.pawnscan.dto.BusinessProfileResponse;
import edu.cit.garbo.pawnscan.dto.BusinessProfileSummaryResponse;
import edu.cit.garbo.pawnscan.entity.User;
import edu.cit.garbo.pawnscan.entity.UserRole;

import java.util.List;
import java.util.Optional;

public interface BusinessProfileService {

    BusinessProfileResponse createProfile(Long targetUserId, BusinessProfileRequest request, Long actorUserId, UserRole actorRole);

    BusinessProfileResponse getProfile(Long targetUserId, Long actorUserId, UserRole actorRole);

    List<BusinessProfileResponse> getProfiles(Boolean isVerified, UserRole actorRole);

    BusinessProfileResponse updateProfile(Long targetUserId, BusinessProfileRequest request, Long actorUserId, UserRole actorRole);

    BusinessProfileResponse updateVerification(Long targetUserId, boolean isVerified, UserRole actorRole);

    void deleteProfile(Long targetUserId, Long actorUserId, UserRole actorRole);

    BusinessProfileResponse createProfileForRegistration(User user, BusinessProfileRequest request);

    Optional<BusinessProfileSummaryResponse> getSummaryByUserId(Long userId);
}