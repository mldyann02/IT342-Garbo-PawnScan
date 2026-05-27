package edu.cit.garbo.pawnscan.features.notifications.repository;

import edu.cit.garbo.pawnscan.features.notifications.entity.FcmDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmDeviceTokenRepository extends JpaRepository<FcmDeviceToken, Long> {

    List<FcmDeviceToken> findByUserUserId(Long userId);

    Optional<FcmDeviceToken> findByToken(String token);

    void deleteByUserUserIdAndToken(Long userId, String token);

    void deleteByToken(String token);
}
