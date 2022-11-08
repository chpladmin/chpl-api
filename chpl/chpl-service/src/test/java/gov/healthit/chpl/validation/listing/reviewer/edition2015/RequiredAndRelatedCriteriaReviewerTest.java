package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

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
    private static final String CRITERIA_COMPLEMENT_NOT_FOUND_KEY = "listing.criteria.complementaryCriteriaRequired";
    private static final String CRITERIA_COMPLEMENT_NOT_FOUND = "Certification criterion %s was found so %s is required but was not found.";

    private CertificationCriterionService certificationCriterionService;
    private ValidationUtils validationUtil;
    private ErrorMessageUtil errorMessageUtil;
    private ResourcePermissions resourcePermissions;
    private RequiredAndRelatedCriteriaReviewer reviewer;

    private CertificationCriterion a1, a2, a3, a4, a5, a6, a9, a10, a12, a13, a14, a15,
        b1, b1Cures, b2, b2Cures, b3, b3Cures, b4, b6, b7, b7Cures, b8, b8Cures, b9, b9Cures, b10,
        c1, c2, c3, c3Cures, c4, g4, g5, d1, d2, d2Cures, d3, d3Cures, d4, d5, d6, d7, d8, d9, d10, d10Cures,
        e1, e1Cures, e2, e3, f1, f2, f3, f4, f5, f5Cures, f6, f7, g6, g6Cures, g7, g8, g9, g9Cures, g10, h1, h2;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void before() throws ParseException {
        a1 = getCriterion(1L, "170.315 (a)(1)", "a1 title", false);
        a2 = getCriterion(2L, "170.315 (a)(2)", "a2 title", false);
        a3 = getCriterion(3L, "170.315 (a)(3)", "a3 title", false);
        a4 = getCriterion(4L, "170.315 (a)(4)", "a4 title", false);
        a5 = getCriterion(5L, "170.315 (a)(5)", "a5 title", false);
        a6 = getCriterion(6L, "170.315 (a)(6)", "a6 title", true);
        a9 = getCriterion(9L, "170.315 (a)(9)", "a9 title", false);
        a10 = getCriterion(10L, "170.315 (a)(10)", "a10 title", true);
        a12 = getCriterion(12L, "170.315 (a)(12)", "a12 title", false);
        a13 = getCriterion(13L, "170.315 (a)(13)", "a13 title", true);
        a14 = getCriterion(14L, "170.315 (a)(14)", "a14 title", false);
        a15 = getCriterion(15L, "170.315 (a)(15)", "a15 title", false);
        b1 = getCriterion(16L, "170.315 (b)(1)", "b1 title", true);
        b1Cures = getCriterion(165L, "170.315 (b)(1)", "b1 title (Cures Update)", false);
        b2 = getCriterion(17L, "170.315 (b)(2)", "b2 title", true);
        b2Cures = getCriterion(166L, "170.315 (b)(2)", "b2 title (Cures Update)", false);
        b3 = getCriterion(18L, "170.315 (b)(3)", "b3 title", true);
        b3Cures = getCriterion(167L, "170.315 (b)(3)", "b3 title (Cures Update)", false);
        b4 = getCriterion(19L, "170.315 (b)(4)", "b4 title", true);
        b6 = getCriterion(21L, "170.315 (b)(6)", "b6 title", false);
        b7 = getCriterion(22L, "170.315 (b)(7)", "b7 title", true);
        b7Cures = getCriterion(168L, "170.315 (b)(7)", "b7 title (Cures Update)", false);
        b8 = getCriterion(23L, "170.315 (b)(8)", "b8 title", true);
        b8Cures = getCriterion(169L, "170.315 (b)(8)", "b8 title (Cures Update)", false);
        b9 = getCriterion(24L, "170.315 (b)(9)", "b9 title", true);
        b9Cures = getCriterion(170L, "170.315 (b)(9)", "b9 title (Cures Update)", false);
        b10 = getCriterion(171L, "170.315 (b)(10)", "b10 title", false);
        c1 = getCriterion(25L, "170.315 (c)(1)", "c1 title", false);
        c2 = getCriterion(26L, "170.315 (c)(2)", "c2 title", true);
        c3 = getCriterion(27L, "170.315 (c)(3)", "c3 title", true);
        c3Cures = getCriterion(172L, "170.315 (c)(3)", "c3 title (Cures Update)", false);
        c4 = getCriterion(27L, "170.315 (c)(4)", "c4 title", false);
        d1 = getCriterion(29L, "170.315 (d)(1)", "d1 title", false);
        d2 = getCriterion(30L, "170.315 (d)(2)", "d2 old title", true);
        d2Cures = getCriterion(173L, "170.315 (d)(2)", "d2 title (Cures Update)", false);
        d3 = getCriterion(31L, "170.315 (d)(3)", "d3 old title", true);
        d3Cures = getCriterion(174L, "170.315 (d)(3)", "d3 title (Cures Update)", false);
        d4 = getCriterion(32L, "170.315 (d)(4)", "d4 title", false);
        d5 = getCriterion(33L, "170.315 (d)(5)", "d5 title", false);
        d6 = getCriterion(34L, "170.315 (d)(6)", "d6 title", false);
        d7 = getCriterion(35L, "170.315 (d)(7)", "d7 title", false);
        d8 = getCriterion(36L, "170.315 (d)(8)", "d8 title", false);
        d9 = getCriterion(37L, "170.315 (d)(9)", "d9 title", false);
        d10 = getCriterion(38L, "170.315 (d)(10)", "d10 old title", true);
        d10Cures = getCriterion(175L, "170.315 (d)(10)", "d10 title (Cures Update)", false);
        e1 = getCriterion(40L, "170.315 (e)(1)", "e1 title", true);
        e1Cures = getCriterion(178L, "170.315 (e)(1)", "e1 title (Cures Update)", false);
        e2 = getCriterion(41L, "170.315 (e)(2)", "e2 title", true);
        e3 = getCriterion(42L, "170.315 (e)(3)", "e3 title", false);
        f1 = getCriterion(43L, "170.315 (f)(1)", "f1 title", false);
        f2 = getCriterion(44L, "170.315 (f)(2)", "f2 title", false);
        f3 = getCriterion(45L, "170.315 (f)(3)", "f3 title", false);
        f4 = getCriterion(46L, "170.315 (f)(4)", "f4 title", false);
        f5 = getCriterion(47L, "170.315 (f)(5)", "f5 title", true);
        f5Cures = getCriterion(179L, "170.315 (f)(5)", "f5 title (Cures Update)", false);
        f6 = getCriterion(48L, "170.315 (f)(6)", "f6 title", false);
        f7 = getCriterion(49L, "170.315 (f)(7)", "f7 title", false);
        g4 = getCriterion(53L, "170.315 (g)(4)", "g4 title", false);
        g5 = getCriterion(54L, "170.315 (g)(5)", "g5 title", false);
        g6 = getCriterion(55L, "170.315 (g)(6)", "g6 title", true);
        g6Cures = getCriterion(180L, "170.315 (g)(6)", "g6 title (Cures Update)", false);
        g7 = getCriterion(56L, "170.315 (g)(7)", "g7 title", false);
        g8 = getCriterion(57L, "170.315 (g)(8)", "g8 title", true);
        g9 = getCriterion(58L, "170.315 (g)(9)", "g9 title", true);
        g9Cures = getCriterion(181L, "170.315 (g)(9)", "g9 title (Cures Update)", false);
        g10 = getCriterion(182L, "170.315 (g)(10)", "g10 title", false);
        h1 = getCriterion(59L, "170.315 (h)(1)", "h1 title", false);
        h2 = getCriterion(60L, "170.315 (h)(2)", "h2 title", false);

        certificationCriterionService = Mockito.mock(CertificationCriterionService.class);

        Mockito.when(certificationCriterionService.get(Criteria2015.A_1)).thenReturn(a1);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_2)).thenReturn(a2);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_3)).thenReturn(a3);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_4)).thenReturn(a4);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_5)).thenReturn(a5);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_6)).thenReturn(a6);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_9)).thenReturn(a9);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_10)).thenReturn(a10);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_12)).thenReturn(a12);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_13)).thenReturn(a13);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_14)).thenReturn(a14);
        Mockito.when(certificationCriterionService.get(Criteria2015.A_15)).thenReturn(a15);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_1_OLD)).thenReturn(b1);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_1_CURES)).thenReturn(b1Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_2_OLD)).thenReturn(b2);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_2_CURES)).thenReturn(b2Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_3_OLD)).thenReturn(b3);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_3_CURES)).thenReturn(b3Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_4)).thenReturn(b4);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_6)).thenReturn(b6);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_7_OLD)).thenReturn(b7);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_7_CURES)).thenReturn(b7Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_8_OLD)).thenReturn(b8);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_8_CURES)).thenReturn(b8Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_9_OLD)).thenReturn(b9);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_9_CURES)).thenReturn(b9Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.B_10)).thenReturn(b10);
        Mockito.when(certificationCriterionService.get(Criteria2015.C_1)).thenReturn(c1);
        Mockito.when(certificationCriterionService.get(Criteria2015.C_2)).thenReturn(c2);
        Mockito.when(certificationCriterionService.get(Criteria2015.C_3_OLD)).thenReturn(c3);
        Mockito.when(certificationCriterionService.get(Criteria2015.C_3_CURES)).thenReturn(c3Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.C_4)).thenReturn(c4);
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
        Mockito.when(certificationCriterionService.get(Criteria2015.E_1_OLD)).thenReturn(e1);
        Mockito.when(certificationCriterionService.get(Criteria2015.E_1_CURES)).thenReturn(e1Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.E_2)).thenReturn(e2);
        Mockito.when(certificationCriterionService.get(Criteria2015.E_3)).thenReturn(e3);
        Mockito.when(certificationCriterionService.get(Criteria2015.F_1)).thenReturn(f1);
        Mockito.when(certificationCriterionService.get(Criteria2015.F_2)).thenReturn(f2);
        Mockito.when(certificationCriterionService.get(Criteria2015.F_3)).thenReturn(f3);
        Mockito.when(certificationCriterionService.get(Criteria2015.F_4)).thenReturn(f4);
        Mockito.when(certificationCriterionService.get(Criteria2015.F_5_OLD)).thenReturn(f5);
        Mockito.when(certificationCriterionService.get(Criteria2015.F_5_CURES)).thenReturn(f5Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.F_6)).thenReturn(f6);
        Mockito.when(certificationCriterionService.get(Criteria2015.F_7)).thenReturn(f7);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_4)).thenReturn(g4);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_5)).thenReturn(g5);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_6_OLD)).thenReturn(g6);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_6_CURES)).thenReturn(g6Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_7)).thenReturn(g7);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_8)).thenReturn(g8);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_9_OLD)).thenReturn(g9);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_9_CURES)).thenReturn(g9Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_10)).thenReturn(g10);
        Mockito.when(certificationCriterionService.get(Criteria2015.H_1)).thenReturn(h1);
        Mockito.when(certificationCriterionService.get(Criteria2015.H_2)).thenReturn(h2);

        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.doesUserHaveRole(ArgumentMatchers.any(List.class))).thenReturn(true);
        validationUtil = new ValidationUtils(certificationCriterionService);
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(CRITERIA_REQUIRED_ERROR_KEY), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CRITERIA_REQUIRED_ERROR, i.getArgument(1), ""));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(CRITERIA_COMPLEMENT_NOT_FOUND_KEY),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(CRITERIA_COMPLEMENT_NOT_FOUND, i.getArgument(1), i.getArgument(2)));

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
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d2Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d3Cures))));
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
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d2Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d3Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d5))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d6))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (a)(*)", Util.formatCriteriaNumber(d7))));
    }

    @Test
    public void review_a6RemovedCriteriaAttestedWithoutDependencies_asAdmin_noErrorsNoWarnings() {
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
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_a6RemovedCriteriaAttestedWithoutDependencies_asOnc_noErrorsNoWarnings() {
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
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
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
                        .criterion(d2Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3Cures)
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
                        .criterion(d2Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3Cures)
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
    public void review_a6RemovedCriteriaAttestedWithAllDependencies_noErrorsNoWarnings() {
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
        assertEquals(0, listing.getErrorMessages().size());
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
    public void review_b7RemovedCriteriaAttestedWithoutBCriteriaDependencies_noErrors() {
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
                        .criterion(b7)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_b7b8RemovedCriteriaAttestedWithoutDependencies_noErrors() {
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
                        .criterion(b7)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b8)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_b7Curesb8RemovedCriteriaAttestedWithoutDependencies_hasErrors() {
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
                        .criterion(b7Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b8)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(7, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d2Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d3Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d5))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d6))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d7))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d8))));
    }

    @Test
    public void review_b3CuresCriteriaAttestedWithD1AndD5Dependencies_hasCorrectErrors() {
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
                        .criterion(b3Cures)
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
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d2Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d3Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d6))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d7))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (b)(*)", Util.formatCriteriaNumber(d8))));
    }

    @Test
    public void review_b3CuresCriteriaAttestedWithAllDependencies_hasNoErrors() {
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
                        .criterion(b3Cures)
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
                        .criterion(d3Cures)
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
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (c)(*)", Util.formatCriteriaNumber(d2Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (c)(*)", Util.formatCriteriaNumber(d3Cures))));
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
                        .criterion(d3Cures)
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
    public void review_c2RemovedCriteriaAttestedWithoutDependencies_adminUser_noWarnings() {
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
                        .criterion(c2)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_c2RemovedCriteriaAttestedWithoutDependencies_oncUser_hasNoWarnings() {
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
                        .criterion(c2)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_c2RemovedCriteriaAttestedWithoutDependencies_acbUser_noWarnings() {
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
                        .criterion(c2)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_c2RemovedCriteriaAttestedWithDependencies_adminUser_noErrorsNoWarnings() {
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
                        .criterion(c2)
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
                        .criterion(d3Cures)
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
    public void review_e1CriteriaNotPresent_noError() {
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
    public void review_e1CuresCriteriaAttestedWithoutDependencies_hasErrors() {
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
                        .criterion(g6Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(e1Cures)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(6, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e1Cures), Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e1Cures), Util.formatCriteriaNumber(d2Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e1Cures), Util.formatCriteriaNumber(d3Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e1Cures), Util.formatCriteriaNumber(d5))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e1Cures), Util.formatCriteriaNumber(d7))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e1Cures), Util.formatCriteriaNumber(d9))));
    }

    @Test
    public void review_e1RemovedCriteriaAttestedWithoutDependencies_noErrors() {
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
                        .criterion(g6Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(e1)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_e1CuresCriteriaAttestedWithDependencies_noErrors() {
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
                        .criterion(g6Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(e1)
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
                        .criterion(d3Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d7)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d9)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_e2E3CriteriaNotPresent_noError() {
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
    public void review_e2RemovedCriteriaAttestedWithoutDependencies_noErrors() {
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
                        .criterion(e2)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_e3CriteriaAttestedWithoutDependencies_hasErrors() {
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
                        .criterion(e3)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(5, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d2Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d3Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d5))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d9))));
    }

    @Test
    public void review_e2Ande3CriteriaAttestedWithoutDependencies_hasErrors() {
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
                        .criterion(e2)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(e3)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(5, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d2Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d3Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d5))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d9))));
    }

    @Test
    public void review_e3AndD1CriteriaAttestedWithoutDependencies_hasErrors() {
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
                        .criterion(e3)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(4, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d2Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d3Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d5))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d9))));
    }

    @Test
    public void review_e3AndD2CuresCriteriaAttestedWithoutDependencies_hasErrors() {
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
                        .criterion(e3)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2Cures)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(4, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d3Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d5))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d9))));
    }


    @Test
    public void review_e3AndSomeCriteriaAttestedWithoutDependencies_hasErrors() {
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
                        .criterion(e2)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(e3)
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
                        .criterion(d3Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d9)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d2Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, Util.formatCriteriaNumber(e3), Util.formatCriteriaNumber(d5))));
    }

    @Test
    public void review_e3CriteriaAttestedWithDependencies_noErrors() {
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
                        .criterion(e3)
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
                        .criterion(d3Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d5)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d9)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_fCriteriaNotPresent_noError() {
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
    public void review_f1CriteriaAttestedWithoutDependencies_hasErrors() {
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
                        .criterion(f1)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(4, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (f)(*)", Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (f)(*)", Util.formatCriteriaNumber(d2Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (f)(*)", Util.formatCriteriaNumber(d3Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (f)(*)", Util.formatCriteriaNumber(d7))));
    }

    @Test
    public void review_f5CuresCriteriaAttestedWithoutDependencies_hasErrors() {
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
                        .criterion(f5Cures)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(4, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (f)(*)", Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (f)(*)", Util.formatCriteriaNumber(d2Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (f)(*)", Util.formatCriteriaNumber(d3Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (f)(*)", Util.formatCriteriaNumber(d7))));
    }

    @Test
    public void review_f5OriginalCriteriaAttestedWithoutDependencies_noErrorsNoWarnings() {
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
                        .criterion(f5)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_f1CriteriaAttestedWithDependencies_noErrors() {
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
                        .criterion(f1)
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
                        .criterion(d3Cures)
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
    public void review_criteriaRequiringG6NotPresent_noError() {
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
    public void review_criterionB1CuresRequiringG6CuresAttestedWithoutG6_hasErrors() {
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
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3Cures)
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
                .certificationResult(CertificationResult.builder()
                        .criterion(b1Cures)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_REQUIRED_ERROR, Util.formatCriteriaNumber(g6Cures))));
    }

    @Test
    public void review_criterionB2CuresRequiringG6CuresAttestedWithG6_hasErrors() {
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
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3Cures)
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
                .certificationResult(CertificationResult.builder()
                        .criterion(g6)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b2Cures)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_REQUIRED_ERROR, Util.formatCriteriaNumber(g6Cures))));
    }

    @Test
    public void review_criterionB9RemovedRequiringG6CuresAttestedWithoutG6_adminUser_hasWarnings() {
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
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3Cures)
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
                .certificationResult(CertificationResult.builder()
                        .criterion(b9)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_REQUIRED_ERROR, Util.formatCriteriaNumber(g6Cures))));
    }

    @Test
    public void review_criterionB9RemovedRequiringG6CuresAttestedWithoutG6_oncUser_hasWarnings() {
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
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3Cures)
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
                .certificationResult(CertificationResult.builder()
                        .criterion(b9)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CRITERIA_REQUIRED_ERROR, Util.formatCriteriaNumber(g6Cures))));
    }

    @Test
    public void review_criterionB9RemovedRequiringG6CuresAttestedWithoutG6_acbUser_noWarnings() {
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
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3Cures)
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
                .certificationResult(CertificationResult.builder()
                        .criterion(b9)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_b1CuresCriteriaRequiringG6CuresAttestedWithG6Cures_noErrors() {
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
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d2Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d3Cures)
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
                .certificationResult(CertificationResult.builder()
                        .criterion(b1Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g6Cures)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_g7g9Curesg10CriteriaNotPresent_noError() {
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
    public void review_g7CriteriaAttestedWithoutDependencies_hasErrors() {
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
                        .criterion(g7)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(3, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND,
                Util.formatCriteriaNumber(g7) + " or " + Util.formatCriteriaNumber(g9Cures) + " or " + Util.formatCriteriaNumber(g10),
                Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND,
                Util.formatCriteriaNumber(g7) + " or " + Util.formatCriteriaNumber(g9Cures) + " or " + Util.formatCriteriaNumber(g10),
                Util.formatCriteriaNumber(d9))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND,
                Util.formatCriteriaNumber(g7) + " or " + Util.formatCriteriaNumber(g9Cures) + " or " + Util.formatCriteriaNumber(g10),
                Util.formatCriteriaNumber(d2Cures) + " or " + Util.formatCriteriaNumber(d10Cures))));
    }

    @Test
    public void review_g7AndG9CuresCriteriaAttestedWithoutDependencies_hasErrors() {
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
                        .criterion(g6Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g7)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g9Cures)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(3, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND,
                Util.formatCriteriaNumber(g7) + " or " + Util.formatCriteriaNumber(g9Cures) + " or " + Util.formatCriteriaNumber(g10),
                Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND,
                Util.formatCriteriaNumber(g7) + " or " + Util.formatCriteriaNumber(g9Cures) + " or " + Util.formatCriteriaNumber(g10),
                Util.formatCriteriaNumber(d9))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND,
                Util.formatCriteriaNumber(g7) + " or " + Util.formatCriteriaNumber(g9Cures) + " or " + Util.formatCriteriaNumber(g10),
                Util.formatCriteriaNumber(d2Cures) + " or " + Util.formatCriteriaNumber(d10Cures))));
    }

    @Test
    public void review_g7AndG9CuresAndG10CriteriaAttestedWithoutDependencies_hasErrors() {
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
                        .criterion(g6Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g7)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g9Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g10)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(3, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND,
                Util.formatCriteriaNumber(g7) + " or " + Util.formatCriteriaNumber(g9Cures) + " or " + Util.formatCriteriaNumber(g10),
                Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND,
                Util.formatCriteriaNumber(g7) + " or " + Util.formatCriteriaNumber(g9Cures) + " or " + Util.formatCriteriaNumber(g10),
                Util.formatCriteriaNumber(d9))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND,
                Util.formatCriteriaNumber(g7) + " or " + Util.formatCriteriaNumber(g9Cures) + " or " + Util.formatCriteriaNumber(g10),
                Util.formatCriteriaNumber(d2Cures) + " or " + Util.formatCriteriaNumber(d10Cures))));
    }

    @Test
    public void review_g9RemovedCriteriaAttestedWithoutDependencies_noErrors() {
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
                        .criterion(g9)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_g9CuresCriteriaAttestedWithD9D10Cures_hasError() {
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
                        .criterion(g6Cures)
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
                .certificationResult(CertificationResult.builder()
                        .criterion(g9Cures)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND,
                Util.formatCriteriaNumber(g7) + " or " + Util.formatCriteriaNumber(g9Cures) + " or " + Util.formatCriteriaNumber(g10),
                Util.formatCriteriaNumber(d1))));
    }

    @Test
    public void review_g9CuresCriteriaAttestedWithD1D10Cures_hasError() {
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
                        .criterion(g6Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d1)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(d10Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(g9Cures)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND,
                Util.formatCriteriaNumber(g7) + " or " + Util.formatCriteriaNumber(g9Cures) + " or " + Util.formatCriteriaNumber(g10),
                Util.formatCriteriaNumber(d9))));
    }

    @Test
    public void review_g9CuresCriteriaAttestedWithD1D9_hasError() {
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
                        .criterion(g6Cures)
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
                        .criterion(g9Cures)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND,
                Util.formatCriteriaNumber(g7) + " or " + Util.formatCriteriaNumber(g9Cures) + " or " + Util.formatCriteriaNumber(g10),
                Util.formatCriteriaNumber(d2Cures) + " or " + Util.formatCriteriaNumber(d10Cures))));
    }

    @Test
    public void review_g7CriteriaAttestedWithDependencies_noErrors() {
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
                        .criterion(g7)
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
                        .criterion(d9)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_hCriteriaNotPresent_noError() {
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
    public void review_h1CriteriaAttestedWithoutDependencies_hasErrors() {
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
                        .criterion(g6Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(h1)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (h)(*)", Util.formatCriteriaNumber(d1))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (h)(*)", Util.formatCriteriaNumber(d2Cures))));
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND, "170.315 (h)(*)", Util.formatCriteriaNumber(d3Cures))));
    }

    @Test
    public void review_h1CriteriaAttestedWithDependencies_noErrors() {
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
                        .criterion(g6Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(b1Cures)
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(h1)
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
                        .criterion(d3Cures)
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
    public void review_h1CriteriaAttestedWithoutB1Cures_hasError() {
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
                        .criterion(h1)
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
                        .criterion(d3Cures)
                        .success(Boolean.TRUE)
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CRITERIA_COMPLEMENT_NOT_FOUND,
                Util.formatCriteriaNumber(h1),
                Util.formatCriteriaNumber(b1Cures))));
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
