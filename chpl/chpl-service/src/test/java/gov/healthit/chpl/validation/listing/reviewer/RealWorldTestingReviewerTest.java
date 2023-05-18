package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class RealWorldTestingReviewerTest {

    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil errorMessageUtil;
    private RealWorldTestingReviewer reviewer;

    @Before
    public void setup() throws EntityRetrievalException {
        certifiedProductDetailsManager = Mockito.mock(CertifiedProductDetailsManager.class);

        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        Mockito.when(certifiedProductDetailsManager.getCertifiedProductDetails(ArgumentMatchers.anyLong()))
                .thenReturn(listing);

        validationUtils = Mockito.mock(ValidationUtils.class);
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        reviewer = new RealWorldTestingReviewer(certifiedProductDetailsManager, validationUtils, errorMessageUtil);
    }

    @Test
    public void review_noPlanUrl_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.plans.url.required"))
                .thenReturn("Real World Testing Plans URL is required.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setId(1l);
        updated.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        updated.setRwtPlansUrl("");
        updated.setRwtPlansCheckDate(LocalDate.parse("2020-08-08"));

        reviewer.review(updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Plans URL is required."));
    }

    @Test
    public void review_planUrlNotValidUrl_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.plans.url.invalid"))
                .thenReturn("Real World Testing Plans URL is not a well formed URL.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(false);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setId(1l);
        updated.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        updated.setRwtPlansUrl("not a valid URL");
        updated.setRwtPlansCheckDate(LocalDate.parse("2020-08-08"));

        reviewer.review(updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Plans URL is not a well formed URL."));
    }

    @Test
    public void review_noPlanSubmissionDate_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.plans.checkDate.required"))
            .thenReturn("Real World Testing Plans Check Date is required.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setId(1l);
        updated.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        updated.setRwtPlansUrl("http://www.abc.com");
        updated.setRwtPlansCheckDate(null);

        reviewer.review(updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Plans Check Date is required."));
    }

    @Test
    public void review_noResultsUrl_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.results.url.required"))
                .thenReturn("Real World Testing Results URL is required.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setId(1l);
        updated.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        updated.setRwtResultsUrl("");
        updated.setRwtResultsCheckDate(LocalDate.parse("2022-01-08"));

        reviewer.review(updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Results URL is required."));
    }

    @Test
    public void review_resultsUrlNotValidUrl_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.results.url.invalid"))
                .thenReturn("Real World Testing Results URL is not a well formed URL.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(false);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setId(1l);
        updated.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        updated.setRwtResultsUrl("not a valid URL");
        updated.setRwtResultsCheckDate(LocalDate.parse("2022-01-08"));

        reviewer.review(updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Results URL is not a well formed URL."));
    }

    @Test
    public void review_noResultsSubmissionDate_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.realWorldTesting.results.checkDate.required"))
            .thenReturn("Real World Testing Results Check Date is required.");
        Mockito.when(validationUtils.isWellFormedUrl(ArgumentMatchers.anyString()))
                .thenReturn(true);

        CertifiedProductSearchDetails updated = new CertifiedProductSearchDetails();
        updated.setId(1l);
        updated.getCertificationEdition().put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        updated.setRwtResultsUrl("http://www.abc.com");
        updated.setRwtResultsCheckDate(null);

        reviewer.review(updated);

        assertEquals(1, updated.getErrorMessages().size());
        assertTrue(updated.getErrorMessages().contains("Real World Testing Results Check Date is required."));
    }
}
