package gov.healthit.chpl.questionableactivity.listing;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;
import gov.healthit.chpl.util.DateUtil;

public class NonActiveCertificateEditedActivityTest {

    private NonActiveCertificateEdited activityChecker;

    @Before
    public void setup() {
        activityChecker = new NonActiveCertificateEdited();
    }

    @Test
    public void check_activeStatusUnchanged_noActivitiesReturned() {
        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(System.currentTimeMillis())
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();
        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(Stream.of(activeStatusEvent).collect(Collectors.toList()))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(Stream.of(activeStatusEvent).collect(Collectors.toList()))
                .build();
        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNull(activities);
    }

    @Test
    public void check_suspendedByAcbStatusUnchanged_noActivitiesReturned() {
        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(System.currentTimeMillis())
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.SuspendedByAcb.getName())
                        .build())
                .build();
        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(Stream.of(activeStatusEvent).collect(Collectors.toList()))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(Stream.of(activeStatusEvent).collect(Collectors.toList()))
                .build();
        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNull(activities);
    }

    @Test
    public void check_suspendedByOncStatusUnchanged_noActivitiesReturned() {
        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(System.currentTimeMillis())
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.SuspendedByOnc.getName())
                        .build())
                .build();
        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(Stream.of(activeStatusEvent).collect(Collectors.toList()))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(Stream.of(activeStatusEvent).collect(Collectors.toList()))
                .build();
        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNull(activities);
    }

    @Test
    public void check_activeStatusOriginalAndInactiveStatusUpdate_noActivitiesReturned() {
        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(System.currentTimeMillis())
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();

        CertificationStatusEvent inactiveStatusEvent = CertificationStatusEvent.builder()
                .eventDate(System.currentTimeMillis() + 1000)
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.WithdrawnByAcb.getName())
                        .build())
                .build();
        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(Stream.of(activeStatusEvent).collect(Collectors.toList()))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(Stream.of(activeStatusEvent, inactiveStatusEvent).collect(Collectors.toList()))
                .build();
        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNull(activities);
    }

    @Test
    public void check_inactiveStatusOriginal_activityReturned() {
        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        LocalDate yesterday = LocalDate.now().minusDays(1);

        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(DateUtil.toEpochMillis(twoDaysAgo))
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();

        CertificationStatusEvent inactiveStatusEvent = CertificationStatusEvent.builder()
                .eventDate(DateUtil.toEpochMillis(yesterday))
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.WithdrawnByAcb.getName())
                        .build())
                .build();
        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(Stream.of(activeStatusEvent, inactiveStatusEvent).collect(Collectors.toList()))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationEvents(Stream.of(activeStatusEvent, inactiveStatusEvent).collect(Collectors.toList()))
                .build();
        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNotNull(activities);
        assertEquals(1, activities.size());
    }
}
