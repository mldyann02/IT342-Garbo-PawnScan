package edu.cit.garbo.pawnscan.features.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Invalidate any existing unused tokens for the email before issuing a new one,
     * so that only the most recently issued reset link is valid.
     */
    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.email = :email AND p.used = false")
    void invalidateAllForEmail(@Param("email") String email);
}
