package edu.cit.garbo.pawnscan.service;

import edu.cit.garbo.pawnscan.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path uploadDir;

    public FileStorageServiceImpl(@Value("${app.upload.base-dir:uploads}") String uploadBaseDir) {
        this.uploadDir = Path.of(uploadBaseDir).toAbsolutePath().normalize();
    }

    @Override
    public String storeReportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Files.createDirectories(uploadDir);

            String extension = extractExtension(file.getOriginalFilename());
            String generatedName = UUID.randomUUID() + extension;
            Path destination = uploadDir.resolve(generatedName).normalize();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/" + generatedName;
        } catch (IOException ex) {
            throw new FileStorageException("Failed to store uploaded file", ex);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        String ext = filename.substring(lastDotIndex).toLowerCase(Locale.ROOT);
        if (ext.length() > 10) {
            return "";
        }

        return ext;
    }
}