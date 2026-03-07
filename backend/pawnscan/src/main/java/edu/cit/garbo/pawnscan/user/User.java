package edu.cit.garbo.pawnscan.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // Primary key for the users table.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // Email used for identity and login. Must be unique.
    @Column(nullable = false, unique = true)
    private String email;

    // Hashed password only. Not exposed in JSON responses.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Display name of the account owner.
    @Column(name = "full_name", nullable = false)
    private String fullName;

    // Optional contact number.
    @Column(name = "phone_number")
    private String phoneNumber;

    // Role for RBAC (USER, BUSINESS, ADMIN), stored as string.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // Timestamp when the record is created.
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Placeholder for future relationship mappings:
    // One-to-Many: User -> Reports
    // One-to-Many: User -> Notifications
    // One-to-One: User -> BusinessProfile
}
