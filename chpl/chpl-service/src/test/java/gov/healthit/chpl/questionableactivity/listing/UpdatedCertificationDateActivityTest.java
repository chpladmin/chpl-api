package gov.healthit.chpl.questionableactivity.listing;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListing;

public class UpdatedCertificationDateActivityTest {

    private UpdatedCertificationDateActivity activityChecker;

    @Before
    public void setup() {
        activityChecker = new UpdatedCertificationDateActivity();
    }

    @Test
    public void check_noCertificationStatusEventChanges_noActivitiesReturned() {
        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1568592000000L)
                .id(18469L)
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();

        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .build();
        originalListing.setCertificationEvents(Stream.of(activeStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));
        updatedListing.setCertificationEvents(Stream.of(activeStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));

        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNull(activities);
    }

    @Test
    public void check_certificationEventAddedButCertificationDateUnchanged_noActivitiesReturned() {
        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1568592000000L)
                .id(18469L)
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();
        CertificationStatusEvent withdrawnStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1640930400000L)
                .id(25323L)
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.WithdrawnByDeveloper.getName())
                        .build())
                .build();

        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .build();
        originalListing.setCertificationEvents(Stream.of(activeStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));
        updatedListing.setCertificationEvents(Stream.of(activeStatusEvent, withdrawnStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));

        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNull(activities);
    }

    @Test
    public void check_certificationEventRemovedCertificationDateUnchanged_noActivitiesReturned() {
        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1568592000000L)
                .id(18469L)
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();
        CertificationStatusEvent withdrawnStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1640930400000L)
                .id(25323L)
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.WithdrawnByDeveloper.getName())
                        .build())
                .build();

        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .build();
        originalListing.setCertificationEvents(Stream.of(activeStatusEvent, withdrawnStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));
        updatedListing.setCertificationEvents(Stream.of(activeStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));

        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNull(activities);
    }

    @Test
    public void check_millisValueChangedButNotCalendarDay_noActivityReturned() {
        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1568577600000L)
                .id(18469L)
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();
        CertificationStatusEvent updatedActiveStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1568577480000L)
                .id(25323L)
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();

        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .build();
        originalListing.setCertificationEvents(Stream.of(activeStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));
        updatedListing.setCertificationEvents(Stream.of(updatedActiveStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));

        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNull(activities);
    }

    @Test
    public void check_noCertificationEventsAddedButCertificationDateChanged_returnsActivity() {
        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1568577600000L)
                .id(18469L)
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();
        CertificationStatusEvent updatedActiveStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1558592000000L)
                .id(25323L)
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();

        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .build();
        originalListing.setCertificationEvents(Stream.of(activeStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));
        updatedListing.setCertificationEvents(Stream.of(updatedActiveStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));

        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        assertEquals("2019-09-15", activities.get(0).getBefore());
        assertEquals("2019-05-23", activities.get(0).getAfter());
    }

    @Test
    public void check_certificationEventAddedAndCertificationDateChanged_returnsActivity() {
        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1568577600000L)
                .id(18469L)
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();
        CertificationStatusEvent updatedActiveStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1558592000000L)
                .id(25324L)
                .reason("test1")
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();
        CertificationStatusEvent withdrawnStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1640930400000L)
                .id(25323L)
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.WithdrawnByDeveloper.getName())
                        .build())
                .build();

        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .build();
        originalListing.setCertificationEvents(Stream.of(activeStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));
        updatedListing.setCertificationEvents(Stream.of(updatedActiveStatusEvent, withdrawnStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));

        List<QuestionableActivityListing> activities = activityChecker.check(originalListing, updatedListing);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        assertEquals("2019-09-15", activities.get(0).getBefore());
        assertEquals("2019-05-23", activities.get(0).getAfter());
        assertEquals("test1", activities.get(0).getCertificationStatusChangeReason());
    }
}
