package gov.healthit.chpl.questionableactivity.listing;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;

public class UpdatedCertificationStatusHistoryActivityTest {

    private UpdatedCertificationStatusHistoryActivity activityChecker;

    @Before
    public void setup() {
        activityChecker = new UpdatedCertificationStatusHistoryActivity();
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

        List<QuestionableActivityListingDTO> activities = activityChecker.check(originalListing, updatedListing);
        assertNull(activities);
    }

    @Test
    public void check_activeListingIsWithdrawn_noActivitiesReturned() {
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
                        .id(2L)
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

        List<QuestionableActivityListingDTO> activities = activityChecker.check(originalListing, updatedListing);
        assertNull(activities);
    }

    @Test
    public void check_withdrawnListingNoChanges_noActivitiesReturned() {
        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1592870400000L)
                .id(18469L)
                .status(CertificationStatus.builder()
                        .id(1L)
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();
        CertificationStatusEvent withdrawnStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1671080400000L)
                .id(25323L)
                .status(CertificationStatus.builder()
                        .id(2L)
                        .name(CertificationStatusType.WithdrawnByDeveloper.getName())
                        .build())
                .build();

        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .build();
        originalListing.setCertificationEvents(Stream.of(activeStatusEvent, withdrawnStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));
        updatedListing.setCertificationEvents(Stream.of(activeStatusEvent, withdrawnStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));

        List<QuestionableActivityListingDTO> activities = activityChecker.check(originalListing, updatedListing);
        assertNull(activities);
    }

    @Test
    public void check_withdrawnListingisRetired_noActivitiesReturned() {
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
                        .id(2L)
                        .name(CertificationStatusType.WithdrawnByDeveloper.getName())
                        .build())
                .build();
        CertificationStatusEvent retiredStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1641930400000L)
                .id(25340L)
                .status(CertificationStatus.builder()
                        .id(3L)
                        .name(CertificationStatusType.Retired.getName())
                        .build())
                .build();

        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .build();
        originalListing.setCertificationEvents(Stream.of(activeStatusEvent, withdrawnStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));
        updatedListing.setCertificationEvents(Stream.of(activeStatusEvent, withdrawnStatusEvent, retiredStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));

        List<QuestionableActivityListingDTO> activities = activityChecker.check(originalListing, updatedListing);
        assertNull(activities);
    }

    @Test
    public void check_activeListingCertificationDateChanged_noActivitiesReturned() {
        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1568592000000L)
                .id(18469L)
                .status(CertificationStatus.builder()
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();
        CertificationStatusEvent updatedActiveStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1640930400000L)
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

        List<QuestionableActivityListingDTO> activities = activityChecker.check(originalListing, updatedListing);
        assertNull(activities);
    }

    @Test
    public void check_addsStatusEventBetweenTwoExistingStatusEvents_activityReturned() {
        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1568592000000L)
                .id(18469L)
                .status(CertificationStatus.builder()
                        .id(1L)
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();

        CertificationStatusEvent retiredStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1641030400000L)
                .id(25324L)
                .status(CertificationStatus.builder()
                        .id(3L)
                        .name(CertificationStatusType.Retired.getName())
                        .build())
                .build();

        CertificationStatusEvent withdrawnStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1640930400000L)
                .id(25323L)
                .status(CertificationStatus.builder()
                        .id(2L)
                        .name(CertificationStatusType.WithdrawnByDeveloper.getName())
                        .build())
                .build();


        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .build();
        originalListing.setCertificationEvents(Stream.of(activeStatusEvent, retiredStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));
        updatedListing.setCertificationEvents(Stream.of(activeStatusEvent, retiredStatusEvent, withdrawnStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));

        List<QuestionableActivityListingDTO> activities = activityChecker.check(originalListing, updatedListing);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        assertNull(activities.get(0).getBefore());
        assertEquals("Withdrawn by Developer (2021-12-31)", activities.get(0).getAfter());
    }

    @Test
    public void check_existingHistoryWithdrawnDateChanges_activityReturned() {
        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1568577600000L)
                .id(18469L)
                .status(CertificationStatus.builder()
                        .id(1L)
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();

        CertificationStatusEvent withdrawnStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1640930400000L)
                .id(25323L)
                .status(CertificationStatus.builder()
                        .id(2L)
                        .name(CertificationStatusType.WithdrawnByDeveloper.getName())
                        .build())
                .build();

        CertificationStatusEvent updatedWithdrawnStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1640816000000L)
                .id(25324L)
                .status(CertificationStatus.builder()
                        .id(2L)
                        .name(CertificationStatusType.WithdrawnByDeveloper.getName())
                        .build())
                .build();

        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .build();
        originalListing.setCertificationEvents(Stream.of(activeStatusEvent, withdrawnStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));
        updatedListing.setCertificationEvents(Stream.of(activeStatusEvent, updatedWithdrawnStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));

        List<QuestionableActivityListingDTO> activities = activityChecker.check(originalListing, updatedListing);
        assertNotNull(activities);
        assertEquals(2, activities.size());
        assertEquals(CertificationStatusType.WithdrawnByDeveloper.getName() + " (2021-12-31)", activities.get(0).getBefore());
        assertNull(activities.get(0).getAfter());
        assertNull(activities.get(1).getBefore());
        assertEquals(CertificationStatusType.WithdrawnByDeveloper.getName() + " (2021-12-29)", activities.get(1).getAfter());
    }

    @Test
    public void check_historyAddedForWithdrawnThenRetired_activityReturned() {
        CertificationStatusEvent activeStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1568577600000L)
                .id(18469L)
                .status(CertificationStatus.builder()
                        .id(1L)
                        .name(CertificationStatusType.Active.getName())
                        .build())
                .build();

        CertificationStatusEvent withdrawnStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1640816000000L)
                .id(25323L)
                .status(CertificationStatus.builder()
                        .id(2L)
                        .name(CertificationStatusType.WithdrawnByDeveloper.getName())
                        .build())
                .build();

        CertificationStatusEvent retiredStatusEvent = CertificationStatusEvent.builder()
                .eventDate(1640930400000L)
                .id(25324L)
                .status(CertificationStatus.builder()
                        .id(3L)
                        .name(CertificationStatusType.Retired.getName())
                        .build())
                .build();

        CertifiedProductSearchDetails originalListing = CertifiedProductSearchDetails.builder()
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .build();
        originalListing.setCertificationEvents(Stream.of(activeStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));
        updatedListing.setCertificationEvents(Stream.of(activeStatusEvent, withdrawnStatusEvent, retiredStatusEvent).collect(
                        Collectors.toCollection(ArrayList<CertificationStatusEvent>::new)));

        List<QuestionableActivityListingDTO> activities = activityChecker.check(originalListing, updatedListing);
        assertNotNull(activities);
        assertEquals(2, activities.size());
        assertNull(activities.get(0).getBefore());
        assertEquals(CertificationStatusType.WithdrawnByDeveloper.getName() + " (2021-12-29)", activities.get(0).getAfter());
        assertNull(activities.get(1).getBefore());
        assertEquals(CertificationStatusType.Retired.getName() + " (2021-12-31)", activities.get(1).getAfter());
    }
}
