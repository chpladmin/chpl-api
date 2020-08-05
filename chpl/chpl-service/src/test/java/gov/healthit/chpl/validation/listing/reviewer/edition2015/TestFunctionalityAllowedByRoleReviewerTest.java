package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import junit.framework.Assert;

public class TestFunctionalityAllowedByRoleReviewerTest {

    private static final String RESTRICTED_TEST_FUNCTIONALITY_JSON = "[{\"criteriaId\":27, \"restrictedTestFunctionalities\": [{\"testFunctionalityId\":56, \"allowedRoleNames\":[\"ROLE_ADMIN\",\"ROLE_ONC\"]}]}]";
    private static final String ERROR_MESSAGE = "Current user does not have permission to add/remove test functionality '%s' for Criteria '%s'.";

    private ResourcePermissions permissions;
    private TestFunctionalityAllowedByRoleReviewer reviewer;

    @Before
    public void before() {
        // Setup some common mocks - these can be changed in each test if necessary
        Environment env = Mockito.mock(Environment.class);
        Mockito.when(env.getProperty("testFunctionalities.restrictions")).thenReturn(RESTRICTED_TEST_FUNCTIONALITY_JSON);

        ErrorMessageUtil errorMessages = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessages.getMessage(ArgumentMatchers.anyString())).thenReturn(ERROR_MESSAGE);

        permissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.any(List.class))).thenReturn(true);

        reviewer = new TestFunctionalityAllowedByRoleReviewer(env, permissions, errorMessages);
    }

    @Test
    public void review_TestFunctionalityDidNotChange_NoErrorMessages() {
        // Setup
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(5L)
                        .criterion(CertificationCriterion.builder()
                                .id(17l)
                                .certificationEdition("2015")
                                .certificationEditionId(3L)
                                .number("170.315 (b)(2)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(b)(2)(i)(E)")
                                .testFunctionalityId(11l)
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(5L)
                        .criterion(CertificationCriterion.builder()
                                .id(17l)
                                .certificationEdition("2015")
                                .certificationEditionId(3L)
                                .number("170.315 (b)(2)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(b)(2)(i)(E)")
                                .testFunctionalityId(11l)
                                .build())
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        Assert.assertEquals(0, updatedListing.getErrorMessages().size());;
    }

    @Test
    public void review_UserIsAdminAndAddNonRestrictedTestFunctionality_NoErrorMessages() {
        // Setup
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder().build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(5L)
                        .criterion(CertificationCriterion.builder()
                                .id(17l)
                                .certificationEdition("2015")
                                .certificationEditionId(3L)
                                .number("170.315 (b)(2)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(b)(2)(i)(E)")
                                .testFunctionalityId(11l)
                                .build())
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        Assert.assertEquals(0, updatedListing.getErrorMessages().size());;
    }

    @Test
    public void review_UserIsAdminAndRemoveNonRestrictedTestFunctionality_NoErrorMessages() {
        // Setup
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(5L)
                        .criterion(CertificationCriterion.builder()
                                .id(17l)
                                .certificationEdition("2015")
                                .certificationEditionId(3L)
                                .number("170.315 (b)(2)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(b)(2)(i)(E)")
                                .testFunctionalityId(11l)
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder().build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        Assert.assertEquals(0, updatedListing.getErrorMessages().size());;
    }

    @Test
    public void review_UserIsAdminAndAddRestrictedTestFunctionality_NoErrorMessages() {
        // Setup
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(5L)
                        .criterion(CertificationCriterion.builder()
                                .id(27l)
                                .certificationEdition("2015")
                                .certificationEditionId(3L)
                                .number("170.315 (c)(3)")
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(5L)
                        .criterion(CertificationCriterion.builder()
                                .id(27l)
                                .certificationEdition("2015")
                                .certificationEditionId(3L)
                                .number("170.315 (c)(3)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(c)(3)(ii)")
                                .testFunctionalityId(56l)
                                .build())
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        Assert.assertEquals(0, updatedListing.getErrorMessages().size());;
    }

    @Test
    public void review_UserIsAdminAndRemoveRestrictedTestFunctionality_NoErrorMessages() {
        // Setup
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(5L)
                        .criterion(CertificationCriterion.builder()
                                .id(27l)
                                .certificationEdition("2015")
                                .certificationEditionId(3L)
                                .number("170.315 (c)(3)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(c)(3)(ii)")
                                .testFunctionalityId(56l)
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(5L)
                        .criterion(CertificationCriterion.builder()
                                .id(27l)
                                .certificationEdition("2015")
                                .certificationEditionId(3L)
                                .number("170.315 (c)(3)")
                                .build())
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        Assert.assertEquals(0, updatedListing.getErrorMessages().size());;
    }

    @Test
    public void review_UserIsAcbAndAddNonRestrictedTestFunctionality_NoErrorMessages() {
        // Setup
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.any(List.class))).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder().build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(5L)
                        .criterion(CertificationCriterion.builder()
                                .id(17l)
                                .certificationEdition("2015")
                                .certificationEditionId(3L)
                                .number("170.315 (b)(2)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(b)(2)(i)(E)")
                                .testFunctionalityId(11l)
                                .build())
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        Assert.assertEquals(0, updatedListing.getErrorMessages().size());;
    }

    @Test
    public void review_UserIsAcbAndRemoveNonRestrictedTestFunctionality_NoErrorMessages() {
        // Setup
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.any(List.class))).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(5L)
                        .criterion(CertificationCriterion.builder()
                                .id(17l)
                                .certificationEdition("2015")
                                .certificationEditionId(3L)
                                .number("170.315 (b)(2)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(b)(2)(i)(E)")
                                .testFunctionalityId(11l)
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder().build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        Assert.assertEquals(0, updatedListing.getErrorMessages().size());;
    }

    @Test
    public void review_UserIsAcbAndAddRestrictedTestFunctionality_ErrorMessageAdded() {
        // Setup
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.any(List.class))).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(5L)
                        .criterion(CertificationCriterion.builder()
                                .id(27l)
                                .certificationEdition("2015")
                                .certificationEditionId(3L)
                                .number("170.315 (c)(3)")
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(5L)
                        .criterion(CertificationCriterion.builder()
                                .id(27l)
                                .certificationEdition("2015")
                                .certificationEditionId(3L)
                                .number("170.315 (c)(3)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(c)(3)(ii)")
                                .testFunctionalityId(56l)
                                .build())
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        Assert.assertEquals(1, updatedListing.getErrorMessages().size());;
    }

    @Test
    public void review_UserIsAcbAndRemoveRestrictedTestFunctionality_ErrorMessageCreated() {
        // Setup
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.any(List.class))).thenReturn(false);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(5L)
                        .criterion(CertificationCriterion.builder()
                                .id(27l)
                                .certificationEdition("2015")
                                .certificationEditionId(3L)
                                .number("170.315 (c)(3)")
                                .build())
                        .testFunctionalitySingle(CertificationResultTestFunctionality.builder()
                                .name("(c)(3)(ii)")
                                .testFunctionalityId(56l)
                                .build())
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(5L)
                        .criterion(CertificationCriterion.builder()
                                .id(27l)
                                .certificationEdition("2015")
                                .certificationEditionId(3L)
                                .number("170.315 (c)(3)")
                                .build())
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        Assert.assertEquals(1, updatedListing.getErrorMessages().size());;
    }

}
