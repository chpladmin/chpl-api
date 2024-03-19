package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.permissions.ChplResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class MeasureComparisonReviewerTest {
    private static final String ERROR_MSG = "The %s Measure %s for %s may not be referenced. The measure has been removed.";
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    private MeasureComparisonReviewer reviewer;

    private static final long GAP_EP_ID = 87L;
    private static final long GAP_EH_CAH = 88L;

    @Before
    public void before() {
        resourcePermissions = Mockito.mock(ChplResourcePermissions.class);

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.removedMeasure"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERROR_MSG, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        reviewer = new MeasureComparisonReviewer(resourcePermissionsFactory, msgUtil);
    }

    @Test
    public void review_UserIsAdmin_NoMessages() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails updatedListing = new CertifiedProductSearchDetails();

        reviewer.review(null, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsOnc_NoMessages() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);

        CertifiedProductSearchDetails updatedListing = new CertifiedProductSearchDetails();

        reviewer.review(null, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndNoMeasuresAdded_NoMessages() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);

        ListingMeasure measure = ListingMeasure.builder()
                .id(1L)
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .measure(Measure.builder()
                        .id(GAP_EP_ID)
                        .abbreviation("GAP-EP")
                        .name("Measure Name")
                        .removed(true)
                        .build())
                .build();

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .measures(Stream.of(measure).collect(Collectors.toList()))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .measures(Stream.of(measure).collect(Collectors.toList()))
                .build();

        reviewer.review(existingListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndG1MeasureAdded_MessagesExist() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        ListingMeasure measure1 = ListingMeasure.builder()
                .id(1L)
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .measure(Measure.builder()
                        .id(GAP_EP_ID)
                        .abbreviation("GAP-EP")
                        .name("Measure Name")
                        .removed(true)
                        .build())
                .build();

        ListingMeasure measure2 = ListingMeasure.builder()
                .id(2L)
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .measure(Measure.builder()
                        .id(GAP_EH_CAH)
                        .abbreviation("GAP-EH/CAH")
                        .name("Measure Name 2")
                        .removed(true)
                        .build())
                .build();

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .measures(Stream.of(measure1).collect(Collectors.toList()))
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .measures(Stream.of(measure1, measure2).collect(Collectors.toList()))
                .build();

        reviewer.review(existingListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndG2MeasureAdded_MessagesExist() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        ListingMeasure measure1 = ListingMeasure.builder()
                .id(1L)
                .measureType(MeasureType.builder()
                        .id(2L)
                        .name("G2")
                        .build())
                .measure(Measure.builder()
                        .id(GAP_EP_ID)
                        .abbreviation("GAP-EP")
                        .name("Measure Name")
                        .removed(true)
                        .build())
                .build();

        ListingMeasure measure2 = ListingMeasure.builder()
                .id(2L)
                .measureType(MeasureType.builder()
                        .id(2L)
                        .name("G2")
                        .build())
                .measure(Measure.builder()
                        .id(GAP_EH_CAH)
                        .abbreviation("GAP-EH/CAH")
                        .name("Measure Name 2")
                        .removed(true)
                        .build())
                .build();

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .measures(Stream.of(measure1).collect(Collectors.toList()))
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .measures(Stream.of(measure1, measure2).collect(Collectors.toList()))
                .build();

        reviewer.review(existingListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndBothG1AndG2MeasureAdded_MessagesExist() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        ListingMeasure measure1 = ListingMeasure.builder()
                .id(1L)
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .measure(Measure.builder()
                        .id(GAP_EP_ID)
                        .abbreviation("GAP-EP")
                        .name("Measure Name")
                        .removed(true)
                        .build())
                .build();
        ListingMeasure measure2 = ListingMeasure.builder()
                .id(2L)
                .measureType(MeasureType.builder()
                        .id(2L)
                        .name("G2")
                        .build())
                .measure(Measure.builder()
                        .id(GAP_EH_CAH)
                        .abbreviation("GAP-EH/CAH")
                        .name("Measure Name 2")
                        .removed(true)
                        .build())
                .build();

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .measures(Stream.of(measure1, measure2).collect(Collectors.toList()))
                .build();

        reviewer.review(existingListing, updatedListing);

        assertEquals(2, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndAddingNonRemovedMeasure_NoMessages() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        ListingMeasure measure1 = ListingMeasure.builder()
                .id(1L)
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .measure(Measure.builder()
                        .id(GAP_EP_ID)
                        .abbreviation("GAP-EP")
                        .name("Measure Name")
                        .removed(false)
                        .build())
                .build();

        ListingMeasure measure2 = ListingMeasure.builder()
                .id(2L)
                .measureType(MeasureType.builder()
                        .id(2L)
                        .name("G2")
                        .build())
                .measure(Measure.builder()
                        .id(GAP_EH_CAH)
                        .abbreviation("GAP-EH/CAH")
                        .name("Measure Name 2")
                        .removed(false)
                        .build())
                .build();

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .measures(Stream.of(measure1, measure2).collect(Collectors.toList()))
                .build();

        reviewer.review(existingListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }
}
