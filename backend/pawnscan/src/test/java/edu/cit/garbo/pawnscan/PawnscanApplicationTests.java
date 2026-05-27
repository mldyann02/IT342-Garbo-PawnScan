package edu.cit.garbo.pawnscan;

import edu.cit.garbo.pawnscan.features.businessprofile.repository.BusinessProfileRepository;
import edu.cit.garbo.pawnscan.features.notifications.repository.FcmDeviceTokenRepository;
import edu.cit.garbo.pawnscan.features.notifications.repository.NotificationRepository;
import edu.cit.garbo.pawnscan.features.reports.repository.ReportFileRepository;
import edu.cit.garbo.pawnscan.features.reports.repository.ReportRepository;
import edu.cit.garbo.pawnscan.features.verification.repository.SearchLogRepository;
import edu.cit.garbo.pawnscan.shared.email.OtpRepository;
import edu.cit.garbo.pawnscan.shared.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PawnscanApplicationTests {

	@MockBean
	private BusinessProfileRepository businessProfileRepository;

	@MockBean
	private NotificationRepository notificationRepository;

	@MockBean
	private FcmDeviceTokenRepository fcmDeviceTokenRepository;

	@MockBean
	private OtpRepository otpRepository;

	@MockBean
	private ReportFileRepository reportFileRepository;

	@MockBean
	private ReportRepository reportRepository;

	@MockBean
	private SearchLogRepository searchLogRepository;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

}
