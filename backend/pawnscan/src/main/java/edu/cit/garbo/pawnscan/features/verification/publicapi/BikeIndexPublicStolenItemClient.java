package edu.cit.garbo.pawnscan.features.verification.publicapi;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class BikeIndexPublicStolenItemClient implements PublicStolenItemClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BikeIndexPublicStolenItemClient.class);
    private static final String SOURCE = "Bike Index";

    private final RestClient.Builder restClientBuilder;

    @Value("${app.public-stolen-api.bike-index-base-url:https://bikeindex.org/api/v3}")
    private String bikeIndexBaseUrl;

    @Override
    public PublicStolenItemMatch searchBySerial(String serial) {
        try {
            JsonNode response = restClientBuilder.build()
                    .get()
                    .uri(bikeIndexBaseUrl + "/search?serial={serial}&stolenness=stolen", serial)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode bikes = response == null ? null : response.path("bikes");
            if (bikes == null || !bikes.isArray() || bikes.size() == 0) {
                return PublicStolenItemMatch.clean(SOURCE);
            }

            JsonNode firstBike = bikes.get(0);
            Long bikeId = firstBike.path("id").canConvertToLong() ? firstBike.path("id").asLong() : null;

            return PublicStolenItemMatch.builder()
                    .stolen(true)
                    .source(SOURCE)
                    .title(firstBike.path("title").asText("Stolen bike registration"))
                    .url(bikeId == null ? "https://bikeindex.org" : "https://bikeindex.org/bikes/" + bikeId)
                    .build();
        } catch (RuntimeException ex) {
            LOGGER.warn("Public stolen item API lookup failed for serial {}", serial, ex);
            return PublicStolenItemMatch.clean(SOURCE);
        }
    }
}
