package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class PrivacyAndSecurityCriteriaReviewerTest {
    private static final String PANDS_MISSING_CRITERIA_ERROR = "Attesting to Criteria %s requires that Criteria %s must also be attested to.";

    private CertificationCriterionService certificationCriterionService;
    private ErrorMessageUtil msgUtil;
    private SpecialProperties specialProperties;
    private ValidationUtils validationUtil;
    private PrivacyAndSecurityCriteriaReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        certificationCriterionService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(certificationCriterionService.get(1L)).thenReturn(getCriterion(1L, "170.315 (a)(1)", false));
        Mockito.when(certificationCriterionService.get(2L)).thenReturn(getCriterion(2L, "170.315 (a)(2)", false));
        Mockito.when(certificationCriterionService.get(3L)).thenReturn(getCriterion(3L, "170.315 (a)(3)", false));
        Mockito.when(certificationCriterionService.get(17L)).thenReturn(getCriterion(17L, "170.315 (b)(2)", true));
        Mockito.when(certificationCriterionService.get(166L)).thenReturn(getCriterion(166L, "170.315 (d)(12)", false));
        Mockito.when(certificationCriterionService.get(167L)).thenReturn(getCriterion(167L, "170.315 (d)(13)", false));

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.dependentCriteriaRequired"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PANDS_MISSING_CRITERIA_ERROR, i.getArgument(1), i.getArgument(2)));

        validationUtil = new ValidationUtils(certificationCriterionService);

        reviewer = new PrivacyAndSecurityCriteriaReviewer(certificationCriterionService, msgUtil, validationUtil, "1,2", "166,167");
    }

    @Test
    public void review_noCertificationResultsAfterCuresEffectiveDate_noErrorMessages() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(System.currentTimeMillis())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_attestsOnlyRemovedCriteriaAndMissingPAndSCriteriaAfterCuresEffectiveDate_noErrorNoWarnings() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(System.currentTimeMillis())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(17L, "170.315 (b)(2)", true))
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_missingPAndSCriteriaAfterCuresEffectiveDate_hasErrorMessages() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(System.currentTimeMillis())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(1L, "170.315 (a)(1)", false))
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(2L, "170.315 (a)(2)", false))
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(166L, "170.315 (d)(12)", false))
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(PANDS_MISSING_CRITERIA_ERROR, "170.315 (a)(1)", "170.315 (d)(13)")));
        assertTrue(listing.getErrorMessages().contains(String.format(PANDS_MISSING_CRITERIA_ERROR, "170.315 (a)(2)", "170.315 (d)(13)")));
    }

    @Test
    public void review_missingPAndSCriteriaBeforeCuresEffectiveDate_hasErrorMessages() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(new GregorianCalendar(2019, Calendar.MARCH, 01).getTime().getTime())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(1L, "170.315 (a)(1)", false))
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(2L, "170.315 (a)(2)", false))
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(166L, "170.315 (d)(12)", false))
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(PANDS_MISSING_CRITERIA_ERROR, "170.315 (a)(1)", "170.315 (d)(13)")));
        assertTrue(listing.getErrorMessages().contains(String.format(PANDS_MISSING_CRITERIA_ERROR, "170.315 (a)(2)", "170.315 (d)(13)")));
    }

    @Test
    public void review_hasAllCriteriaAfterCuresEffectiveDate_noErrorMessages() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(System.currentTimeMillis())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(1L, "170.315 (a)(1)", false))
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(2L, "170.315 (a)(2)", false))
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(166L, "170.315 (d)(12)", false))
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(getCriterion(167L, "170.315 (d)(13)", false))
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    private CertificationCriterion getCriterion(Long id, String number, boolean removed) {
        return CertificationCriterion.builder()
                .id(id)
                .number(number)
                .removed(removed)
                .build();
    }

}
