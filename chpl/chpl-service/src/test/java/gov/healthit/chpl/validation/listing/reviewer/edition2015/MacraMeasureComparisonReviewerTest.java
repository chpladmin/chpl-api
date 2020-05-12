package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class MacraMeasureComparisonReviewerTest {

    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    private MacraMeasureComparisonReviewer reviewer;

    private static final long EDITION_2015_A_1 = 1L;
    private static final long GAP_EP_ID = 87L;
    private static final long GAP_EH_CAH = 88L;

    @Before
    public void before() {
        resourcePermissions = Mockito.mock(ResourcePermissions.class);

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(
                ArgumentMatchers.eq("listing.criteria.removedG1MacraMeasure"),
                ArgumentMatchers.any())).thenReturn("AnyMessage1");

        Mockito.when(msgUtil.getMessage(
                ArgumentMatchers.eq("listing.criteria.removedG2MacraMeasure"),
                ArgumentMatchers.any())).thenReturn("AnyMessage2");

        reviewer = new MacraMeasureComparisonReviewer(resourcePermissions, msgUtil);
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
    public void review_UserIsAcbAndNoMacraMeasuresAdded_NoMessages() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .g1MacraMeasure(MacraMeasure.builder()
                                .id(87L)
                                .abbreviation("GAP-EP")
                                .removed(true)
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .g1MacraMeasure(MacraMeasure.builder()
                                .id(87L)
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
    public void review_UserIsAcbAndG1MacraMeasureAdded_MessagesExist() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .g1MacraMeasure(MacraMeasure.builder()
                                .id(87L)
                                .abbreviation("GAP-EP")
                                .removed(true)
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .g1MacraMeasure(MacraMeasure.builder()
                                .id(87L)
                                .abbreviation("GAP-EP")
                                .removed(true)
                                .build())
                        .g1MacraMeasure(MacraMeasure.builder()
                                .id(88L)
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
    public void review_UserIsAcbAndG2MacraMeasureAdded_MessagesExist() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .g2MacraMeasure(MacraMeasure.builder()
                                .id(87L)
                                .abbreviation("GAP-EP")
                                .removed(true)
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .g2MacraMeasure(MacraMeasure.builder()
                                .id(87L)
                                .abbreviation("GAP-EP")
                                .removed(true)
                                .build())
                        .g2MacraMeasure(MacraMeasure.builder()
                                .id(88L)
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
    public void review_UserIsAcbAndBothG1AndG2MacraMeasureAdded_MessagesExist() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .g1MacraMeasure(MacraMeasure.builder()
                                .id(87L)
                                .abbreviation("GAP-EP")
                                .removed(true)
                                .build())
                        .g2MacraMeasure(MacraMeasure.builder()
                                .id(88L)
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
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_A_1)
                                .number("170.315 (a)(1)")
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(EDITION_2015_A_1)
                                .number("170.315 (a)(1)")
                                .build())
                        .g1MacraMeasure(MacraMeasure.builder()
                                .id(GAP_EP_ID)
                                .abbreviation("GAP-EP")
                                .removed(false)
                                .build())
                        .g2MacraMeasure(MacraMeasure.builder()
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
