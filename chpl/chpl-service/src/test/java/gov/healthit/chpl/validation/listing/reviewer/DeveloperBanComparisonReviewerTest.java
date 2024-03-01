package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.certifiedproduct.service.CertificationStatusEventsService;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperBanComparisonReviewerTest {
    private static final String ERROR_MESSAGE = "User %s does not have permission to modify certification status%s '%s' on the listing.";
    private CertificationStatusEventsService cseService;
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;
    private DeveloperBanComparisonReviewer reviewer;

    @Before
    public void before() {
        SecurityContextHolder.getContext().setAuthentication(getAcbUser());
        cseService = new CertificationStatusEventsService(
                Mockito.mock(CertificationStatusEventDAO.class),
                Mockito.mock(CertificationStatusDAO.class));
        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.certStatusChange.notAllowed"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERROR_MESSAGE, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        reviewer = new DeveloperBanComparisonReviewer(cseService, resourcePermissions, msgUtil);
    }

    @Test
    public void review_identicalCertificationStatusEvent_nonOncUser_noErrors() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);

        List<CertificationStatusEvent> events = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build()).toList();
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(events)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(events)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_identicalCertificationStatusEvent_oncUser_noErrors() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);

        List<CertificationStatusEvent> events = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build()).toList();
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(events)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(events)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_identicalCertificationStatusEvents_nonOncUser_noErrors() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);

        List<CertificationStatusEvent> events = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Suspended by ONC").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-30")))
                    .build()).toList();
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(events)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(events)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_identicalCertificationStatusEvents_oncUser_noErrors() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);

        List<CertificationStatusEvent> events = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Suspended by ONC").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-30")))
                    .build()).toList();
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(events)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(events)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_addingWithdrawnCertificationStatusEvent_nonOncUser_noErrors() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);

        List<CertificationStatusEvent> beforeEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build()).toList();
        List<CertificationStatusEvent> afterEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Withdrawn by Developer").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-02")))
                    .build()).toList();
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(beforeEvents)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(afterEvents)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_removingWithdrawnCertificationStatusEvent_nonOncUser_noErrors() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);

        List<CertificationStatusEvent> afterEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build()).toList();
        List<CertificationStatusEvent> beforeEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Withdrawn by Developer").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-02")))
                    .build()).toList();
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(beforeEvents)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(afterEvents)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_addingOncCertificationStatusEvent_nonOncUser_hasError() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);

        List<CertificationStatusEvent> beforeEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build()).toList();
        List<CertificationStatusEvent> afterEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Suspended by ONC").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-02")))
                    .build()).toList();
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(beforeEvents)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(afterEvents)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(1, updatedListing.getErrorMessages().size());
        assertEquals(updatedListing.getErrorMessages().iterator().next(),
                String.format(ERROR_MESSAGE, "unit@test.com", "", "Suspended by ONC"));
    }

    @Test
    public void review_addingOncCertificationStatusEvent_oncUser_noError() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);

        List<CertificationStatusEvent> beforeEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build()).toList();
        List<CertificationStatusEvent> afterEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Suspended by ONC").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-02")))
                    .build()).toList();
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(beforeEvents)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(afterEvents)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_removingOncCertificationStatusEvent_nonOncUser_hasError() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);

        List<CertificationStatusEvent> afterEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build()).toList();
        List<CertificationStatusEvent> beforeEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Suspended by ONC").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-02")))
                    .build()).toList();
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(beforeEvents)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(afterEvents)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(1, updatedListing.getErrorMessages().size());
        assertEquals(updatedListing.getErrorMessages().iterator().next(),
                String.format(ERROR_MESSAGE, "unit@test.com", "", "Suspended by ONC"));
    }

    @Test
    public void test_removingOncCertificationStatusEvent_oncUser_noError() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);

        List<CertificationStatusEvent> beforeEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build()).toList();
        List<CertificationStatusEvent> afterEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Suspended by ONC").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-02")))
                    .build()).toList();
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(beforeEvents)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(afterEvents)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_addingAndRemovingOncCertificationStatusEvent_nonOncUser_hasErrors() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);

        List<CertificationStatusEvent> beforeEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Terminated by ONC").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-03")))
                    .build()).toList();
        List<CertificationStatusEvent> afterEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Suspended by ONC").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-02")))
                    .build()).toList();
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(beforeEvents)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(afterEvents)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(1, updatedListing.getErrorMessages().size());
        assertEquals(updatedListing.getErrorMessages().iterator().next(),
                String.format(ERROR_MESSAGE, "unit@test.com", "es", "Suspended by ONC and Terminated by ONC"));
    }

    @Test
    public void review_addingAndRemovingOncCertificationStatusEvent_oncUser_noErrors() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);

        List<CertificationStatusEvent> beforeEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Terminated by ONC").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-03")))
                    .build()).toList();
        List<CertificationStatusEvent> afterEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Suspended by ONC").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-02")))
                    .build()).toList();
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(beforeEvents)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(afterEvents)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_modifyingOncCertificationStatusEventDate_nonOncUser_hasErrors() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);

        List<CertificationStatusEvent> beforeEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Terminated by ONC").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-03")))
                    .build()).toList();
        List<CertificationStatusEvent> afterEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Terminated by ONC").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-04")))
                    .build()).toList();
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(beforeEvents)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(afterEvents)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(1, updatedListing.getErrorMessages().size());
        assertEquals(updatedListing.getErrorMessages().iterator().next(),
                String.format(ERROR_MESSAGE, "unit@test.com", "", "Terminated by ONC"));
    }

    @Test
    public void review_modifyingOncCertificationStatusEventReason_nonOncUser_hasErrors() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);

        List<CertificationStatusEvent> beforeEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Terminated by ONC").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-03")))
                    .build()).toList();
        List<CertificationStatusEvent> afterEvents = Stream.of(
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Active").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-01")))
                    .build(),
                CertificationStatusEvent.builder()
                    .status(CertificationStatus.builder().name("Terminated by ONC").build())
                    .eventDate(DateUtil.toEpochMillis(LocalDate.parse("2024-01-03")))
                    .reason("Test!")
                    .build()).toList();
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(beforeEvents)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(afterEvents)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(1, updatedListing.getErrorMessages().size());
        assertEquals(updatedListing.getErrorMessages().iterator().next(),
                String.format(ERROR_MESSAGE, "unit@test.com", "", "Terminated by ONC"));
    }

    private JWTAuthenticatedUser getAcbUser() {
        JWTAuthenticatedUser acbUser = new JWTAuthenticatedUser();
        acbUser.setFullName("Test");
        acbUser.setId(3L);
        acbUser.setFriendlyName("User3");
        acbUser.setSubjectName("unit@test.com");
        acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));
        return acbUser;
    }
}
