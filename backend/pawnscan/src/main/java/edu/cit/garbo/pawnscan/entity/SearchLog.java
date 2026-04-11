package edu.cit.garbo.pawnscan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_logs", indexes = {
        @Index(name = "idx_search_logs_business_user", columnList = "business_user_id"),
        @Index(name = "idx_search_logs_timestamp", columnList = "searched_at"),
        @Index(name = "idx_search_logs_serial", columnList = "searched_serial"),
        @Index(name = "idx_search_logs_result", columnList = "result")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "searched_serial", nullable = false, length = 255)
    private String searchedSerial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private VerificationResult result;

    @CreationTimestamp
    @Column(name = "searched_at", nullable = false, updatable = false)
    private LocalDateTime searchedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_search_logs_business_user"))
    private User businessUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_report_id", foreignKey = @ForeignKey(name = "fk_search_logs_matched_report"))
    private Report matchedReport;
}