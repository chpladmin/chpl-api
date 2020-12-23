package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class RequiredAndRelatedCriteriaReviewerTest {
    private static final String CRITERIA_REQUIRED_ERROR_KEY = "listing.criteriaRequired";
    private static final String CRITERIA_REQUIRED_ERROR = "%s is required but was not found.";
    private static final String DEPENDENT_CRITERIA_REQUIRED_ERROR_KEY = "listing.criteria.dependentCriteriaRequired";
    private static final String DEPENDENT_CRITERIA_REQUIRED_ERROR = "Attesting to Criteria %s requires that Criteria %s must also be attested to.";

    private RequiredAndRelatedCriteriaReviewer reviewer;
    private CertificationCriterionService certificationCriterionService;
    private ValidationUtils validationUtil;
    private ErrorMessageUtil errorMessageUtil;
    private ResourcePermissions resourcePermissions;

    private CertificationCriterion b1, b10, g4, g5, d1, d2, d2Cures, d3, d3Cures, d5, d6, d7, d8, d9, d10, d10Cures, g10;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void before() throws ParseException {
        g4 = getCriterion(1L, "170.315 (g)(4)", "g4 title");
        g5 = getCriterion(2L, "170.315 (g)(5)", "g5 title");
        d1 = getCriterion(3L, "170.315 (d)(1)", "d1 title");
        d2 = getCriterion(4L, "170.315 (d)(2)", "d2 old title");
        d2Cures = getCriterion(5L, "170.315 (d)(2)", "d2 title (Cures Update)");
        d3 = getCriterion(6L, "170.315 (d)(3)", "d3 old title");
        d3Cures = getCriterion(7L, "170.315 (d)(3)", "d3 title (Cures Update)");
        d5 = getCriterion(8L, "170.315 (d)(5)", "d5 title");
        d6 = getCriterion(9L, "170.315 (d)(6)", "d6 title");
        d7 = getCriterion(10L, "170.315 (d)(7)", "d7 title");
        d8 = getCriterion(11L, "170.315 (d)(8)", "d8 title");
        d9 = getCriterion(12L, "170.315 (d)(9)", "d9 title");
        d10 = getCriterion(13L, "170.315 (d)(10)", "d10 old title");
        d10Cures = getCriterion(14L, "170.315 (d)(10)", "d10 title (Cures Update)");
        g10 = getCriterion(15L, "170.315 (g)(10)", "g10 title");
        b1 = getCriterion(16L, "170.315 (b)(1)", "b1 title");
        b10 = getCriterion(17L, "170.315 (b)(10)", "b10 title");

        certificationCriterionService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_4)).thenReturn(g4);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_5)).thenReturn(g5);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_1)).thenReturn(d1);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_2_OLD)).thenReturn(d2);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_2_CURES)).thenReturn(d2Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_3_OLD)).thenReturn(d3);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_3_CURES)).thenReturn(d3Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_5)).thenReturn(d5);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_6)).thenReturn(d6);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_7)).thenReturn(d7);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_8)).thenReturn(d8);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_9)).thenReturn(d9);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_10_OLD)).thenReturn(d10);
        Mockito.when(certificationCriterionService.get(Criteria2015.D_10_CURES)).thenReturn(d10Cures);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_10)).thenReturn(g10);

        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.doesUserHaveRole(ArgumentMatchers.any(List.class))).thenReturn(true);
        validationUtil = new ValidationUtils(Mockito.mock(CertificationCriterionDAO.class));
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        reviewer = new RequiredAndRelatedCriteriaReviewer(certificationCriterionService, errorMessageUtil,
                validationUtil, resourcePermissions);
        reviewer.postConstruct();
    }

    @Test
    public void reviewListing_g4g5NotPresent_ReturnsErrors() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(CRITERIA_REQUIRED_ERROR_KEY),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(CRITERIA_REQUIRED_ERROR, i.getArgument(1), ""));
        reviewer.review(listing);
        assertNotNull(listing.getErrorMessages());
        assertEquals(2, listing.getErrorMessages().size());
        listing.getErrorMessages().stream()
            .forEach(errorMessage -> assertTrue(errorMessage.contains("required but was not found")));
    }

    @Test
    public void reviewListing_g4NotAttested_ReturnsError() {
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
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(CRITERIA_REQUIRED_ERROR_KEY),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(CRITERIA_REQUIRED_ERROR, i.getArgument(1), ""));
        reviewer.review(listing);
        assertNotNull(listing.getErrorMessages());
        assertEquals(1, listing.getErrorMessages().size());
        listing.getErrorMessages().stream()
            .forEach(errorMessage -> assertTrue(errorMessage.contains("required but was not found")));
    }

    @Test
    public void reviewListing_g5NotAttested_ReturnsError() {
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
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(CRITERIA_REQUIRED_ERROR_KEY),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(CRITERIA_REQUIRED_ERROR, i.getArgument(1), ""));
        reviewer.review(listing);
        assertNotNull(listing.getErrorMessages());
        assertEquals(1, listing.getErrorMessages().size());
        listing.getErrorMessages().stream()
            .forEach(errorMessage -> assertTrue(errorMessage.contains("required but was not found")));
    }

    @Test
    public void reviewListing_g4Andg5Attested_NoError() {
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
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(CRITERIA_REQUIRED_ERROR_KEY),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(CRITERIA_REQUIRED_ERROR, i.getArgument(1), ""));
        reviewer.review(listing);
        assertNotNull(listing.getErrorMessages());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void reviewListing_g10NotPresent_NoError() {
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
        assertNotNull(listing.getErrorMessages());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void reviewListing_g10NotAttested_NoError() {
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
        assertNotNull(listing.getErrorMessages());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void reviewListing_g10AttestedMissingAllDependentCriteria_ReturnsErrors() {
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
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(DEPENDENT_CRITERIA_REQUIRED_ERROR_KEY),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DEPENDENT_CRITERIA_REQUIRED_ERROR, i.getArgument(1), i.getArgument(2)));
        reviewer.review(listing);
        assertNotNull(listing.getErrorMessages());
        assertEquals(3, listing.getErrorMessages().size());
        listing.getErrorMessages().stream()
            .forEach(errorMessage -> assertTrue(errorMessage.contains("must also be attested to")));
    }

    @Test
    public void reviewListing_g10AttestedMissingD2AndD10_ReturnsError() {
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
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(DEPENDENT_CRITERIA_REQUIRED_ERROR_KEY),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DEPENDENT_CRITERIA_REQUIRED_ERROR, i.getArgument(1), i.getArgument(2)));
        reviewer.review(listing);
        assertNotNull(listing.getErrorMessages());
        assertEquals(1, listing.getErrorMessages().size());
        listing.getErrorMessages().stream()
            .forEach(errorMessage -> assertTrue(errorMessage.contains("must also be attested to")));
    }

    @Test
    public void reviewListing_g10AttestedWithD1D9D2_NoErrors() {
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
        assertNotNull(listing.getErrorMessages());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void reviewListing_g10AttestedWithD1D9D2Cures_NoErrors() {
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
        assertNotNull(listing.getErrorMessages());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void reviewListing_g10AttestedWithD1D9D10_NoErrors() {
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
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (g)(10)")
                                .id(15L)
                                .title("g10 title")
                                .build())
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
        assertNotNull(listing.getErrorMessages());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void reviewListing_g10AttestedWithD1D9D10Cures_NoErrors() {
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
        assertNotNull(listing.getErrorMessages());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void reviewListing_bCriteriaNotPresent_NoError() {
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
        assertNotNull(listing.getErrorMessages());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void reviewListing_b10OnlyCriteriaAttested_NoError() {
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
        assertNotNull(listing.getErrorMessages());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void reviewListing_b1CriteriaAttestedWithoutDependencies_HasError() {
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
        assertNotNull(listing.getErrorMessages());
        assertEquals(1, listing.getErrorMessages().size());
        listing.getErrorMessages().stream()
            .forEach(errorMessage -> assertTrue(errorMessage.contains("(*) was found")));
    }

    private CertificationCriterion getCriterion(Long id, String number, String title) {
        return CertificationCriterion.builder()
                .number(number)
                .title(title)
                .id(id)
                .build();
    }
}
