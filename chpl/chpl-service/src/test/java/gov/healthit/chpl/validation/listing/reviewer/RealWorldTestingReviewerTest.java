package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class RealWorldTestingReviewerTest {
    private static final Integer ELIG_YEAR_EXTREME_FUTURE = 3000;
    private static final Integer ELIG_YEAR_FOR_PLAN_TESTING = 2020;
    private static final Integer ELIG_YEAR_FOR_RESULTS_TESTING = 2018;

    private ValidationUtils validationUtils;
    private ErrorMessageUtil errorMessageUtil;
    private RealWorldTestingReviewer reviewer;

    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    @Before
    public void setup() {
        validationUtils = Mockito.mock(ValidationUtils.class);
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        reviewer = new RealWorldTestingReviewer(validationUtils, errorMessageUtil);
        ReflectionTestUtils.setField(reviewer, "rwtPlanStartDayOfYear", "09/01");
        ReflectionTestUtils.setField(reviewer, "rwtResultsStartDayOfYear", "01/01");

    }

    @Test
    public void review_noRwtEligibilityYear_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.notEligible"))
                .thenReturn("Listing is not eligible for Real World Testing.");

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.setRwtEligibilityYear(null);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setRwtPlanUrl("http://www.test.com");
        updated.setRwtPlanSubmissionDate(sdf.parse("08/08/2020"));

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Listing is not eligible for Real World Testing."));
    }

    @Test
    public void review_currentDateBeforePlanEligible_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.<String>any()))
                .thenReturn("Listing is not eligible for Real World Testing Plan data until %s.");

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.setRwtEligibilityYear(ELIG_YEAR_EXTREME_FUTURE);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setRwtPlanUrl("http://www.test.com");
        updated.setRwtPlanSubmissionDate(sdf.parse("08/08/2020"));

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Listing is not eligible for Real World Testing Plan data until %s."));
    }

    @Test
    public void review_noPlanUrl_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.plan.url.required"))
                .thenReturn("Real World Testing Plan URL is required.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.setRwtEligibilityYear(ELIG_YEAR_FOR_PLAN_TESTING);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setRwtEligibilityYear(ELIG_YEAR_FOR_PLAN_TESTING);
        updated.setRwtPlanUrl("");
        updated.setRwtPlanSubmissionDate(sdf.parse("08/08/2020"));

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Plan URL is required."));
    }

    @Test
    public void review_planUrlNotValidUrl_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.plan.url.invalid"))
                .thenReturn("Real World Testing Plan URL is not a well formed URL.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(false);

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.setRwtEligibilityYear(ELIG_YEAR_FOR_PLAN_TESTING);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setRwtEligibilityYear(ELIG_YEAR_FOR_PLAN_TESTING);
        updated.setRwtPlanUrl("not a valid URL");
        updated.setRwtPlanSubmissionDate(sdf.parse("08/08/2020"));

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Plan URL is not a well formed URL."));
    }

    @Test
    public void review_noPlanSubmissionDate_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.plan.submissionDate.required"))
            .thenReturn("Real World Testing Plan Submission Date is required.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.setRwtEligibilityYear(ELIG_YEAR_FOR_PLAN_TESTING);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setRwtEligibilityYear(ELIG_YEAR_FOR_PLAN_TESTING);
        updated.setRwtPlanUrl("http://www.abc.com");
        updated.setRwtPlanSubmissionDate(null);

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Plan Submission Date is required."));
    }

    @Test
    public void review_planSubmissionDateBeforePlanEligibleDate_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.<String>any()))
                .thenReturn("Real World Testing Plan Submission Date must be after %s.");

        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.setRwtEligibilityYear(ELIG_YEAR_FOR_PLAN_TESTING);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setRwtEligibilityYear(ELIG_YEAR_FOR_PLAN_TESTING);
        updated.setRwtPlanUrl("http://www.abc.com");
        updated.setRwtPlanSubmissionDate(sdf.parse("08/08/2019"));

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Plan Submission Date must be after %s."));
    }

    @Test
    public void review_currentDateBeforeResultsEligible_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.<String>any()))
                .thenReturn("Listing is not eligible for Real World Testing Results data until %s.");

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.setRwtEligibilityYear(ELIG_YEAR_EXTREME_FUTURE);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setRwtResultsUrl("http://www.test.com");
        updated.setRwtResultsSubmissionDate(sdf.parse("01/08/2021"));

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Listing is not eligible for Real World Testing Results data until %s."));
    }

    @Test
    public void review_noResultsUrl_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.results.url.required"))
                .thenReturn("Real World Testing Results URL is required.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.setRwtEligibilityYear(ELIG_YEAR_FOR_RESULTS_TESTING);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setRwtEligibilityYear(ELIG_YEAR_FOR_RESULTS_TESTING);
        updated.setRwtResultsUrl("");
        updated.setRwtResultsSubmissionDate(sdf.parse("01/08/2021"));

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
        existing.setRwtEligibilityYear(ELIG_YEAR_FOR_RESULTS_TESTING);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setRwtEligibilityYear(ELIG_YEAR_FOR_RESULTS_TESTING);
        updated.setRwtResultsUrl("not a valid URL");
        updated.setRwtResultsSubmissionDate(sdf.parse("01/08/2021"));

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Results URL is not a well formed URL."));
    }

    @Test
    public void review_noResultsSubmissionDate_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.results.submissionDate.required"))
            .thenReturn("Real World Testing Results Submission Date is required.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.setRwtEligibilityYear(ELIG_YEAR_FOR_RESULTS_TESTING);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setRwtEligibilityYear(ELIG_YEAR_FOR_RESULTS_TESTING);
        updated.setRwtResultsUrl("http://www.abc.com");
        updated.setRwtResultsSubmissionDate(null);

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Results Submission Date is required."));
    }

    @Test
    public void review_resultsSubmissionDateBeforeResultsEligibleDate_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.<String>any()))
                .thenReturn("Real World Testing Results Submission Date must be after %s.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails existing = new CertifiedProductSearchDetails();
        existing.setRwtEligibilityYear(ELIG_YEAR_FOR_RESULTS_TESTING);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setRwtEligibilityYear(ELIG_YEAR_FOR_RESULTS_TESTING);
        updated.setRwtResultsUrl("http://www.abc.com");
        updated.setRwtResultsSubmissionDate(sdf.parse("08/08/2018"));

        reviewer.review(existing, updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Results Submission Date must be after %s."));
    }
}
