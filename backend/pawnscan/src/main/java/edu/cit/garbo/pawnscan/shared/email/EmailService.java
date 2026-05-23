package edu.cit.garbo.pawnscan.shared.email;

public interface EmailService {

    void sendWelcomeEmail(String to, String fullName);
}
