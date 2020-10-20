package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMipsMeasure;
import gov.healthit.chpl.domain.MipsMeasure;
import gov.healthit.chpl.domain.MipsMeasurementType;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class MipsMeasureComparisonReviewerTest {

    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    private MipsMeasureComparisonReviewer reviewer;

    private static final long GAP_EP_ID = 87L;
    private static final long GAP_EH_CAH = 88L;

    @Before
    public void before() {
        resourcePermissions = Mockito.mock(ResourcePermissions.class);

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(
                ArgumentMatchers.eq("listing.removedMipsMeasure"),
                ArgumentMatchers.anyString()))
        .thenAnswer(i -> i.getArguments()[1]);

        reviewer = new MipsMeasureComparisonReviewer(resourcePermissions, msgUtil);
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
    public void review_UserIsAcbAndNoMipsMeasuresAdded_NoMessages() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .mipsMeasure(ListingMipsMeasure.builder()
                        .id(1L)
                        .measurementType(MipsMeasurementType.builder()
                                .id(1L)
                                .name("G1")
                                .build())
                        .measure(MipsMeasure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .removed(true)
                            .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .mipsMeasure(ListingMipsMeasure.builder()
                        .id(1L)
                        .measurementType(MipsMeasurementType.builder()
                                .id(1L)
                                .name("G1")
                                .build())
                        .measure(MipsMeasure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .removed(true)
                            .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(existingListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndG1MipsMeasureAdded_MessagesExist() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .mipsMeasure(ListingMipsMeasure.builder()
                        .id(1L)
                        .measurementType(MipsMeasurementType.builder()
                                .id(1L)
                                .name("G1")
                                .build())
                        .measure(MipsMeasure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .removed(true)
                            .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .mipsMeasure(ListingMipsMeasure.builder()
                        .id(1L)
                        .measurementType(MipsMeasurementType.builder()
                                .id(1L)
                                .name("G1")
                                .build())
                        .measure(MipsMeasure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .removed(true)
                            .build())
                        .build())
                .mipsMeasure(ListingMipsMeasure.builder()
                        .id(2L)
                        .measurementType(MipsMeasurementType.builder()
                                .id(1L)
                                .name("G1")
                                .build())
                        .measure(MipsMeasure.builder()
                            .id(GAP_EH_CAH)
                            .abbreviation("GAP-EH/CAH")
                            .removed(true)
                            .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(existingListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndG2MipsMeasureAdded_MessagesExist() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .mipsMeasure(ListingMipsMeasure.builder()
                        .id(1L)
                        .measurementType(MipsMeasurementType.builder()
                                .id(2L)
                                .name("G2")
                                .build())
                        .measure(MipsMeasure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .removed(true)
                            .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .mipsMeasure(ListingMipsMeasure.builder()
                        .id(1L)
                        .measurementType(MipsMeasurementType.builder()
                                .id(2L)
                                .name("G2")
                                .build())
                        .measure(MipsMeasure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .removed(true)
                            .build())
                        .build())
                .mipsMeasure(ListingMipsMeasure.builder()
                        .id(2L)
                        .measurementType(MipsMeasurementType.builder()
                                .id(2L)
                                .name("G2")
                                .build())
                        .measure(MipsMeasure.builder()
                            .id(GAP_EH_CAH)
                            .abbreviation("GAP-EH/CAH")
                            .removed(true)
                            .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(existingListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndBothG1AndG2MipsMeasureAdded_MessagesExist() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .mipsMeasure(ListingMipsMeasure.builder()
                        .id(1L)
                        .measurementType(MipsMeasurementType.builder()
                                .id(1L)
                                .name("G1")
                                .build())
                        .measure(MipsMeasure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .removed(true)
                            .build())
                        .build())
                .mipsMeasure(ListingMipsMeasure.builder()
                        .id(2L)
                        .measurementType(MipsMeasurementType.builder()
                                .id(2L)
                                .name("G2")
                                .build())
                        .measure(MipsMeasure.builder()
                            .id(GAP_EH_CAH)
                            .abbreviation("GAP-EH/CAH")
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
                .mipsMeasure(ListingMipsMeasure.builder()
                        .id(1L)
                        .measurementType(MipsMeasurementType.builder()
                                .id(1L)
                                .name("G1")
                                .build())
                        .measure(MipsMeasure.builder()
                            .id(GAP_EP_ID)
                            .abbreviation("GAP-EP")
                            .removed(false)
                            .build())
                        .build())
                .mipsMeasure(ListingMipsMeasure.builder()
                        .id(2L)
                        .measurementType(MipsMeasurementType.builder()
                                .id(2L)
                                .name("G2")
                                .build())
                        .measure(MipsMeasure.builder()
                            .id(GAP_EH_CAH)
                            .abbreviation("GAP-EH/CAH")
                            .removed(false)
                            .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(existingListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }
}
