package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.ChplProductNumberDAO;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class EditionCodeNotEditionlessReviewerTest {
    private static final String INVALID_EDITION_CODE = "The edition code %s is not one of the allowed edition codes %s.";
    private static final String MISMATCHED_CERT_EDITION = "The edition code from the listing %s does not match the certification edition of the listing %s.";
    private static final String ALLOWED_EDITION_CODES = "15";

    private ErrorMessageUtil errorMessageUtil;
    private EditionCodeReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.certificationEditionMismatch"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISMATCHED_CERT_EDITION, i.getArgument(1), i.getArgument(2)));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.invalidEditionCode"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_EDITION_CODE, i.getArgument(1), i.getArgument(2)));

        FF4j ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(ArgumentMatchers.eq(FeatureList.EDITIONLESS))).thenReturn(false);
        ChplProductNumberUtil chplProductNumberUtil = new ChplProductNumberUtil(
                Mockito.mock(CertifiedProductSearchResultDAO.class),
                Mockito.mock(ChplProductNumberDAO.class),
                ff4j);
        reviewer = new EditionCodeReviewer(chplProductNumberUtil, new ValidationUtils(), errorMessageUtil);
    }

    @Test
    public void review_nullEditionValidCode_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .edition(null)
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISMATCHED_CERT_EDITION, "15", "<none>")));
    }

    @Test
    public void review_nullEditionInvalidCode_hasErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("22.04.04.2526.WEBe.06.00.1.210101")
                .edition(null)
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_EDITION_CODE, "22", ALLOWED_EDITION_CODES)));
    }

    @Test
    public void review_nullEditionEmptyCode_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(".04.04.2526.WEBe.06.00.1.210101")
                .edition(null)
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISMATCHED_CERT_EDITION, "", "<none>")));
    }

    @Test
    public void review_emptyEditionValidCode_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .edition(CertificationEdition.builder().name("").build())
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISMATCHED_CERT_EDITION, "15", "<none>")));
    }

    @Test
    public void review_legacyChplProductNumberWithEdition_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .edition(CertificationEdition.builder().name("2014").build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_legacyChplProductNumberNullEdition_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .edition(null)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_mismatchedEditionAndCode_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .edition(CertificationEdition.builder().name("2014").build())
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISMATCHED_CERT_EDITION, "15", "2014")));
    }

    @Test
    public void review_mismatchedEditionInvalidChplProductNumber_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("ED.04.04.2526.WEBe.06.00.1.210101")
                .edition(CertificationEdition.builder().name("2014").build())
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_EDITION_CODE, "ED", ALLOWED_EDITION_CODES)));
    }

    @Test
    public void review_invalidCodeNullEdition_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("11.04.04.2526.WEBe.06.00.1.210101")
                .edition(null)
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_EDITION_CODE, "11", ALLOWED_EDITION_CODES)));
    }

    @Test
    public void review_goodCertEditionCodeWithMatchingListingEdition_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .edition(CertificationEdition.builder().name("2015").build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }
}
