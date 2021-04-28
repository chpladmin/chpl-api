package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class AccessibilityStandardReviewerTest {
    private static final String MISSING_ACCSTDS = "Accessibility Standards are required.";
    private static final String MISSING_NAME = "A name is required for each Accessibility Standard listed.";
    private static final String FUZZY_MATCH_REPLACEMENT = "The %s value was changed from %s to %s.";

    private ErrorMessageUtil errorMessageUtil;
    private AccessibilityStandardReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.accessibilityStandardsNotFound")))
            .thenReturn(MISSING_ACCSTDS);
        Mockito.when(errorMessageUtil.getMessage("listing.accessibilityStandardMissingName"))
            .thenReturn(MISSING_NAME);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.fuzzyMatch"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(FUZZY_MATCH_REPLACEMENT, i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        reviewer = new AccessibilityStandardReviewer(errorMessageUtil);
    }

    @Test
    public void review_nullAccessibilityStandards_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setAccessibilityStandards(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ACCSTDS));
    }

    @Test
    public void review_emptyAccessibilityStandards_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandards(new ArrayList<CertifiedProductAccessibilityStandard>())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ACCSTDS));
    }

    @Test
    public void review_hasNullAccessibilityStandardName_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandard(CertifiedProductAccessibilityStandard.builder()
                        .id(1L)
                        .accessibilityStandardName(null)
                        .accessibilityStandardId(null)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_NAME));
    }

    @Test
    public void review_hasEmptyAccessibilityStandardName_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandard(CertifiedProductAccessibilityStandard.builder()
                        .id(1L)
                        .accessibilityStandardName("")
                        .accessibilityStandardId(null)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_NAME));
    }

    @Test
    public void review_hasAccessibilityStandardName_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandard(CertifiedProductAccessibilityStandard.builder()
                        .id(1L)
                        .accessibilityStandardName("test")
                        .accessibilityStandardId(null)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_hasAccessibilityStandardNameAndId_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandard(CertifiedProductAccessibilityStandard.builder()
                        .id(1L)
                        .accessibilityStandardName("test")
                        .accessibilityStandardId(1L)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_hasAccessibilityStandardNameNullIdFindsFuzzyMatch_hasWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandard(CertifiedProductAccessibilityStandard.builder()
                        .accessibilityStandardName("test")
                        .accessibilityStandardId(null)
                        .userEnteredAccessibilityStandardName("tst")
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(FUZZY_MATCH_REPLACEMENT, FuzzyType.ACCESSIBILITY_STANDARD.fuzzyType(), "tst", "test")));
    }
}
