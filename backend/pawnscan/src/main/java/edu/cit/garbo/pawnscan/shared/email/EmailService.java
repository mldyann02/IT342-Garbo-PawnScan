package edu.cit.garbo.pawnscan.shared.email;

import edu.cit.garbo.pawnscan.features.reports.entity.Report;

public interface EmailService {

    void sendOtpEmail(String to, String otp);

    void sendWelcomeEmail(String to, String fullName);

    void sendReportStatusEmail(String to, Report report, String newStatus);

    void sendPasswordResetEmail(String to, String resetLink);
}
