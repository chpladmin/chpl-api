package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class CertificationDateComparisonReviewerTest {
    private static final long SEPT_14_2021_IN_MILLIS = 1631643578205L;
    private static final long AUG_31_2021_IN_MILLIS = 1630454400000L;
    private static final String MISSING_CERTIFICATION_DATE = "No certification date was found.";
    private static final String CHANGED_CERTIFICATION_DATE = "The certification date was changed from %s to %s. This field is read-only and must instead be changed by modifying the certificationEvents field.";
    private ErrorMessageUtil msgUtil;

    private CertificationDateComparisonReviewer reviewer;

    @Before
    public void before() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.certificationDateMissing")))
            .thenReturn(MISSING_CERTIFICATION_DATE);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.certificationDateChanged"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CHANGED_CERTIFICATION_DATE, i.getArgument(1), i.getArgument(2)));
        reviewer = new CertificationDateComparisonReviewer(msgUtil);
    }

    @Test
    public void review_nullCertificationDates_hasError() {
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationDate(null)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationDate(null)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(1, updatedListing.getErrorMessages().size());
        assertTrue(updatedListing.getErrorMessages().contains(MISSING_CERTIFICATION_DATE));
    }

    @Test
    public void review_nullOriginalListingCertificationDate_hasError() {
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationDate(null)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationDate(SEPT_14_2021_IN_MILLIS)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(1, updatedListing.getErrorMessages().size());
        assertTrue(updatedListing.getErrorMessages().contains(MISSING_CERTIFICATION_DATE));
    }

    @Test
    public void review_nullUpdatedListingCertificationDate_hasError() {
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationDate(SEPT_14_2021_IN_MILLIS)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationDate(null)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(1, updatedListing.getErrorMessages().size());
        assertTrue(updatedListing.getErrorMessages().contains(MISSING_CERTIFICATION_DATE));
    }

    @Test
    public void review_sameMillisCertificationDates_noError() {
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationDate(SEPT_14_2021_IN_MILLIS)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationDate(SEPT_14_2021_IN_MILLIS)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_differentMillisSameDayCertificationDates_noError() {
        long sept142021LaterInMillis = 1631643653214L;

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationDate(SEPT_14_2021_IN_MILLIS)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationDate(sept142021LaterInMillis)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_differentDayCertificationDates_hasError() {
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .certificationDate(SEPT_14_2021_IN_MILLIS)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .certificationDate(AUG_31_2021_IN_MILLIS)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(1, updatedListing.getErrorMessages().size());
        assertTrue(updatedListing.getErrorMessages().contains(String.format(CHANGED_CERTIFICATION_DATE, "September 14, 2021 ET", "August 31, 2021 ET")));
    }
}
