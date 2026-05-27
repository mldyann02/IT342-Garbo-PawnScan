package edu.cit.garbo.pawnscan.shared.email;

import edu.cit.garbo.pawnscan.features.reports.entity.Report;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

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
                """.formatted(HtmlUtils.htmlEscape(otp));

        sendHtmlEmail(to, "Your PawnScan Verification Code", html, "OTP");
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String fullName) {
        String name = StringUtils.hasText(fullName) ? fullName.trim() : "there";
        String html = """
                <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <h2 style="color: #00d287;">Welcome to PawnScan, %s!</h2>
                    <p>Your account has been successfully verified.</p>
                    <p>You can now report and track high-value items securely from your dashboard.</p>
                    <br>
                    <p>Best regards,<br><strong>The PawnScan Team</strong></p>
                </div>
                """.formatted(HtmlUtils.htmlEscape(name));

        sendHtmlEmail(to, "Welcome to PawnScan", html, "welcome");
    }

    @Override
    @Async
    public void sendReportStatusEmail(String to, Report report, String newStatus) {
        String itemModel = report != null && StringUtils.hasText(report.getItemModel())
                ? report.getItemModel().trim()
                : "your report";
        String status = StringUtils.hasText(newStatus) ? newStatus.trim() : "UPDATED";
        String statusColor = status.equalsIgnoreCase("APPROVED") ? "#00d287"
                : status.equalsIgnoreCase("REJECTED") ? "#ef4444" : "#3b82f6";

        String html = """
                <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <h2>Report Status Update</h2>
                    <p>There has been an update regarding your report for <strong>%s</strong>.</p>
                    <p>New Status: <span style="color: %s; font-weight: bold;">%s</span></p>
                    <br>
                    <p>Log in to your PawnScan dashboard to view the full details.</p>
                </div>
                """.formatted(
                HtmlUtils.htmlEscape(itemModel),
                statusColor,
                HtmlUtils.htmlEscape(status.toUpperCase()));

        sendHtmlEmail(to, "PawnScan Report Update: " + itemModel, html, "report status");
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String resetLink) {
        String safeLink = HtmlUtils.htmlEscape(resetLink);
        String html = """
                <div style="font-family: Arial, sans-serif; padding: 20px; color: #333; max-width: 600px; margin: 0 auto;">
                    <div style="text-align: center; margin-bottom: 24px;">
                        <h2 style="color: #00d287; margin-bottom: 4px;">PawnScan</h2>
                    </div>
                    <h3 style="margin-bottom: 8px;">Password Reset Request</h3>
                    <p>We received a request to reset your PawnScan account password.</p>
                    <p>Click the button below to choose a new password. This link expires in <strong>60 minutes</strong>.</p>
                    <div style="text-align: center; margin: 32px 0;">
                        <a href="%s"
                           style="display: inline-block; background-color: #00d287; color: #0b0f1a; font-weight: bold;
                                  font-size: 16px; padding: 14px 32px; border-radius: 8px; text-decoration: none;">
                            Reset My Password
                        </a>
                    </div>
                    <p style="font-size: 13px; color: #666;">
                        If you didn't request a password reset, you can safely ignore this email — your password will not change.
                    </p>
                    <p style="font-size: 13px; color: #666;">
                        If the button above doesn't work, copy and paste this link into your browser:<br>
                        <a href="%s" style="color: #00d287; word-break: break-all;">%s</a>
                    </p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;" />
                    <p style="font-size: 12px; color: #999;">Best regards,<br><strong>The PawnScan Team</strong></p>
                </div>
                """.formatted(safeLink, safeLink, safeLink);

        sendHtmlEmail(to, "Reset Your PawnScan Password", html, "password reset");
    }

    private void sendHtmlEmail(String to, String subject, String html, String emailType) {
        if (!StringUtils.hasText(to)) {
            LOGGER.warn("Skipped {} email because the recipient address is blank", emailType);
            return;
        }

        if (!StringUtils.hasText(from)) {
            LOGGER.warn("Skipped {} email to {} because app.mail.from is blank", emailType, to);
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            LOGGER.warn("Skipped {} email to {} because JavaMailSender is not configured", emailType, to);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from.trim());
            helper.setTo(to.trim());
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            LOGGER.info("Sent {} email to {}", emailType, to);
        } catch (MessagingException | MailException ex) {
            LOGGER.warn("Failed to send {} email to {}", emailType, to, ex);
        }
    }
}
