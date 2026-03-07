package edu.cit.garbo.pawnscan.controller;

import edu.cit.garbo.pawnscan.dto.BusinessProfileRequest;
import edu.cit.garbo.pawnscan.dto.BusinessProfileResponse;
import edu.cit.garbo.pawnscan.dto.BusinessVerificationRequest;
import edu.cit.garbo.pawnscan.entity.UserRole;
import edu.cit.garbo.pawnscan.service.BusinessProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/business-profiles")
@RequiredArgsConstructor
public class BusinessProfileController {

    private final BusinessProfileService businessProfileService;

    @PostMapping("/{userId}")
    public ResponseEntity<BusinessProfileResponse> createProfile(
            @PathVariable Long userId,
            @Valid @RequestBody BusinessProfileRequest request,
            @RequestHeader("X-Requester-User-Id") Long requesterUserId,
            @RequestHeader("X-Requester-Role") UserRole requesterRole
    ) {
        BusinessProfileResponse response = businessProfileService.createProfile(userId, request, requesterUserId, requesterRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BusinessProfileResponse>> getProfiles(
            @RequestParam(required = false) Boolean isVerified,
            @RequestHeader("X-Requester-Role") UserRole requesterRole
    ) {
        List<BusinessProfileResponse> response = businessProfileService.getProfiles(isVerified, requesterRole);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<BusinessProfileResponse> getProfile(
            @PathVariable Long userId,
            @RequestHeader("X-Requester-User-Id") Long requesterUserId,
            @RequestHeader("X-Requester-Role") UserRole requesterRole
    ) {
        BusinessProfileResponse response = businessProfileService.getProfile(userId, requesterUserId, requesterRole);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<BusinessProfileResponse> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody BusinessProfileRequest request,
            @RequestHeader("X-Requester-User-Id") Long requesterUserId,
            @RequestHeader("X-Requester-Role") UserRole requesterRole
    ) {
        BusinessProfileResponse response = businessProfileService.updateProfile(userId, request, requesterUserId, requesterRole);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/verification")
    public ResponseEntity<BusinessProfileResponse> updateVerification(
            @PathVariable Long userId,
            @Valid @RequestBody BusinessVerificationRequest request,
            @RequestHeader("X-Requester-Role") UserRole requesterRole
    ) {
        BusinessProfileResponse response = businessProfileService.updateVerification(userId, request.getIsVerified(), requesterRole);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteProfile(
            @PathVariable Long userId,
            @RequestHeader("X-Requester-User-Id") Long requesterUserId,
            @RequestHeader("X-Requester-Role") UserRole requesterRole
    ) {
        businessProfileService.deleteProfile(userId, requesterUserId, requesterRole);
        return ResponseEntity.noContent().build();
    }
}