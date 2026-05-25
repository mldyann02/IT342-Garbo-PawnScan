package edu.cit.garbo.pawnscan.features.verification.publicapi;

import lombok.Builder;

@Builder
public record PublicStolenItemMatch(
        boolean stolen,
        String source,
        String title,
        String url) {

    public static PublicStolenItemMatch clean(String source) {
        return PublicStolenItemMatch.builder()
                .stolen(false)
                .source(source)
                .build();
    }
}
