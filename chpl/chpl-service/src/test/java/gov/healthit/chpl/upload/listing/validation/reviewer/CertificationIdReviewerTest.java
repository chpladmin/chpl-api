package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class CertificationIdReviewerTest {
    private static final String MISSING_CERTIFICATION_ID = "CHPL certification ID was not found.";

    private ErrorMessageUtil errorMessageUtil;
    private CertificationIdReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.certificationIdMissing")))
            .thenReturn(MISSING_CERTIFICATION_ID);
        reviewer = new CertificationIdReviewer(errorMessageUtil);
    }

    @Test
    public void review_nullCertificationId_hasWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .acbCertificationId(null)
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_CERTIFICATION_ID));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyCertificationId_hasWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .acbCertificationId("")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_CERTIFICATION_ID));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_blankCertificationId_hasWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .acbCertificationId("    ")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(MISSING_CERTIFICATION_ID));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_presentCertificationId_noWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .acbCertificationId("something")
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }
}
