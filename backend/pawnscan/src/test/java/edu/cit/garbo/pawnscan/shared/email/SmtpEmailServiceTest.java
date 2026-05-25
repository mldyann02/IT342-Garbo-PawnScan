package edu.cit.garbo.pawnscan.shared.email;

import edu.cit.garbo.pawnscan.features.reports.entity.Report;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SmtpEmailServiceTest {

    private JavaMailSender mailSender;
    private SmtpEmailService emailService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        ObjectProvider<JavaMailSender> mailSenderProvider = mock(ObjectProvider.class);
        Session session = Session.getInstance(new Properties());

        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> new MimeMessage(session));

        emailService = new SmtpEmailService(mailSenderProvider);
        ReflectionTestUtils.setField(emailService, "from", "no-reply@pawnscan.test");
    }

    @Test
    void sendOtpEmailSendsVerificationCodeMessage() throws Exception {
        emailService.sendOtpEmail("user@test.com", "123456");

        MimeMessage message = sentMessage();
        assertThat(message.getSubject()).isEqualTo("Your PawnScan Verification Code");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("user@test.com");
    }

    @Test
    void sendWelcomeEmailSendsWelcomeMessage() throws Exception {
        emailService.sendWelcomeEmail("user@test.com", "Juan");

        MimeMessage message = sentMessage();
        assertThat(message.getSubject()).isEqualTo("Welcome to PawnScan");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("user@test.com");
    }

    @Test
    void sendReportStatusEmailSendsReportUpdateMessage() throws Exception {
        Report report = Report.builder()
                .itemModel("iPhone 15")
                .build();

        emailService.sendReportStatusEmail("owner@test.com", report, "APPROVED");

        MimeMessage message = sentMessage();
        assertThat(message.getSubject()).isEqualTo("PawnScan Report Update: iPhone 15");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("owner@test.com");
    }

    @Test
    void sendEmailSkipsBlankRecipient() {
        emailService.sendOtpEmail(" ", "123456");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    private MimeMessage sentMessage() {
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        return messageCaptor.getValue();
    }
}
