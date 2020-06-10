package gov.healthit.chpl.validation.pendinglisting.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.TestFunctionalityAllowedByRoleReviewer;

public class TestFunctionalityAllowedByRoleReviewerTest {

    private static final String RESTRICTED_TEST_FUNCTIONALITY_JSON = "[{\"criteriaId\":27, \"restrictedTestFunctionalities\": "
            + "[{\"testFunctionalityId\":56, \"allowedRoleNames\":[\"ROLE_ADMIN\",\"ROLE_ONC\"]}]}]";
    private static final Long CRITERIA_ID_WITH_RESTRICTIONS = 27L;
    private static final Long CRITERIA_ID_WITHOUT_RESTRICTIONS = 13L;
    private static final Long TEST_FUNCTIONALITY_ID_WITH_RESTRICTIONS = 56L;
    private static final Long TEST_FUNCTIONALITY_ID_WITHOUT_RESTRICTIONS = 52L;

    private FF4j ff4j;
    private Environment env;
    private ResourcePermissions resourcePermissions;
    private TestFunctionalityAllowedByRoleReviewer reviewer;

    @Before
    public void before() {
        // Setup some common mocks - these can be changed in each test if necessary
        ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)).thenReturn(true);

        env = Mockito.mock(Environment.class);
        Mockito.when(env.getProperty("testFunctionalities.restrictions")).thenReturn(RESTRICTED_TEST_FUNCTIONALITY_JSON);

        resourcePermissions = Mockito.mock(ResourcePermissions.class);

        reviewer = new TestFunctionalityAllowedByRoleReviewer(ff4j, env, resourcePermissions);
        reviewer.setup();
    }

    @Test
    public void review_BeforeRuleDateAndRestrictedTestFunctionality_TestFunctionalityNotRemoved() {
        // Setup
        Mockito.when(ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)).thenReturn(false);

        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(CRITERIA_ID_WITH_RESTRICTIONS)
                                .number("170.315 (c)(3)")
                                .build())
                        .testFunctionalitySingle(PendingCertificationResultTestFunctionalityDTO.builder()
                                .testFunctionalityId(TEST_FUNCTIONALITY_ID_WITH_RESTRICTIONS)
                                .number("(c)(3)(ii)")
                                .build())
                        .build())
                .build();
        // This makes the test func collection a mutable collection - we are removing TFs from the collection
        listing.getCertificationCriterion().get(0).setTestFunctionality(
                new ArrayList<PendingCertificationResultTestFunctionalityDTO>(
                        listing.getCertificationCriterion().get(0).getTestFunctionality()));

        // Run
        reviewer.review(listing);

        // Check
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_AfterRuleDateAndNoRestrictedTestFunctionality_TestFunctionalityNotRemoved() {
        // Setup
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(CRITERIA_ID_WITHOUT_RESTRICTIONS)
                                .number("170.315 (a)(13)")
                                .build())
                        .testFunctionalitySingle(PendingCertificationResultTestFunctionalityDTO.builder()
                                .testFunctionalityId(TEST_FUNCTIONALITY_ID_WITHOUT_RESTRICTIONS)
                                .number("(a)(13)(ii)")
                                .build())
                        .build())
                .build();
        // This makes the test func collection a mutable collection - we are removing TFs from the collection
        listing.getCertificationCriterion().get(0).setTestFunctionality(
                new ArrayList<PendingCertificationResultTestFunctionalityDTO>(
                        listing.getCertificationCriterion().get(0).getTestFunctionality()));

        // Run
        reviewer.review(listing);

        // Check
        assertEquals(1, listing.getCertificationCriterion().get(0).getTestFunctionality().size());
    }

    @Test
    public void review_AfterRuleDateAndRestrictedTestFunctionalityAndUserHasValidRole_TestFunctionalityExists() {
        // Setup
        Mockito.when(resourcePermissions.doesUserHaveRole(ArgumentMatchers.anyList()))
        .thenReturn(true);

        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(CRITERIA_ID_WITH_RESTRICTIONS)
                                .number("170.315 (c)(3)")
                                .build())
                        .testFunctionalitySingle(PendingCertificationResultTestFunctionalityDTO.builder()
                                .testFunctionalityId(TEST_FUNCTIONALITY_ID_WITH_RESTRICTIONS)
                                .number("(c)(3)(ii)")
                                .build())
                        .build())
                .build();
        // This makes the test func collection a mutable collection - we are removing TFs from the collection
        listing.getCertificationCriterion().get(0).setTestFunctionality(
                new ArrayList<PendingCertificationResultTestFunctionalityDTO>(
                        listing.getCertificationCriterion().get(0).getTestFunctionality()));

        // Run
        reviewer.review(listing);

        // Check
        assertEquals(1, listing.getCertificationCriterion().get(0).getTestFunctionality().size());
    }

    @Test
    public void review_AfterRuleDateAndRestrictedTestFunctionalityAndUserDoesNotHaveAValidRole_TestFunctionalityRemoved() {
        // Setup
        Mockito.when(resourcePermissions.doesUserHaveRole(ArgumentMatchers.anyList()))
        .thenReturn(false);

        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .criterion(CertificationCriterionDTO.builder()
                                .id(CRITERIA_ID_WITH_RESTRICTIONS)
                                .number("170.315 (c)(3)")
                                .build())
                        .testFunctionalitySingle(PendingCertificationResultTestFunctionalityDTO.builder()
                                .testFunctionalityId(TEST_FUNCTIONALITY_ID_WITH_RESTRICTIONS)
                                .number("(c)(3)(ii)")
                                .build())
                        .build())
                .build();
        // This makes the test func collection a mutable collection - we are removing TFs from the collection
        listing.getCertificationCriterion().get(0).setTestFunctionality(
                new ArrayList<PendingCertificationResultTestFunctionalityDTO>(
                        listing.getCertificationCriterion().get(0).getTestFunctionality()));

        // Run
        reviewer.review(listing);

        // Check
        assertEquals(0, listing.getCertificationCriterion().get(0).getTestFunctionality().size());
    }

}
