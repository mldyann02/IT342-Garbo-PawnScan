package edu.cit.garbo.pawnscan.shared.email;

import edu.cit.garbo.pawnscan.features.reports.entity.Report;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    public void sendOtpEmail(String to, String otp) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Your PawnScan Verification Code");
            
            String html = """
                <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <h2>Verify Your Email</h2>
                    <p>Thank you for registering with PawnScan.</p>
                    <p>Your verification code is:</p>
                    <div style="background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; border-radius: 5px; margin: 20px 0;">
                        %s
                    </div>
                    <p>This code will expire in 10 minutes.</p>
                    <p>If you didn't request this, you can safely ignore this email.</p>
                </div>
                """.formatted(otp);
                
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            LOGGER.warn("Failed to send OTP email to {}", to, ex);
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String fullName) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            LOGGER.info("SMTP is not configured; skipped welcome email for {}", to);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Welcome to PawnScan");
            
            String name = (fullName == null || fullName.isBlank()) ? "there" : fullName;
            String html = """
                <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <h2 style="color: #00d287;">Welcome to PawnScan, %s!</h2>
                    <p>Your account has been successfully verified.</p>
                    <p>You can now report and track high-value items securely from your dashboard.</p>
                    <br>
                    <p>Best regards,<br><strong>The PawnScan Team</strong></p>
                </div>
                """.formatted(name);

            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            LOGGER.warn("Failed to send welcome email to {}", to, ex);
        }
    }

    @Override
    @Async
    public void sendReportStatusEmail(String to, Report report, String newStatus) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("PawnScan Report Update: " + report.getItemModel());
            
            String statusColor = newStatus.equalsIgnoreCase("APPROVED") ? "#00d287" :
                                 newStatus.equalsIgnoreCase("REJECTED") ? "#ef4444" : "#3b82f6";

            String html = """
                <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <h2>Report Status Update</h2>
                    <p>There has been an update regarding your report for <strong>%s</strong>.</p>
                    <p>New Status: <span style="color: %s; font-weight: bold;">%s</span></p>
                    <br>
                    <p>Log in to your PawnScan dashboard to view the full details.</p>
                </div>
                """.formatted(report.getItemModel(), statusColor, newStatus.toUpperCase());

            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            LOGGER.warn("Failed to send report status email to {}", to, ex);
        }
    }
}
