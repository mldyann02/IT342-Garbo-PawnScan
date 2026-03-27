package edu.cit.garbo.pawnscan.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        Map<String, Object> props = new HashMap<>();
        for (DotenvEntry entry : dotenv.entries()) {
            props.put(entry.getKey(), entry.getValue());
        }
        environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", props));
    }
}
