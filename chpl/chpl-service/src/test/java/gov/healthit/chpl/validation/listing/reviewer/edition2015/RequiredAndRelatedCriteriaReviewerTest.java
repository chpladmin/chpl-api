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

    private RequiredAndRelatedCriteriaReviewer reviewer;
    private CertificationCriterionService certificationCriterionService;
    private ValidationUtils validationUtil;
    private ErrorMessageUtil errorMessageUtil;
    private ResourcePermissions resourcePermissions;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void before() throws ParseException {
        certificationCriterionService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(certificationCriterionService.get(Criteria2015.G_4))
            .thenReturn(getCriterion(1L, "170.315 (g)(4)", "g4 title"));
        Mockito.when(certificationCriterionService.get(Criteria2015.G_5))
            .thenReturn(getCriterion(2L, "170.315 (g)(5)", "g5 title"));
        Mockito.when(certificationCriterionService.get(Criteria2015.D_1))
            .thenReturn(getCriterion(3L, "170.315 (d)(1)", "d1 title"));
        Mockito.when(certificationCriterionService.get(Criteria2015.D_2_OLD))
            .thenReturn(getCriterion(4L, "170.315 (d)(2)", "d2 old title"));
        Mockito.when(certificationCriterionService.get(Criteria2015.D_2_CURES))
            .thenReturn(getCriterion(5L, "170.315 (d)(2)", "d2 title (Cures Update)"));
        Mockito.when(certificationCriterionService.get(Criteria2015.D_3_OLD))
            .thenReturn(getCriterion(6L, "170.315 (d)(3)", "d3 old title"));
        Mockito.when(certificationCriterionService.get(Criteria2015.D_3_CURES))
            .thenReturn(getCriterion(7L, "170.315 (d)(3)", "d3 title (Cures Update)"));
        Mockito.when(certificationCriterionService.get(Criteria2015.D_5))
            .thenReturn(getCriterion(8L, "170.315 (d)(5)", "d5 title"));
        Mockito.when(certificationCriterionService.get(Criteria2015.D_6))
            .thenReturn(getCriterion(9L, "170.315 (d)(6)", "d6 title"));
        Mockito.when(certificationCriterionService.get(Criteria2015.D_7))
            .thenReturn(getCriterion(10L, "170.315 (d)(7)", "d7 title"));
        Mockito.when(certificationCriterionService.get(Criteria2015.D_8))
            .thenReturn(getCriterion(11L, "170.315 (d)(8)", "d8 title"));
        Mockito.when(certificationCriterionService.get(Criteria2015.D_9))
            .thenReturn(getCriterion(12L, "170.315 (d)(9)", "d9 title"));
        Mockito.when(certificationCriterionService.get(Criteria2015.D_10_OLD))
            .thenReturn(getCriterion(13L, "170.315 (d)(10)", "d10 old title"));
        Mockito.when(certificationCriterionService.get(Criteria2015.D_10_CURES))
            .thenReturn(getCriterion(14L, "170.315 (d)(10)", "d10 title (Cures Update)"));
        Mockito.when(certificationCriterionService.get(Criteria2015.G_10))
            .thenReturn(getCriterion(15L, "170.315 (g)(10)", "g10 title"));

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
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (g)(4)")
                                .id(1L)
                                .title("g4 title")
                                .build())
                        .success(Boolean.FALSE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (g)(5)")
                                .id(2L)
                                .title("g5 title")
                                .build())
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
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (g)(4)")
                                .id(1L)
                                .title("g4 title")
                                .build())
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (g)(5)")
                                .id(2L)
                                .title("g5 title")
                                .build())
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
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (g)(4)")
                                .id(1L)
                                .title("g4 title")
                                .build())
                        .success(Boolean.TRUE)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (g)(5)")
                                .id(2L)
                                .title("g5 title")
                                .build())
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

    private CertificationCriterion getCriterion(Long id, String number, String title) {
        return CertificationCriterion.builder()
                .number(number)
                .title(title)
                .id(id)
                .build();
    }
}
