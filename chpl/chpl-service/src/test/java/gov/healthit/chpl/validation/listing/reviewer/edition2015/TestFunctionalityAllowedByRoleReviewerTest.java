package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.FeatureList;
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

    private FF4j ff4j;
    private Environment env;
    private ErrorMessageUtil errorMessages;
    private ResourcePermissions permissions;
    private TestFunctionalityAllowedByRoleReviewer reviewer;

    @Before
    public void before() {
        // Setup some common mocks - these can be changed in each test if necessary
        ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)).thenReturn(true);

        Environment env = Mockito.mock(Environment.class);
        Mockito.when(env.getProperty("testFunctionalities.restrictions")).thenReturn(RESTRICTED_TEST_FUNCTIONALITY_JSON);

        ErrorMessageUtil errorMessages = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessages.getMessage(ArgumentMatchers.anyString())).thenReturn(ERROR_MESSAGE);

        permissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.any(List.class))).thenReturn(true);

        reviewer = new TestFunctionalityAllowedByRoleReviewer(ff4j, env, permissions, errorMessages);
    }

    @Test
    public void review_BeforeEffectiveRuleDate_NoErrorMessages() {
        // Setup
        Mockito.when(ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)).thenReturn(false);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();

        // Run
        reviewer.review(null, listing);

        // Check
        Assert.assertEquals(0, listing.getErrorMessages().size());;
    }

    @Test
    public void review_AfterRuleDateAndTestFunctionalityDidNotChange_NoErrorMessages() {
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
    public void review_AfterRuleDateAndUserIsAdminAndAddNonRestrictedTestFunctionality_NoErrorMessages() {
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
    public void review_AfterRuleDateAndUserIsAdminAndRemoveNonRestrictedTestFunctionality_NoErrorMessages() {
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
    public void review_AfterRuleDateAndUserIsAdminAndAddRestrictedTestFunctionality_NoErrorMessages() {
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
    public void review_AfterRuleDateAndUserIsAdminAndRemoveRestrictedTestFunctionality_NoErrorMessages() {
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
    public void review_AfterRuleDateAndUserIsAcbAndAddNonRestrictedTestFunctionality_NoErrorMessages() {
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
    public void review_AfterRuleDateAndUserIsAcbAndRemoveNonRestrictedTestFunctionality_NoErrorMessages() {
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
    public void review_AfterRuleDateAndUserIsAcbAndAddRestrictedTestFunctionality_ErrorMessageAdded() {
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
    public void review_AfterRuleDateAndUserIsAcbAndRemoveRestrictedTestFunctionality_ErrorMessageCreated() {
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
