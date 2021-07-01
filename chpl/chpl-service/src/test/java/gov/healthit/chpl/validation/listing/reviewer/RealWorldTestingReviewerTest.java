package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class RealWorldTestingReviewerTest {
    private static final Integer ELIG_YEAR_FOR_PLAN_TESTING = 2020;
    private static final Integer ELIG_YEAR_FOR_RESULTS_TESTING = 2018;

    private ValidationUtils validationUtils;
    private ErrorMessageUtil errorMessageUtil;
    private RealWorldTestingReviewer reviewer;

    @Before
    public void setup() {
        validationUtils = Mockito.mock(ValidationUtils.class);
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        reviewer = new RealWorldTestingReviewer(validationUtils, errorMessageUtil);
        //ReflectionTestUtils.setField(reviewer, "rwtPlanStartDayOfYear", "09/01");
        //ReflectionTestUtils.setField(reviewer, "rwtResultsStartDayOfYear", "01/01");

    }

    @Test
    public void review_noPlanUrl_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.plans.url.required"))
                .thenReturn("Real World Testing Plans URL is required.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        existing.setRwtEligibilityYear(ELIG_YEAR_FOR_PLAN_TESTING);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        updated.setRwtEligibilityYear(ELIG_YEAR_FOR_PLAN_TESTING);
        updated.setRwtPlansUrl("");
        updated.setRwtPlansCheckDate(LocalDate.parse("2020-08-08"));

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Plans URL is required."));
    }

    @Test
    public void review_planUrlNotValidUrl_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.plans.url.invalid"))
                .thenReturn("Real World Testing Plans URL is not a well formed URL.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(false);

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        existing.setRwtEligibilityYear(ELIG_YEAR_FOR_PLAN_TESTING);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        updated.setRwtEligibilityYear(ELIG_YEAR_FOR_PLAN_TESTING);
        updated.setRwtPlansUrl("not a valid URL");
        updated.setRwtPlansCheckDate(LocalDate.parse("2020-08-08"));

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Plans URL is not a well formed URL."));
    }

    @Test
    public void review_noPlanSubmissionDate_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.plans.checkDate.required"))
            .thenReturn("Real World Testing Plans Check Date is required.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        existing.setRwtEligibilityYear(ELIG_YEAR_FOR_PLAN_TESTING);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        updated.setRwtEligibilityYear(ELIG_YEAR_FOR_PLAN_TESTING);
        updated.setRwtPlansUrl("http://www.abc.com");
        updated.setRwtPlansCheckDate(null);

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Plans Check Date is required."));
    }

    @Test
    public void review_noResultsUrl_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.results.url.required"))
                .thenReturn("Real World Testing Results URL is required.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        existing.setRwtEligibilityYear(ELIG_YEAR_FOR_RESULTS_TESTING);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        updated.setRwtEligibilityYear(ELIG_YEAR_FOR_RESULTS_TESTING);
        updated.setRwtResultsUrl("");
        updated.setRwtResultsCheckDate(LocalDate.parse("2022-01-08"));

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Results URL is required."));
    }

    @Test
    public void review_resultsUrlNotValidUrl_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.results.url.invalid"))
                .thenReturn("Real World Testing Results URL is not a well formed URL.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(false);

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        existing.setRwtEligibilityYear(ELIG_YEAR_FOR_RESULTS_TESTING);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        updated.setRwtEligibilityYear(ELIG_YEAR_FOR_RESULTS_TESTING);
        updated.setRwtResultsUrl("not a valid URL");
        updated.setRwtResultsCheckDate(LocalDate.parse("2022-01-08"));

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Results URL is not a well formed URL."));
    }

    @Test
    public void review_noResultsSubmissionDate_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.results.checkDate.required"))
            .thenReturn("Real World Testing Results Check Date is required.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        existing.setRwtEligibilityYear(ELIG_YEAR_FOR_RESULTS_TESTING);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        updated.setRwtEligibilityYear(ELIG_YEAR_FOR_RESULTS_TESTING);
        updated.setRwtResultsUrl("http://www.abc.com");
        updated.setRwtResultsCheckDate(null);

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Results Check Date is required."));
    }

    @Test
    public void review_rwtEligibilityYearChanged_errorMessage() {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.<String>any()))
        .thenReturn("Real World Eligibility Year cannot be updated.");

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.setRwtEligibilityYear(null);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        updated.setRwtEligibilityYear(ELIG_YEAR_FOR_RESULTS_TESTING);

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Eligibility Year cannot be updated."));

    }

}
