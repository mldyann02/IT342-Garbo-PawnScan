package edu.cit.garbo.pawnscan.service;

import edu.cit.garbo.pawnscan.dto.SearchLogResponse;
import edu.cit.garbo.pawnscan.dto.StolenMatchResponse;
import edu.cit.garbo.pawnscan.dto.VerifySearchResponse;

import java.util.List;

public interface VerificationService {

    VerifySearchResponse verifySerial(String authenticatedEmail, String serial);

    SearchLogResponse logSearch(String authenticatedEmail, String serial);

    List<SearchLogResponse> getSearchHistory(String authenticatedEmail, int page, int size);

    List<StolenMatchResponse> getStolenMatches(String authenticatedEmail, int page, int size);
}
