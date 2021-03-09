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
import gov.healthit.chpl.util.ErrorMessageUtil;

public class EditionReviewerTest {
    private static final String MISSING_EDITION = "A certification edition is required for the listing.";
    private static final String MISSING_YEAR = "A certification edition was found but is missing the year.";
    private static final String INVALID_EDITION = "The certification edition %s is not valid.";

    private ErrorMessageUtil errorMessageUtil;
    private EditionReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(errorMessageUtil.getMessage("listing.missingEdition"))
        .thenReturn(MISSING_EDITION);

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.missingEditionYear")))
        .thenReturn(MISSING_YEAR);

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.invalidEdition"), ArgumentMatchers.anyString()))
        .thenAnswer(i -> String.format(INVALID_EDITION, i.getArgument(1), ""));

        reviewer = new EditionReviewer(errorMessageUtil);
    }

    @Test
    public void review_nullEdition_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(null)
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_EDITION));
    }

    @Test
    public void review_editionObjectWithEmptyValues_hasError() {

        Map<String, Object> certEditionMap = new HashMap<String, Object>();
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "");
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, "");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(certEditionMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_YEAR));
    }

    @Test
    public void review_editionObjectWithNoNameKey_hasError() {
        Map<String, Object> certEditionMap = new HashMap<String, Object>();
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, "");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(certEditionMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_YEAR));
    }

    @Test
    public void review_editionObjectWithNullValues_hasError() {
        Map<String, Object> certEditionMap = new HashMap<String, Object>();
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, null);
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, null);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(certEditionMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_YEAR));
    }

    @Test
    public void review_editionObjectWithEmptyYearValidId_hasError() {
        Map<String, Object> certEditionMap = new HashMap<String, Object>();
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "");
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 2L);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(certEditionMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_YEAR));
    }

    @Test
    public void review_editionObjectWithNullYearValidId_hasError() {
        Map<String, Object> certEditionMap = new HashMap<String, Object>();
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, null);
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 2L);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(certEditionMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_YEAR));
    }

    @Test
    public void review_editionObjectWithValidYearEmptyId_hasError() {
        Map<String, Object> certEditionMap = new HashMap<String, Object>();
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, "");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(certEditionMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_EDITION, "2015", "")));
    }

    @Test
    public void review_editionObjectWithValidYearNullId_hasError() {
        Map<String, Object> certEditionMap = new HashMap<String, Object>();
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, null);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(certEditionMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_EDITION, "2015", "")));
    }

    @Test
    public void review_editionObjectWithValidYearMissingIdKey_hasError() {
        Map<String, Object> certEditionMap = new HashMap<String, Object>();
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(certEditionMap)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_EDITION, "2015", "")));
    }

    @Test
    public void review_editionObjectWithValidYearAndId_noErrors() {
        Map<String, Object> certEditionMap = new HashMap<String, Object>();
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        certEditionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, "3");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(certEditionMap)
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }
}
