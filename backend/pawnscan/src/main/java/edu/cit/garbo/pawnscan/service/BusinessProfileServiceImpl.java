package edu.cit.garbo.pawnscan.service;

import edu.cit.garbo.pawnscan.dto.BusinessProfileRequest;
import edu.cit.garbo.pawnscan.dto.BusinessProfileResponse;
import edu.cit.garbo.pawnscan.dto.BusinessProfileSummaryResponse;
import edu.cit.garbo.pawnscan.entity.BusinessProfile;
import edu.cit.garbo.pawnscan.entity.User;
import edu.cit.garbo.pawnscan.entity.UserRole;
import edu.cit.garbo.pawnscan.exception.BusinessProfileAlreadyExistsException;
import edu.cit.garbo.pawnscan.exception.BusinessProfileNotFoundException;
import edu.cit.garbo.pawnscan.exception.ForbiddenActionException;
import edu.cit.garbo.pawnscan.exception.InvalidBusinessProfileException;
import edu.cit.garbo.pawnscan.repository.BusinessProfileRepository;
import edu.cit.garbo.pawnscan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BusinessProfileServiceImpl implements BusinessProfileService {

    private final BusinessProfileRepository businessProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BusinessProfileResponse createProfile(Long targetUserId, BusinessProfileRequest request, Long actorUserId, UserRole actorRole) {
        ensureAdminOrBusinessOwner(targetUserId, actorUserId, actorRole);
        User user = getBusinessUserOrThrow(targetUserId);

        if (businessProfileRepository.existsById(targetUserId)) {
            throw new BusinessProfileAlreadyExistsException("Business profile already exists for user id: " + targetUserId);
        }

        BusinessProfile savedProfile = businessProfileRepository.save(BusinessProfile.builder()
                .user(user)
                .businessName(request.getBusinessName().trim())
                .businessAddress(request.getBusinessAddress().trim())
                .permitNumber(request.getPermitNumber().trim())
                .isVerified(false)
                .build());

        return toResponse(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessProfileResponse getProfile(Long targetUserId, Long actorUserId, UserRole actorRole) {
        ensureAdminOrBusinessOwner(targetUserId, actorUserId, actorRole);
        return toResponse(getProfileOrThrow(targetUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessProfileResponse> getProfiles(Boolean isVerified, UserRole actorRole) {
        ensureAdmin(actorRole);

        List<BusinessProfile> profiles = isVerified == null
                ? businessProfileRepository.findAll()
                : businessProfileRepository.findByIsVerified(isVerified);

        return profiles.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public BusinessProfileResponse updateProfile(Long targetUserId, BusinessProfileRequest request, Long actorUserId, UserRole actorRole) {
        ensureAdminOrBusinessOwner(targetUserId, actorUserId, actorRole);
        getBusinessUserOrThrow(targetUserId);

        BusinessProfile profile = getProfileOrThrow(targetUserId);
        profile.setBusinessName(request.getBusinessName().trim());
        profile.setBusinessAddress(request.getBusinessAddress().trim());
        profile.setPermitNumber(request.getPermitNumber().trim());

        return toResponse(businessProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public BusinessProfileResponse updateVerification(Long targetUserId, boolean isVerified, UserRole actorRole) {
        ensureAdmin(actorRole);

        BusinessProfile profile = getProfileOrThrow(targetUserId);
        profile.setIsVerified(isVerified);

        return toResponse(businessProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public void deleteProfile(Long targetUserId, Long actorUserId, UserRole actorRole) {
        ensureAdminOrBusinessOwner(targetUserId, actorUserId, actorRole);

        if (!businessProfileRepository.existsById(targetUserId)) {
            throw new BusinessProfileNotFoundException("Business profile not found for user id: " + targetUserId);
        }

        businessProfileRepository.deleteById(targetUserId);
    }

    @Override
    @Transactional
    public BusinessProfileResponse createProfileForRegistration(User user, BusinessProfileRequest request) {
        if (user.getRole() != UserRole.BUSINESS) {
            throw new InvalidBusinessProfileException("Only users with BUSINESS role can have a business profile");
        }

        if (businessProfileRepository.existsById(user.getUserId())) {
            throw new BusinessProfileAlreadyExistsException("Business profile already exists for user id: " + user.getUserId());
        }

        validateRequiredFields(request);

        BusinessProfile savedProfile = businessProfileRepository.save(BusinessProfile.builder()
                .user(user)
                .businessName(request.getBusinessName().trim())
                .businessAddress(request.getBusinessAddress().trim())
                .permitNumber(request.getPermitNumber().trim())
                .isVerified(false)
                .build());

        return toResponse(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BusinessProfileSummaryResponse> getSummaryByUserId(Long userId) {
        return businessProfileRepository.findById(userId)
                .map(this::toSummaryResponse);
    }

    private BusinessProfile getProfileOrThrow(Long userId) {
        return businessProfileRepository.findById(userId)
                .orElseThrow(() -> new BusinessProfileNotFoundException("Business profile not found for user id: " + userId));
    }

    private User getBusinessUserOrThrow(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessProfileNotFoundException("User not found for user id: " + userId));

        if (user.getRole() != UserRole.BUSINESS) {
            throw new InvalidBusinessProfileException("Only users with BUSINESS role can have a business profile");
        }

        return user;
    }

    private void validateRequiredFields(BusinessProfileRequest request) {
        if (request == null
                || isBlank(request.getBusinessName())
                || isBlank(request.getBusinessAddress())
                || isBlank(request.getPermitNumber())) {
            throw new InvalidBusinessProfileException("Business name, business address, and permit number are required for BUSINESS users");
        }
    }

    private void ensureAdmin(UserRole actorRole) {
        if (actorRole != UserRole.ADMIN) {
            throw new ForbiddenActionException("Only ADMIN can perform this action");
        }
    }

    private void ensureAdminOrBusinessOwner(Long targetUserId, Long actorUserId, UserRole actorRole) {
        if (actorRole == UserRole.ADMIN) {
            return;
        }

        if (actorRole == UserRole.BUSINESS && targetUserId.equals(actorUserId)) {
            return;
        }

        throw new ForbiddenActionException("Access denied for this business profile operation");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private BusinessProfileResponse toResponse(BusinessProfile profile) {
        return BusinessProfileResponse.builder()
                .userId(profile.getUserId())
                .businessName(profile.getBusinessName())
                .businessAddress(profile.getBusinessAddress())
                .permitNumber(profile.getPermitNumber())
                .isVerified(Boolean.TRUE.equals(profile.getIsVerified()))
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private BusinessProfileSummaryResponse toSummaryResponse(BusinessProfile profile) {
        return BusinessProfileSummaryResponse.builder()
                .userId(profile.getUserId())
                .businessName(profile.getBusinessName())
                .isVerified(Boolean.TRUE.equals(profile.getIsVerified()))
                .build();
    }
}