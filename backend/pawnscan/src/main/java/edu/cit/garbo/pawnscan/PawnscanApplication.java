package edu.cit.garbo.pawnscan;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PawnscanApplication {

	private static final Logger log = LoggerFactory.getLogger(PawnscanApplication.class);

	public static void main(String[] args) {
		// Load .env into system properties so Spring can resolve ${...} placeholders
		try {
			Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
			for (DotenvEntry entry : dotenv.entries()) {
				System.setProperty(entry.getKey(), entry.getValue());
			}
			log.info("Loaded {} .env entries into system properties", dotenv.entries().size());
		} catch (Exception e) {
			log.warn("Failed to load .env: {}", e.getMessage());
		}

		SpringApplication.run(PawnscanApplication.class, args);
	}

}
