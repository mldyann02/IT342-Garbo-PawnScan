package edu.cit.garbo.pawnscan.features.verification;

import edu.cit.garbo.pawnscan.features.verification.dto.SearchLogRequest;
import edu.cit.garbo.pawnscan.features.verification.dto.SearchLogResponse;
import edu.cit.garbo.pawnscan.features.verification.dto.StolenMatchResponse;
import edu.cit.garbo.pawnscan.features.verification.dto.VerifySearchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        return verificationService.verifySerial(resolveAuthenticatedName(authentication), serial);
    }

    @PostMapping("/log")
    public SearchLogResponse log(
            Authentication authentication,
            @Valid @RequestBody SearchLogRequest request) {
        return verificationService.logSearch(resolveAuthenticatedName(authentication), request.getSerial());
    }

    @GetMapping("/history")
    public List<SearchLogResponse> history(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return verificationService.getSearchHistory(resolveAuthenticatedName(authentication), page, size);
    }

    @GetMapping("/matches")
    public List<StolenMatchResponse> matches(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return verificationService.getStolenMatches(resolveAuthenticatedName(authentication), page, size);
    }

    private String resolveAuthenticatedName(Authentication authentication) {
        Authentication currentAuthentication = authentication != null
                ? authentication
                : SecurityContextHolder.getContext().getAuthentication();

        if (currentAuthentication == null || currentAuthentication.getName() == null) {
            throw new IllegalStateException("Authentication is required");
        }

        return currentAuthentication.getName();
    }
}







