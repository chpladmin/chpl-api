package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class FunctionalityTestedAllowedByRoleReviewerTest {

    private static final String RESTRICTED_FUNCTIONALITY_TESTED_JSON = "[{\"criterionId\":27, \"restrictedFunctionalitiesTested\": [{\"functionalityTestedId\":56, \"allowedRoleNames\":[\"ROLE_ADMIN\",\"ROLE_ONC\"]}]}]";
    private static final String ERROR_MESSAGE = "Current user does not have permission to add/remove functionality tested '%s' for Criteria '%s'.";
    private static final Long CERTIFICATION_RESULT_ID = 5L;
    private static final Long CERTIFICATION_EDITION_ID = 4L;
    private static final Long FUNCTIONALITY_TESTED_ID_RANDOM = 11L;
    private static final Long CERTIFICATION_CRITERION_B2 = 17L;
    private static final Long CERTIFICATION_CRITERION_C3 = 27L;
    private static final Long FUNCTIONALITY_TESTED_CIII = 56L;
    private ResourcePermissions permissions;
    private FunctionalityTestedAllowedByRoleReviewer reviewer;

    @Before
    public void before() {
        // Setup some common mocks - these can be changed in each test if necessary
        ErrorMessageUtil errorMessages = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessages.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(ERROR_MESSAGE);

        permissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.anyList())).thenReturn(true);

        reviewer = new FunctionalityTestedAllowedByRoleReviewer(permissions, errorMessages, RESTRICTED_FUNCTIONALITY_TESTED_JSON);
    }

    @Test
    public void review_functionalityTestedDidNotChange_NoErrorMessages() {
        // Setup
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .name("(b)(2)(i)(E)")
                .functionalityTestedId(FUNCTIONALITY_TESTED_ID_RANDOM)
                .build());

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
                        .functionalitiesTested(functionalitiesTested)
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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAdminAndAddNonRestrictedFunctionalityTested_NoErrorMessages() {
        // Setup
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .name("(b)(2)(i)(E)")
                .functionalityTestedId(FUNCTIONALITY_TESTED_ID_RANDOM)
                .build());
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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAdminAndRemoveNonRestrictedFunctionalityTested_NoErrorMessages() {
        // Setup
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .name("(b)(2)(i)(E)")
                .functionalityTestedId(FUNCTIONALITY_TESTED_ID_RANDOM)
                .build());

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_B2)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (b)(2)")
                                .build())
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder().build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAdminAndAddRestrictedFunctionalityTested_NoErrorMessages() {
        // Setup
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .name("(c)(3)(ii)")
                .functionalityTestedId(FUNCTIONALITY_TESTED_CIII)
                .build());

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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAdminAndRemoveRestrictedFunctionalityTested_NoErrorMessages() {
        // Setup
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .name("(c)(3)(ii)")
                .functionalityTestedId(FUNCTIONALITY_TESTED_CIII)
                .build());

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
                        .functionalitiesTested(functionalitiesTested)
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
    public void review_UserIsAcbAndAddNonRestrictedFunctionalityTested_NoErrorMessages() {
        // Setup
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.anyList())).thenReturn(false);
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .name("(b)(2)(i)(E)")
                .functionalityTestedId(FUNCTIONALITY_TESTED_ID_RANDOM)
                .build());

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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndRemoveNonRestrictedFunctionalityTesed_NoErrorMessages() {
        // Setup
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.anyList())).thenReturn(false);
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .name("(b)(2)(i)(E)")
                .functionalityTestedId(FUNCTIONALITY_TESTED_ID_RANDOM)
                .build());

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(CERTIFICATION_RESULT_ID)
                        .criterion(CertificationCriterion.builder()
                                .id(CERTIFICATION_CRITERION_B2)
                                .certificationEdition("2015")
                                .certificationEditionId(CERTIFICATION_EDITION_ID)
                                .number("170.315 (b)(2)")
                                .build())
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();

        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder().build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndAddRestrictedFunctionalityTested_ErrorMessageAdded() {
        // Setup
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.anyList())).thenReturn(false);
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .name("(c)(3)(ii)")
                .functionalityTestedId(FUNCTIONALITY_TESTED_CIII)
                .build());

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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();

        // Run
        reviewer.review(existingListing, updatedListing);

        // Check
        assertEquals(1, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_UserIsAcbAndRemoveRestrictedFunctionalityTested_ErrorMessageCreated() {
        // Setup
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.anyList())).thenReturn(false);
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .name("(c)(3)(ii)")
                .functionalityTestedId(FUNCTIONALITY_TESTED_CIII)
                .build());

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
                        .functionalitiesTested(functionalitiesTested)
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
    public void review_UserIsAcbAndRemoveRestrictedFunctionalityTestedAndUpdateCriteriaIsAttestedTo_ErrorMessageCreated() {
        // Setup
        Mockito.when(permissions.doesUserHaveRole(ArgumentMatchers.anyList())).thenReturn(false);
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .name("(c)(3)(ii)")
                .functionalityTestedId(FUNCTIONALITY_TESTED_CIII)
                .build());

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
                        .functionalitiesTested(functionalitiesTested)
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
