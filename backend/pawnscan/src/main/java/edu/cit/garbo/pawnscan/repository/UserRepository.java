package edu.cit.garbo.pawnscan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.garbo.pawnscan.entity.User;
import edu.cit.garbo.pawnscan.entity.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(UserRole role);

    Optional<User> findById(Long id);
}
