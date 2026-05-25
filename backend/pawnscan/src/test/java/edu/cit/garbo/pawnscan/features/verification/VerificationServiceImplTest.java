package edu.cit.garbo.pawnscan.features.verification;

import edu.cit.garbo.pawnscan.features.businessprofile.entity.BusinessProfile;
import edu.cit.garbo.pawnscan.features.businessprofile.repository.BusinessProfileRepository;
import edu.cit.garbo.pawnscan.features.notifications.NotificationService;
import edu.cit.garbo.pawnscan.features.reports.entity.Report;
import edu.cit.garbo.pawnscan.features.reports.entity.ReportStatus;
import edu.cit.garbo.pawnscan.features.reports.repository.ReportRepository;
import edu.cit.garbo.pawnscan.features.verification.entity.SearchLog;
import edu.cit.garbo.pawnscan.features.verification.entity.VerificationResult;
import edu.cit.garbo.pawnscan.features.verification.exception.InvalidVerificationRequestException;
import edu.cit.garbo.pawnscan.features.verification.publicapi.PublicStolenItemClient;
import edu.cit.garbo.pawnscan.features.verification.publicapi.PublicStolenItemMatch;
import edu.cit.garbo.pawnscan.features.verification.repository.SearchLogRepository;
import edu.cit.garbo.pawnscan.shared.user.User;
import edu.cit.garbo.pawnscan.shared.user.UserRepository;
import edu.cit.garbo.pawnscan.shared.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private SearchLogRepository searchLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PublicStolenItemClient publicStolenItemClient;

    @Mock
    private BusinessProfileRepository businessProfileRepository;

    private VerificationServiceImpl verificationService;
    private User businessUser;

    @BeforeEach
    void setUp() {
        verificationService = new VerificationServiceImpl(
                reportRepository,
                searchLogRepository,
                userRepository,
                notificationService,
                publicStolenItemClient,
                businessProfileRepository);

        businessUser = User.builder()
                .userId(7L)
                .email("business@test.com")
                .fullName("Verified Pawn Shop")
                .role(UserRole.BUSINESS)
                .build();

        when(userRepository.findByEmail("business@test.com")).thenReturn(Optional.of(businessUser));
        when(businessProfileRepository.findById(7L)).thenReturn(Optional.of(BusinessProfile.builder()
                .user(businessUser)
                .isVerified(true)
                .build()));
    }

    @Test
    void verifySerialAcceptsApprovedSerialWithEqualsSign() {
        String serial = "SN-EIR7343=343";
        User owner = User.builder()
                .userId(10L)
                .email("owner@test.com")
                .fullName("Item Owner")
                .role(UserRole.USER)
                .build();
        Report approvedReport = Report.builder()
                .id(21L)
                .serialNumber(serial)
                .itemModel("Laptop")
                .description("Approved stolen report")
                .status(ReportStatus.APPROVED)
                .user(owner)
                .build();

        when(reportRepository.findFirstBySerialNumberIgnoreCaseAndStatusWithUser(serial, ReportStatus.APPROVED))
                .thenReturn(Optional.of(approvedReport));
        when(publicStolenItemClient.searchBySerial(serial)).thenReturn(PublicStolenItemMatch.clean("bike_index"));
        when(searchLogRepository.save(any(SearchLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = verificationService.verifySerial("business@test.com", serial);

        assertThat(response.getStatus()).isEqualTo(VerificationResult.STOLEN);
        assertThat(response.getSerial()).isEqualTo(serial);
        assertThat(response.getReport().getSerialNumber()).isEqualTo(serial);
        verify(reportRepository).findFirstBySerialNumberIgnoreCaseAndStatusWithUser(serial, ReportStatus.APPROVED);
    }

    @Test
    void verifySerialRejectsInvalidCharacters() {
        assertThatThrownBy(() -> verificationService.verifySerial("business@test.com", "SN-123@456"))
                .isInstanceOf(InvalidVerificationRequestException.class)
                .hasMessage("Serial number can only contain letters, numbers, spaces, and - _ . / : # + =");

        verify(reportRepository, never()).findFirstBySerialNumberIgnoreCaseAndStatusWithUser(any(), eq(ReportStatus.APPROVED));
    }
}
