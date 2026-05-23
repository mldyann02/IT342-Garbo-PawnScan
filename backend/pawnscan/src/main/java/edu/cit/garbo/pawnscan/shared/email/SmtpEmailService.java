package edu.cit.garbo.pawnscan.shared.email;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpEmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.from:no-reply@pawnscan.local}")
    private String from;

    @Override
    @Async
    public void sendWelcomeEmail(String to, String fullName) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            LOGGER.info("SMTP is not configured; skipped welcome email for {}", to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("Welcome to PawnScan");
            message.setText("""
                    Welcome to PawnScan, %s!

                    Your account is ready. You can now report and track high-value items securely from your dashboard.

                    PawnScan Team
                    """.formatted(fullName == null || fullName.isBlank() ? "there" : fullName));

            mailSender.send(message);
        } catch (MailException ex) {
            LOGGER.warn("Failed to send welcome email to {}", to, ex);
        }
    }
}
