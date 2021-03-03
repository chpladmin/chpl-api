package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class CertificationDateReviewerTest {
    private static final String MISSING_CERT_DATE = "No certification date was found.";
    private static final String BAD_CERT_DATE = "Certification date %s is not in the format yyyymmdd.";
    private static final String FUTURE_CERT_DATE = "Certification date occurs in the future.";

    private ErrorMessageUtil errorMessageUtil;
    private CertificationDateReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        reviewer = new CertificationDateReviewer(errorMessageUtil);
    }

    @Test
    public void review_nullCertDateAndNullCertDateString_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.certificationDateMissing"))
                .thenReturn(MISSING_CERT_DATE);

        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setCertificationDate(null);
        listing.setCertificationDateStr(null);

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CERT_DATE));
    }

    @Test
    public void review_nullCertDateAndEmptyCertDateString_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage("listing.certificationDateMissing"))
                .thenReturn(MISSING_CERT_DATE);

        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setCertificationDate(null);
        listing.setCertificationDateStr("");

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_CERT_DATE));
    }

    @Test
    public void review_nullCertDateWithCertDateString_errorMessage() throws ParseException {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.badCertificationDate"), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(BAD_CERT_DATE, i.getArgument(1), ""));

        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setCertificationDate(null);
        listing.setCertificationDateStr("baddate");

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

        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setCertificationDate(tomorrow.getTimeInMillis());
        listing.setCertificationDateStr("150101");

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

        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setCertificationDate(tomorrow.getTimeInMillis());
        listing.setCertificationDateStr(null);

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(FUTURE_CERT_DATE));
    }

    @Test
    public void review_goodCertDateWithCertDateString_noError() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        listing.setCertificationDate(yesterday.getTimeInMillis());
        listing.setCertificationDateStr("150101");

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_goodCertDateWithNullDateString_noError() throws ParseException {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        listing.setCertificationDate(yesterday.getTimeInMillis());
        listing.setCertificationDateStr(null);

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }
}
