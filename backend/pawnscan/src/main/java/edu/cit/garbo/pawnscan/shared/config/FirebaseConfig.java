package edu.cit.garbo.pawnscan.shared.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${pawnscan.firebase.credentials}")
    private String firebaseCredentials;

    @PostConstruct
    public void initializeFirebase() {
        if (firebaseCredentials == null || firebaseCredentials.trim().isEmpty()) {
            LOGGER.warn("FIREBASE_CREDENTIALS environment variable is empty. Firebase Admin SDK will not be initialized.");
            return;
        }

        try {
            if (FirebaseApp.getApps().isEmpty()) {
                ByteArrayInputStream stream = new ByteArrayInputStream(
                        firebaseCredentials.getBytes(StandardCharsets.UTF_8)
                );

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(stream))
                        .build();

                FirebaseApp.initializeApp(options);
                LOGGER.info("Firebase Admin SDK has been successfully initialized from env credentials.");
            } else {
                LOGGER.info("Firebase App is already initialized.");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to initialize Firebase Admin SDK", e);
        }
    }
}
