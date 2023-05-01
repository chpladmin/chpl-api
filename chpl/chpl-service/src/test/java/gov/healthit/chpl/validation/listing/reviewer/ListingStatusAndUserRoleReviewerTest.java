package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertificationStatusProvider;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@SuppressWarnings("checkstyle:MethodName")
public class ListingStatusAndUserRoleReviewerTest {
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil messages;
    private ListingStatusAndUserRoleReviewer reviewer;

    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    private CertificationStatusProvider certificationStatusProvider = new CertificationStatusProvider();

    @Before
    public void before() {
        resourcePermissions = Mockito.mock(ResourcePermissions.class);

        messages = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(messages.getMessage("listing.criteria.userCannotAddOrRemove")).thenReturn("Targeted error message");

        reviewer = new ListingStatusAndUserRoleReviewer(resourcePermissions, messages);
    }

    @Test
    public void review_UserIsAdmin_NoErrorMessagesAdded() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = new CertifiedProductSearchDetails();
        CertifiedProductSearchDetails updatedListing = new CertifiedProductSearchDetails();

        reviewer.review(origListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsOnc_NoErrorMessagesAdded() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);

        CertifiedProductSearchDetails origListing = new CertifiedProductSearchDetails();
        CertifiedProductSearchDetails updatedListing = new CertifiedProductSearchDetails();

        reviewer.review(origListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingIsActiveAndCriteriaAdded_NoErrorMessages() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.ACTIVE))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.ACTIVE))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new LinkedHashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingIsActiveAndCriteriaRemoved_NoErrorMessages() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.ACTIVE))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.ACTIVE))
                .build();
        updatedListing.setErrorMessages(new LinkedHashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingIsSuspendedByAcbAndCriteriaAdded_NoErrorMessages() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.SUSPENDED_BY_ACB))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.SUSPENDED_BY_ACB))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new LinkedHashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingIsSuspendedByAcbAndCriteriaRemoved_NoErrorMessages() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.SUSPENDED_BY_ACB))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.SUSPENDED_BY_ACB))
                .build();
        updatedListing.setErrorMessages(new LinkedHashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingIsRetiredAndCriteriaAdded_ErrorMessageAdded() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.RETIRED))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.RETIRED))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new LinkedHashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingIsRetiredAndCriteriaDeleted_ErrorMessageAdded() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.RETIRED))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.RETIRED))
                .build();
        updatedListing.setErrorMessages(new LinkedHashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingIsWithdrawnByDeveloperAndCriteriaAdded_ErrorMessageAdded() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(
                        getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.WITHDRAWN_BY_DEVELOPER))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(
                        getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.WITHDRAWN_BY_DEVELOPER))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new LinkedHashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingIsWithdrawnByDeveloperAndCriteriaDeleted_ErrorMessageAdded() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(
                        getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.WITHDRAWN_BY_DEVELOPER))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(
                        getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.WITHDRAWN_BY_DEVELOPER))
                .build();
        updatedListing.setErrorMessages(new LinkedHashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingUpdatedStatusToRetiredAndCriteriaAdded_ErrorMessageAdded() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.ACTIVE))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.ACTIVE))
                .certificationEvent(getCertificationStatusEvent(1L, "02/01/2020", CertificationStatusProvider.RETIRED))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new LinkedHashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingUpdatedStatusToRetiredAndCriteriaDeleted_ErrorMessageAdded() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.ACTIVE))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.ACTIVE))
                .certificationEvent(getCertificationStatusEvent(1L, "02/01/2020", CertificationStatusProvider.RETIRED))
                .build();
        updatedListing.setErrorMessages(new LinkedHashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingUpdatedStatusFromRetiredToActiveAndCriteriaAdded_ErrorMessageAdded()
            throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.ACTIVE))
                .certificationEvent(getCertificationStatusEvent(1L, "02/01/2020", CertificationStatusProvider.RETIRED))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.ACTIVE))
                .certificationEvent(getCertificationStatusEvent(1L, "02/01/2020", CertificationStatusProvider.RETIRED))
                .certificationEvent(getCertificationStatusEvent(1L, "03/01/2020", CertificationStatusProvider.ACTIVE))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new LinkedHashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingUpdatedStatusFromRetiredToActiveAndCriteriaDeleted_ErrorMessageAdded()
            throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.ACTIVE))
                .certificationEvent(getCertificationStatusEvent(1L, "02/01/2020", CertificationStatusProvider.RETIRED))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.ACTIVE))
                .certificationEvent(getCertificationStatusEvent(1L, "02/01/2020", CertificationStatusProvider.RETIRED))
                .certificationEvent(getCertificationStatusEvent(1L, "03/01/2020", CertificationStatusProvider.ACTIVE))
                .build();
        updatedListing.setErrorMessages(new LinkedHashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());

    }

    @Test
    public void review_UserIsAcbAndListingIsUpdatedWithAChangedCriteria_ErrorMessageAdded() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.RETIRED))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", CertificationStatusProvider.RETIRED))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(2)")
                                .id(2L)
                                .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new LinkedHashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    private CertificationStatusEvent getCertificationStatusEvent(Long eventId, String date, Long statusId)
            throws ParseException {
        return CertificationStatusEvent.builder()
                .id(eventId)
                .eventDate(sdf.parse(date).getTime())
                .status(certificationStatusProvider.get(statusId))
                .build();
    }
}
