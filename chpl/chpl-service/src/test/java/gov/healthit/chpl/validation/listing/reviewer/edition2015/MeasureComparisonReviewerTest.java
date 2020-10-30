package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasurementType;
import gov.healthit.chpl.permissions.ResourcePermissions;
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
        resourcePermissions = Mockito.mock(ResourcePermissions.class);

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.removedMeasure"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERROR_MSG, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        reviewer = new MeasureComparisonReviewer(resourcePermissions, msgUtil);
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

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .measure(ListingMeasure.builder()
                        .id(1L)
                        .measurementType(MeasurementType.builder()
                                .id(1L)
                                .name("G1")
                                .build())
                        .measure(Measure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .name("Measure Name")
                            .removed(true)
                            .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .measure(ListingMeasure.builder()
                        .id(1L)
                        .measurementType(MeasurementType.builder()
                                .id(1L)
                                .name("G1")
                                .build())
                        .measure(Measure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .name("Measure Name")
                            .removed(true)
                            .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(existingListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndG1MeasureAdded_MessagesExist() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .measure(ListingMeasure.builder()
                        .id(1L)
                        .measurementType(MeasurementType.builder()
                                .id(1L)
                                .name("G1")
                                .build())
                        .measure(Measure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .name("Measure Name")
                            .removed(true)
                            .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .measure(ListingMeasure.builder()
                        .id(1L)
                        .measurementType(MeasurementType.builder()
                                .id(1L)
                                .name("G1")
                                .build())
                        .measure(Measure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .name("Measure Name")
                            .removed(true)
                            .build())
                        .build())
                .measure(ListingMeasure.builder()
                        .id(2L)
                        .measurementType(MeasurementType.builder()
                                .id(1L)
                                .name("G1")
                                .build())
                        .measure(Measure.builder()
                            .id(GAP_EH_CAH)
                            .abbreviation("GAP-EH/CAH")
                            .name("Measure Name 2")
                            .removed(true)
                            .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(existingListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndG2MeasureAdded_MessagesExist() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .measure(ListingMeasure.builder()
                        .id(1L)
                        .measurementType(MeasurementType.builder()
                                .id(2L)
                                .name("G2")
                                .build())
                        .measure(Measure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .name("Measure Name")
                            .removed(true)
                            .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .measure(ListingMeasure.builder()
                        .id(1L)
                        .measurementType(MeasurementType.builder()
                                .id(2L)
                                .name("G2")
                                .build())
                        .measure(Measure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .name("Measure Name")
                            .removed(true)
                            .build())
                        .build())
                .measure(ListingMeasure.builder()
                        .id(2L)
                        .measurementType(MeasurementType.builder()
                                .id(2L)
                                .name("G2")
                                .build())
                        .measure(Measure.builder()
                            .id(GAP_EH_CAH)
                            .abbreviation("GAP-EH/CAH")
                            .name("Measure Name 2")
                            .removed(true)
                            .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(existingListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndBothG1AndG2MeasureAdded_MessagesExist() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .measure(ListingMeasure.builder()
                        .id(1L)
                        .measurementType(MeasurementType.builder()
                                .id(1L)
                                .name("G1")
                                .build())
                        .measure(Measure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .name("Measure Name")
                            .removed(true)
                            .build())
                        .build())
                .measure(ListingMeasure.builder()
                        .id(2L)
                        .measurementType(MeasurementType.builder()
                                .id(2L)
                                .name("G2")
                                .build())
                        .measure(Measure.builder()
                            .id(GAP_EH_CAH)
                            .abbreviation("GAP-EH/CAH")
                            .name("Measure Name 2")
                            .removed(true)
                            .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(existingListing, updatedListing);

        assertEquals(2, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndAddingNonRemovedMeasure_NoMessages() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .measure(ListingMeasure.builder()
                        .id(1L)
                        .measurementType(MeasurementType.builder()
                                .id(1L)
                                .name("G1")
                                .build())
                        .measure(Measure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .name("Measure Name")
                            .removed(false)
                            .build())
                        .build())
                .measure(ListingMeasure.builder()
                        .id(2L)
                        .measurementType(MeasurementType.builder()
                                .id(2L)
                                .name("G2")
                                .build())
                        .measure(Measure.builder()
                            .id(GAP_EH_CAH)
                            .abbreviation("GAP-EH/CAH")
                            .name("Measure Name 2")
                            .removed(false)
                            .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(existingListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }
}
