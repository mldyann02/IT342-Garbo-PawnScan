package edu.cit.garbo.pawnscan.features.businessprofile;

import edu.cit.garbo.pawnscan.features.businessprofile.dto.BusinessProfileRequest;
import edu.cit.garbo.pawnscan.features.businessprofile.dto.BusinessProfileResponse;
import edu.cit.garbo.pawnscan.features.businessprofile.dto.BusinessProfileSummaryResponse;
import edu.cit.garbo.pawnscan.shared.user.User;
import edu.cit.garbo.pawnscan.shared.user.UserRole;

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







