package edu.cit.garbo.pawnscan.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String uploadPath;

    public WebMvcConfig(@Value("${app.upload.base-dir:uploads}") String uploadBaseDir) {
        this.uploadPath = Path.of(uploadBaseDir).toAbsolutePath().normalize().toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}