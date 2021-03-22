package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestFunctionalityAllowedByRoleReviewerTest {

    private static final String RESTRICTED_TEST_FUNCTIONALITY_JSON = "[{\"criteriaId\":27, \"restrictedTestFunctionalities\": [{\"testFunctionalityId\":56, \"allowedRoleNames\":[\"ROLE_ADMIN\",\"ROLE_ONC\"]}]}]";
    private static final String ERROR_MESSAGE = "Current user does not have permission to add/remove test functionality '%s' for Criteria '%s'.";
    private static final Long CERTIFICATION_RESULT_ID = 5L;
    private static final Long CERTIFICATION_EDITION_ID = 4L;
    private static final Long TEST_FUNCTIONALITY_ID_RANDOM = 11L;
    private static final Long CERTIFICATION_CRITERION_B2 = 17L;
    private static final Long CERTIFICATION_CRITERION_C3 = 27L;
    private static final Long TEST_FUNCTIONALITY_CIII = 56L;
    private ResourcePermissions permissions;
    private TestFunctionalityAllowedByRoleReviewer reviewer;

    @Before
    public void before() {
        // Setup some common mocks - these can be changed in each test if necessary
                ErrorMessageUtil errorMessages = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessages.getMessage(ArgumentMatchers.anyString())).thenReturn(ERROR_MESSAGE);

        permissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.anyList())).thenReturn(true);

        reviewer = new TestFunctionalityAllowedByRoleReviewer(permissions, errorMessages, RESTRICTED_TEST_FUNCTIONALITY_JSON);
    }

    @Test
    public void review_TestFunctionalityDidNotChange_NoErrorMessages() {
        // Setup
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_B2)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (b)(2)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(b)(2)(i)(E)")
                                .testFunctionalityId(TEST_FUNCTIONALITY_ID_RANDOM)
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_B2)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (b)(2)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(b)(2)(i)(E)")
                                .testFunctionalityId(TEST_FUNCTIONALITY_ID_RANDOM)
                                .build())
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAdminAndAddNonRestrictedTestFunctionality_NoErrorMessages() {
        // Setup
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder().build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_B2)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (b)(2)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(b)(2)(i)(E)")
                                .testFunctionalityId(TEST_FUNCTIONALITY_ID_RANDOM)
                                .build())
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAdminAndRemoveNonRestrictedTestFunctionality_NoErrorMessages() {
        // Setup
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_B2)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (b)(2)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(b)(2)(i)(E)")
                                .testFunctionalityId(TEST_FUNCTIONALITY_ID_RANDOM)
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder().build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAdminAndAddRestrictedTestFunctionality_NoErrorMessages() {
        // Setup
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_C3)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (c)(3)")
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_C3)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (c)(3)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(c)(3)(ii)")
                                .testFunctionalityId(TEST_FUNCTIONALITY_CIII)
                                .build())
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAdminAndRemoveRestrictedTestFunctionality_NoErrorMessages() {
        // Setup
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_C3)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (c)(3)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(c)(3)(ii)")
                                .testFunctionalityId(TEST_FUNCTIONALITY_CIII)
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_C3)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (c)(3)")
                                .build())
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndAddNonRestrictedTestFunctionality_NoErrorMessages() {
        // Setup
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.anyList())).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder().build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_B2)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (b)(2)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(b)(2)(i)(E)")
                                .testFunctionalityId(TEST_FUNCTIONALITY_ID_RANDOM)
                                .build())
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndRemoveNonRestrictedTestFunctionality_NoErrorMessages() {
        // Setup
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.anyList())).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_B2)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (b)(2)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(b)(2)(i)(E)")
                                .testFunctionalityId(TEST_FUNCTIONALITY_ID_RANDOM)
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder().build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndAddRestrictedTestFunctionality_ErrorMessageAdded() {
        // Setup
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.anyList())).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_C3)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (c)(3)")
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_C3)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (c)(3)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(c)(3)(ii)")
                                .testFunctionalityId(TEST_FUNCTIONALITY_CIII)
                                .build())
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndRemoveRestrictedTestFunctionality_ErrorMessageCreated() {
        // Setup
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.anyList())).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_C3)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (c)(3)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(c)(3)(ii)")
                                .testFunctionalityId(TEST_FUNCTIONALITY_CIII)
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_C3)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (c)(3)")
                                .build())
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndRemoveRestrictedTestFunctionalityAndUpdateCriteriaIsAttestedTo_ErrorMessageCreated() {
        // Setup
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.anyList())).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_C3)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (c)(3)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(c)(3)(ii)")
                                .testFunctionalityId(TEST_FUNCTIONALITY_CIII)
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_C3)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (c)(3)")
                                .build())
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        //This should succeed
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

}
