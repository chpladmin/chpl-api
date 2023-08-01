package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationEdition;
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
                .edition(null)
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_EDITION));
    }

    @Test
    public void review_editionObjectWithNullValues_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(CertificationEdition.builder().id(null).name(null).build())
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_EDITION));
    }

    @Test
    public void review_editionObjectWithEmptyYearValidId_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(CertificationEdition.builder().id(2L).name("").build())
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_YEAR));
    }

    @Test
    public void review_editionObjectWithNullYearValidId_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(CertificationEdition.builder().id(2L).build())
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_YEAR));
    }

    @Test
    public void review_editionObjectWithValidYearMissingIdKey_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(CertificationEdition.builder().name("2015").build())
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_EDITION, "2015", "")));
    }

    @Test
    public void review_editionObjectWithValidYearAndId_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(CertificationEdition.builder().id(3L).name("2015").build())
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }
}
