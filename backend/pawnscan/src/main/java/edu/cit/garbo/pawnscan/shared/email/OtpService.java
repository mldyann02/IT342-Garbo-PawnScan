package edu.cit.garbo.pawnscan.shared.email;

import edu.cit.garbo.pawnscan.features.auth.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;

    @Transactional
    public void generateAndSendOtp(String email) {
        String code = generateCode();

        OtpEntity otpEntity = OtpEntity.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .used(false)
                .build();

        otpRepository.save(otpEntity);
        emailService.sendOtpEmail(email, code);
    }

    @Transactional
    public boolean verifyOtp(String email, String code) {
        OtpEntity otpEntity = otpRepository.findTopByEmailAndUsedFalseOrderByExpiresAtDesc(email)
                .orElseThrow(() -> new InvalidCredentialsException("No active OTP found for this email"));

        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("OTP has expired");
        }

        if (!otpEntity.getCode().equals(code)) {
            throw new InvalidCredentialsException("Invalid OTP code");
        }

        otpEntity.setUsed(true);
        otpRepository.save(otpEntity);
        return true;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }
}
