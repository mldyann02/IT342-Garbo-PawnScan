package edu.cit.garbo.pawnscan.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeReportFile(MultipartFile file);
}