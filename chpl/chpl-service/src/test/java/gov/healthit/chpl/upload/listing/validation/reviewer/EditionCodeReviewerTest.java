package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class EditionCodeReviewerTest {
    private static final String INVALID_EDITION_CODE = "The edition code %s is not one of the allowed edition codes %s.";
    private static final String MISMATCHED_CERT_EDITION = "The edition code from the listing %s does not match the certification edition of the listing %s.";

    private ErrorMessageUtil errorMessageUtil;
    private EditionCodeReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        reviewer = new EditionCodeReviewer(new ChplProductNumberUtil(), new ValidationUtils(), errorMessageUtil);
    }

    @Test
    public void review_nullEditionValidCode_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .certificationEdition(null)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_nullEditionEmptyCode_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber(".04.04.2526.WEBe.06.00.1.210101")
                .certificationEdition(null)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }


    @Test
    public void review_emptyEditionValidCode_noError() {
        Map<String, Object> certEditionMap = new HashMap<String, Object>();
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .certificationEdition(certEditionMap)
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_legacyChplProductNumberWithEdition_noError() {
        Map<String, Object> certEditionMap = new HashMap<String, Object>();
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2014");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .certificationEdition(certEditionMap)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_legacyChplProductNumberNullEdition_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .certificationEdition(null)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_mismatchedEdition_hasError() {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.certificationEditionMismatch"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISMATCHED_CERT_EDITION, i.getArgument(1), i.getArgument(2)));

        Map<String, Object> certEditionMap = new HashMap<String, Object>();
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2014");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .certificationEdition(certEditionMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISMATCHED_CERT_EDITION, "15", "2014")));
    }

    @Test
    public void review_mismatchedEditionInvalidChplProductNumber_noError() {
        Map<String, Object> certEditionMap = new HashMap<String, Object>();
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2014");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("ED.04.04.2526.WEBe.06.00.1.210101")
                .certificationEdition(certEditionMap)
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_invalidCodeNullEdition_hasError() {
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.invalidEditionCode"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_EDITION_CODE, i.getArgument(1), i.getArgument(2)));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("11.04.04.2526.WEBe.06.00.1.210101")
                .certificationEdition(null)
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_EDITION_CODE, "11", "15")));
    }

    @Test
    public void review_goodCertEditionCodeWithMatchingListingEdition_noError() {
        Map<String, Object> certEditionMap = new HashMap<String, Object>();
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.00.1.210101")
                .certificationEdition(certEditionMap)
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }
}
