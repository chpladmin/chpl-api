package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

public class RequiredAndRelatedCriteriaReviewerTest {
    private static final String CRITERIA_REQUIRED_ERROR_KEY = "listing.criteriaRequired";
    private static final String CRITERIA_REQUIRED_ERROR = "%s is required but was not found.";
    private static final String DEPENDENT_CRITERIA_REQUIRED_ERROR_KEY = "listing.criteria.dependentCriteriaRequired";
    private static final String DEPENDENT_CRITERIA_REQUIRED_ERROR = "Attesting to Criteria %s requires that Criteria %s must also be attested to.";
    private static final String CRITERIA_COMPLEMENT_NOT_FOUND = "Certification criterion %s was found so %s is required but was not found.";

    private CertificationCriterionService certificationCriterionService;
    private ValidationUtils validationUtil;
    private ErrorMessageUtil errorMessageUtil;
    private ResourcePermissions resourcePermissions;
    private RequiredAndRelatedCriteriaReviewer reviewer;

    private CertificationCriterion a1, a4, a6, a9, a10, a13, b1, b2, b10, c1, g4, g5, d1, d2, d2Cures, d3, d3Cures, d4, d5, d6, d7, d8, d9, d10, d10Cures, g10;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void before() throws ParseException {
        a1 = getCriterion(1L, "170.315 (a)(1)", "a1 title", false);
        a4 = getCriterion(4L, "170.315 (a)(4)", "a4 title", false);
        a6 = getCriterion(6L, "170.315 (a)(6)", "a6 title", true);
        a9 = getCriterion(9L, "170.315 (a)(9)", "a9 title", false);
        a10 = getCriterion(10L, "170.315 (a)(10)", "a10 title", false);
        a13 = getCriterion(13L, "170.315 (a)(13)", "a13 title", false);
        b1 = getCriterion(16L, "170.315 (b)(1)", "b1 title", false);
        b2 = getCriterion(17L, "170.315 (b)(2)", "b2 title", false);
        b10 = getCriterion(171L, "170.315 (b)(10)", "b10 title", false);
        c1 = getCriterion(25L, "170.315 (c)(1)", "c1 title", false);
        d1 = getCriterion(29L, "170.315 (d)(1)", "d1 title", false);
        d2 = getCriterion(30L, "170.315 (d)(2)", "d2 old title", false);
        d2Cures = getCriterion(173L, "170.315 (d)(2)", "d2 title (Cures Update)", false);
        d3 = getCriterion(31L, "170.315 (d)(3)", "d3 old title", false);
        d3Cures = getCriterion(174L, "170.315 (d)(3)", "d3 title (Cures Update)", false);
        d4 = getCriterion(32L, "170.315 (d)(4)", "d4 title", false);
        d5 = getCriterion(33L, "170.315 (d)(5)", "d5 title", false);
        d6 = getCriterion(34L, "170.315 (d)(6)", "d6 title", false);
        d7 = getCriterion(35L, "170.315 (d)(7)", "d7 title", false);
        d8 = getCriterion(36L, "170.315 (d)(8)", "d8 title", false);
        d9 = getCriterion(37L, "170.315 (d)(9)", "d9 title", false);
        d10 = getCriterion(38L, "170.315 (d)(10)", "d10 old title", false);
        d10Cures = getCriterion(175L, "170.315 (d)(10)", "d10 title (Cures Update)", false);
        g4 = getCriterion(53L, "170.315 (g)(4)", "g4 title", false);
        g5 = getCriterion(54L, "170.315 (g)(5)", "g5 title", false);
        g10 = getCriterion(182L, "170.315 (g)(10)", "g10 title", false);

        certificationCriterionService = Mockito.mock(CertificationCriterionService.class);

        Mockito.when(certificationCriterionService.get(Criteria2015.A_1)).thenReturn(a1);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_4)).thenReturn(a4);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_6)).thenReturn(a6);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_9)).thenReturn(a9);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_10)).thenReturn(a10);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_13)).thenReturn(a13);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_1_OLD)).thenReturn(b1);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_2_OLD)).thenReturn(b2);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_10)).thenReturn(b10);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_1)).thenReturn(d1);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_2_OLD)).thenReturn(d2);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_2_CURES)).thenReturn(d2Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_3_OLD)).thenReturn(d3);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_3_CURES)).thenReturn(d3Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_4)).thenReturn(d4);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_5)).thenReturn(d5);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_6)).thenReturn(d6);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_7)).thenReturn(d7);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_8)).thenReturn(d8);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_9)).thenReturn(d9);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_10_OLD)).thenReturn(d10);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_10_CURES)).thenReturn(d10Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_4)).thenReturn(g4);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_5)).thenReturn(g5);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_10)).thenReturn(g10);

        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.doesUserHaveRole(ArgumentMatchers.any(List.class))).thenReturn(true);

        CertificationCriterionService criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.getByNumber(ArgumentMatchers.anyString()))
            .thenAnswer(new Answer<List<CertificationCriterion>>() {
                @Override
                public List<CertificationCriterion> answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    List<CertificationCriterion> criteriaWithNumber = new ArrayList<CertificationCriterion>();
                    criteriaWithNumber.add(CertificationCriterion.builder()
                            .number(args[0].toString())
                            .build());
                    return criteriaWithNumber;
                }
            });

        validationUtil = new ValidationUtils(criteriaService);
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(CRITERIA_REQUIRED_ERROR_KEY), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CRITERIA_REQUIRED_ERROR, i.getArgument(1), ""));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(DEPENDENT_CRITERIA_REQUIRED_ERROR_KEY),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DEPENDENT_CRITERIA_REQUIRED_ERROR, i.getArgument(1), i.getArgument(2)));

        reviewer = new RequiredAndRelatedCriteriaReviewer(certificationCriterionService, errorMessageUtil,
                validationUtil, resourcePermissions);
    }

    @Test
    public void review_aCriteriaNotPresent_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_a1CriteriaAttestedWithoutDependencies_hasErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(7, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d2))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d3))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d4))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d5))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d6))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d7))));
    }

    @Test
    public void review_a9ExceptionalCriteriaAttestedWithoutDependencies_hasErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(a9)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(6, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d2))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d3))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d5))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d6))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d7))));
    }

    @Test
    public void review_a6RemovedCriteriaAttestedWithoutDependencies_asAdmin_hasWarnings() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(a6)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(7, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d2))));
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d3))));
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d4))));
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d5))));
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d6))));
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d7))));
    }

    @Test
    public void review_a6RemovedCriteriaAttestedWithoutDependencies_asOnc_hasWarnings() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(a6)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(7, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d2))));
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d3))));
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d4))));
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d5))));
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d6))));
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d7))));
    }

    @Test
    public void review_a6RemovedCriteriaAttestedWithoutDependencies_asAcb_noErrorsNoWarnings() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(a6)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_a1CriteriaAttestedWithAllDependencies_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(a1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d6)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d7)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_a9ExceptionalCriteriaAttestedWithAllDependencies_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(a9)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d6)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d7)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_a6RemovedCriteriaAttestedWithAllDependencies_noWarnings() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(a6)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d6)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d7)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_bCriteriaNotPresent_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_b10OnlyCriteriaAttested_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b10)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_b1CriteriaAttestedWithoutDependencies_hasErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b1)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(7, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d2))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d3))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d5))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d6))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d7))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d8))));
    }

    @Test
    public void review_b1b2CriteriaAttestedWithoutDependencies_hasAllErrorsNoDuplicates() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b2)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(7, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d2))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d3))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d5))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d6))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d7))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d8))));
    }

    @Test
    public void review_b1CriteriaAttestedWithD1AndD5Dependencies_hasCorrectErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d5)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(5, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d2))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d3))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d6))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d7))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d8))));    }

    @Test
    public void review_b1CriteriaAttestedWithAllDependencies_hasNoErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d6)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d7)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d8)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_cCriteriaNotPresent_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_c1CriteriaAttestedWithoutDependencies_hasErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(c1)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(4, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (c)(*)", Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (c)(*)", Util.formatCriteriaNumber(d2))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (c)(*)", Util.formatCriteriaNumber(d3))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (c)(*)", Util.formatCriteriaNumber(d5))));
    }

    @Test
    public void review_c1CriteriaAttestedWithDependencies_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(c1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d5)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_g4g5NotPresent_hasErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        reviewer.review(listing);
        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_REQUIRED_ERROR, Util.formatCriteriaNumber(g4))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_REQUIRED_ERROR, Util.formatCriteriaNumber(g5))));
    }

    @Test
    public void review_g4NotAttested_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.FALSE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_REQUIRED_ERROR, Util.formatCriteriaNumber(g4))));
    }

    @Test
    public void review_g5NotAttested_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.FALSE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_REQUIRED_ERROR, Util.formatCriteriaNumber(g5))));
    }

    @Test
    public void review_g4Andg5Attested_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_g10NotPresent_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_g10NotAttested_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g10)
                        .success(Boolean.FALSE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_g10AttestedMissingAllDependentCriteria_hasErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g10)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(3, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(DEPENDENT_CRITERIA_REQUIRED_ERROR, Util.formatCriteriaNumber(g10), Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(DEPENDENT_CRITERIA_REQUIRED_ERROR, Util.formatCriteriaNumber(g10), Util.formatCriteriaNumber(d9))));
        List<CertificationCriterion> d2Ord10 = Stream.of(d2, d2Cures, d10, d10Cures).collect(Collectors.toList());
        assertTrue(listing.getErrorMessages().contains(String.format(DEPENDENT_CRITERIA_REQUIRED_ERROR, Util.formatCriteriaNumber(g10),
                d2Ord10.stream().map(criterion -> Util.formatCriteriaNumber(criterion)).collect(Collectors.joining(" or ")))));
    }

    @Test
    public void review_g10AttestedMissingD2AndD10_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g10)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d9)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        List<CertificationCriterion> d2Ord10 = Stream.of(d2, d2Cures, d10, d10Cures).collect(Collectors.toList());
        assertTrue(listing.getErrorMessages().contains(String.format(DEPENDENT_CRITERIA_REQUIRED_ERROR, Util.formatCriteriaNumber(g10),
                d2Ord10.stream().map(criterion -> Util.formatCriteriaNumber(criterion)).collect(Collectors.joining(" or ")))));
    }

    @Test
    public void review_g10AttestedWithD1D9D2_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g10)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d9)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_g10AttestedWithD1D9D2Cures_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g10)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d9)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2Cures)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_g10AttestedWithD1D9D10_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g10)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d9)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d10)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_g10AttestedWithD1D9D10Cures_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(g4)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g10)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d9)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d10Cures)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    private CertificationCriterion getCriterion(Long id, String number, String title, boolean removed) {
        return CertificationCriterion.builder()
                .number(number)
                .title(title)
                .id(id)
                .removed(removed)
                .build();
    }
}
