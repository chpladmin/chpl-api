package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

public class SedRelatedCriteriaReviewerTest {
    private static final String CRITERION_REQUIRED = "Criterion %s is required.";
    private static final String G3_NOT_ALLOWED = "G3 is not allowed but was found.";

    private CertificationCriterion g3;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private SedRelatedCriteriaReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteriaRequired"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CRITERION_REQUIRED, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.g3NotAllowed")))
            .thenReturn(G3_NOT_ALLOWED);
        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        CertificationCriterionService criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_a_1"))).thenReturn(getCriterion(1L, "170.315 (a)(1)", "a1 original", false));
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_a_2"))).thenReturn(getCriterion(2L, "170.315 (a)(2)", "a2 original", false));
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_a_3"))).thenReturn(getCriterion(3L, "170.315 (a)(3)", "a3 original", false));
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_a_4"))).thenReturn(getCriterion(4L, "170.315 (a)(4)", "a4 original", false));
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_a_5"))).thenReturn(getCriterion(5L, "170.315 (a)(5)", "a5 original", false));
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_a_6"))).thenReturn(getCriterion(6L, "170.315 (a)(6)", "a6 original", true));
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_a_7"))).thenReturn(getCriterion(7L, "170.315 (a)(7)", "a7 original", true));
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_a_8"))).thenReturn(getCriterion(8L, "170.315 (a)(8)", "a8 original", true));
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_a_9"))).thenReturn(getCriterion(9L, "170.315 (a)(9)", "a9 original", false));
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_a_14"))).thenReturn(getCriterion(14L, "170.315 (a)(14)", "a14 original", false));
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_b_2_old"))).thenReturn(getCriterion(17L, "170.315 (b)(2)", "b2 original", false));
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_b_2_cures"))).thenReturn(getCriterion(166L, "170.315 (b)(2)", "b2 (Cures Update)", false));
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_b_3_old"))).thenReturn(getCriterion(18L, "170.315 (b)(3)", "b3 original", false));
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_b_3_cures"))).thenReturn(getCriterion(167L, "170.315 (b)(3)", "b3 (Cures Update)", false));
        g3 = getCriterion(52L, "170.315 (g)(3)", "g3 original", false);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_g_3"))).thenReturn(g3);

        ValidationUtils validationUtils = new ValidationUtils(Mockito.mock(CertificationCriterionDAO.class));
        reviewer = new SedRelatedCriteriaReviewer(validationUtils, criteriaService, msgUtil, resourcePermissions);
    }

    @Test
    public void review_noCertificationResults_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_hasG3AndAttestsPresentSedRelatedCriteria_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(1L, "170.315 (a)(1)", "A1 original", false))
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_hasG3AndAttestsRemovedSedRelatedCriteria_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(6L, "170.315 (a)(6)", "A6 original", true))
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(G3_NOT_ALLOWED));
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_noG3AndAttestsPresentSedRelatedCriteria_hasErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(1L, "170.315 (a)(1)", "A1 original", false))
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERION_REQUIRED, Util.formatCriteriaNumber(g3))));
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_noG3AndAttestsRemovedSedRelatedCriteria_hasErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(1L, "170.315 (a)(1)", "A1 original", false))
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERION_REQUIRED, Util.formatCriteriaNumber(g3))));
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_noG3AndAttestsRemovedSedRelatedCriteria_admin_hasWarning() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(6L, "170.315 (a)(6)", "A6 original", true))
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERION_REQUIRED, Util.formatCriteriaNumber(g3))));
    }

    @Test
    public void review_noG3AndAttestsRemovedSedRelatedCriteria_oncAdmin_hasWarning() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(6L, "170.315 (a)(6)", "A6 original", true))
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERION_REQUIRED, Util.formatCriteriaNumber(g3))));
    }

    @Test
    public void review_noG3AndAttestsRemovedSedRelatedCriteria_acbAdmin_noErrors() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(6L, "170.315 (a)(6)", "A6 original", true))
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    private CertificationCriterion getCriterion(Long id, String number, String title, boolean removed) {
        return CertificationCriterion.builder()
                .id(id)
                .number(number)
                .title(title)
                .removed(removed)
                .build();
    }
}
