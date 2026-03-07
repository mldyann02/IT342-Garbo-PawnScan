package edu.cit.garbo.pawnscan.config;

import edu.cit.garbo.pawnscan.entity.User;
import edu.cit.garbo.pawnscan.entity.UserRole;
import edu.cit.garbo.pawnscan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        try {
            boolean adminExists = !userRepository.findByRole(UserRole.ADMIN).isEmpty();
            if (adminExists) {
                logger.info("Admin user already exists; skipping AdminSeeder.");
                return;
            }

            String adminEmail = "pawnscan@gmail.com";
            if (userRepository.existsByEmail(adminEmail)) {
                logger.info("An account with email {} already exists but no ADMIN role found; skipping creation.", adminEmail);
                return;
            }

            User admin = User.builder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("PawnScan_Admin2026!"))
                    .fullName("System Administrator")
                    .role(UserRole.ADMIN)
                    .build();

            userRepository.save(admin);
            logger.info("Seeded admin user with email {}", adminEmail);
        } catch (Exception e) {
            logger.error("Failed to seed admin user", e);
        }
    }
}
