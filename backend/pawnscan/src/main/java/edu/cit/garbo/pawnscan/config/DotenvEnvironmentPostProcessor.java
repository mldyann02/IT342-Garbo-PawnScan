package edu.cit.garbo.pawnscan.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(DotenvEnvironmentPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        Map<String, Object> props = new HashMap<>();
        for (DotenvEntry entry : dotenv.entries()) {
            props.put(entry.getKey(), entry.getValue());
        }
        if (!props.isEmpty()) {
            log.info("Loaded {} entries from .env", props.size());
            props.forEach((k, v) -> log.debug("dotenv {}={}", k, v));
            environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", props));
        } else {
            log.info("No .env entries found (ignoreIfMissing=true)");
        }
    }
}
