package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.util.Calendar;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadHeadingUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class CertificationDateReviewerTest {
    private static final String MISSING_CERT_DATE = "No certification date was found.";
    private static final String BAD_CERT_DATE = "Certification date %s is not in the format yyyymmdd.";
    private static final String FUTURE_CERT_DATE = "Certification date occurs in the future.";

    private ListingUploadHandlerUtil uploadUtil;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationDateReviewer reviewer;

    @Before
    public void setup() {
        CertificationCriterionService criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.getAllowedCriterionHeadingsForNewListing())
            .thenReturn(Stream.of("CRITERIA_170_315_A_1__C").toList());
        ListingUploadHeadingUtil uploadHeadingUtil = new ListingUploadHeadingUtil(criteriaService);

        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        uploadUtil = new ListingUploadHandlerUtil(uploadHeadingUtil, errorMessageUtil);
        reviewer = new CertificationDateReviewer(uploadUtil, errorMessageUtil);
    }

    @Test
    public void review_nullCertDateAndNullCertDateString_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.certificationDateMissing"))
                .thenReturn(MISSING_CERT_DATE);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(null)
                .certificationDateStr(null)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CERT_DATE));
    }

    @Test
    public void review_nullCertDateAndEmptyCertDateString_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.certificationDateMissing"))
                .thenReturn(MISSING_CERT_DATE);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(null)
                .certificationDateStr("")
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CERT_DATE));
    }

    @Test
    public void review_nullCertDateWithBadCertDateString_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badCertificationDate"), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(BAD_CERT_DATE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(null)
                .certificationDateStr("baddate")
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_CERT_DATE, "baddate")));
    }

    @Test
    public void review_certDateExistsWithBadCertDateString_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badCertificationDate"), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(BAD_CERT_DATE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(System.currentTimeMillis())
                .certificationDateStr("baddate")
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_CERT_DATE, "baddate")));
    }

    @Test
    public void review_futureCertDateWithCertDateString_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.futureCertificationDate"))
        .thenReturn(FUTURE_CERT_DATE);

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(tomorrow.getTimeInMillis())
                .certificationDateStr("20150101")
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(FUTURE_CERT_DATE));
    }

    @Test
    public void review_futureCertDateWithNullDateString_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.futureCertificationDate"))
        .thenReturn(FUTURE_CERT_DATE);

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(tomorrow.getTimeInMillis())
                .certificationDateStr(null)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(FUTURE_CERT_DATE));
    }

    @Test
    public void review_goodCertDateWithCertDateString_noError() throws ParseException {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(yesterday.getTimeInMillis())
                .certificationDateStr("20150101")
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_goodCertDateWithNullDateString_noError() throws ParseException {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(yesterday.getTimeInMillis())
                .certificationDateStr(null)
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }
}
