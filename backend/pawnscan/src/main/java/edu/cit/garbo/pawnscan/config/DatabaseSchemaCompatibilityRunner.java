package edu.cit.garbo.pawnscan.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSchemaCompatibilityRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE reports ADD COLUMN IF NOT EXISTS status VARCHAR(32)");
            jdbcTemplate.execute("UPDATE reports SET status = 'APPROVED' WHERE status IS NULL");
            jdbcTemplate.execute("ALTER TABLE reports ALTER COLUMN status SET DEFAULT 'APPROVED'");
            jdbcTemplate.execute("ALTER TABLE reports ALTER COLUMN status SET NOT NULL");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_reports_status ON reports(status)");
            log.info("Database schema compatibility check completed for reports.status");
        } catch (Exception ex) {
            log.warn("Database schema compatibility check skipped or failed: {}", ex.getMessage());
        }
    }
}
