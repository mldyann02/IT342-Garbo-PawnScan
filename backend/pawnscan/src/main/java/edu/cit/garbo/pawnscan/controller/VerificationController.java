package edu.cit.garbo.pawnscan.controller;

import edu.cit.garbo.pawnscan.dto.SearchLogRequest;
import edu.cit.garbo.pawnscan.dto.SearchLogResponse;
import edu.cit.garbo.pawnscan.dto.StolenMatchResponse;
import edu.cit.garbo.pawnscan.dto.VerifySearchResponse;
import edu.cit.garbo.pawnscan.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/verify")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUSINESS')")
public class VerificationController {

    private final VerificationService verificationService;

    @GetMapping("/search")
    public VerifySearchResponse search(
            Authentication authentication,
            @RequestParam("serial") String serial) {
        return verificationService.verifySerial(authentication.getName(), serial);
    }

    @PostMapping("/log")
    public SearchLogResponse log(
            Authentication authentication,
            @Valid @RequestBody SearchLogRequest request) {
        return verificationService.logSearch(authentication.getName(), request.getSerial());
    }

    @GetMapping("/history")
    public List<SearchLogResponse> history(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return verificationService.getSearchHistory(authentication.getName(), page, size);
    }

    @GetMapping("/matches")
    public List<StolenMatchResponse> matches(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return verificationService.getStolenMatches(authentication.getName(), page, size);
    }
}
