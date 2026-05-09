package edu.cit.garbo.pawnscan.features.reports.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeReportFile(MultipartFile file);
}







