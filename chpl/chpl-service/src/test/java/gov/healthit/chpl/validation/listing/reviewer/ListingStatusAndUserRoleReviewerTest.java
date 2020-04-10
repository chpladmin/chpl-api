package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@SuppressWarnings("checkstyle:MethodName")
public class ListingStatusAndUserRoleReviewerTest {
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil messages;
    private ListingStatusAndUserRoleReviewer reviewer;

    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

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
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 1L, "Active"))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 1L, "Active"))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingIsActiveAndCriteriaRemoved_NoErrorMessages() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 1L, "Active"))
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
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 1L, "Active"))
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingIsSuspendedByAcbAndCriteriaAdded_NoErrorMessages() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 6L, "Suspended by ONC-ACB"))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 6L, "Suspended by ONC-ACB"))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingIsSuspendedByAcbAndCriteriaRemoved_NoErrorMessages() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 6L, "Suspended by ONC-ACB"))
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
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 6L, "Suspended by ONC-ACB"))
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingIsRetiredAndCriteriaAdded_ErrorMessageAdded() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 2L, "Retired"))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 2L, "Retired"))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingIsRetiredAndCriteriaDeleted_ErrorMessageAdded() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 2L, "Retired"))
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
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 2L, "Retired"))
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingIsWithdrawnByDeveloperAndCriteriaAdded_ErrorMessageAdded() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 3L, "Withdrawn by Developer"))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 3L, "Withdrawn by Developer"))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingIsWithdrawnByDeveloperAndCriteriaDeleted_ErrorMessageAdded() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 3L, "Withdrawn by Developer"))
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
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 3L, "Withdrawn by Developer"))
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingUpdatedStatusToRetiredAndCriteriaAdded_ErrorMessageAdded() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 1L, "Active"))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 1L, "Active"))
                .certificationEvent(getCertificationStatusEvent(1L, "02/01/2020", 2L, "Retired"))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndListingUpdatedStatusToRetiredAndCriteriaDeleted_ErrorMessageAdded() throws ParseException {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);

        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 1L, "Active"))
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
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 1L, "Active"))
                .certificationEvent(getCertificationStatusEvent(1L, "02/01/2020", 2L, "Retired"))
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

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
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 1L, "Active"))
                .certificationEvent(getCertificationStatusEvent(1L, "02/01/2020", 2L, "Retired"))
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 1L, "Active"))
                .certificationEvent(getCertificationStatusEvent(1L, "02/01/2020", 2L, "Retired"))
                .certificationEvent(getCertificationStatusEvent(1L, "03/01/2020", 1L, "Active"))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(1)")
                                .id(1L)
                                .build())
                        .build())
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

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
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 1L, "Active"))
                .certificationEvent(getCertificationStatusEvent(1L, "02/01/2020", 2L, "Retired"))
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
                .certificationEvent(getCertificationStatusEvent(1L, "01/01/2020", 1L, "Active"))
                .certificationEvent(getCertificationStatusEvent(1L, "02/01/2020", 2L, "Retired"))
                .certificationEvent(getCertificationStatusEvent(1L, "03/01/2020", 1L, "Active"))
                .build();
        updatedListing.setErrorMessages(new HashSet<String>());

        reviewer.review(origListing, updatedListing);

        assertEquals(1, updatedListing.getErrorMessages().size());

    }

    private CertificationStatusEvent getCertificationStatusEvent(Long eventId, String date, Long statusId, String statusName)
            throws ParseException {
        return CertificationStatusEvent.builder()
                .id(eventId)
                .eventDate(sdf.parse(date).getTime())
                .status(CertificationStatus.builder()
                        .id(statusId)
                        .name(statusName)
                        .build())
                .build();
    }
}
