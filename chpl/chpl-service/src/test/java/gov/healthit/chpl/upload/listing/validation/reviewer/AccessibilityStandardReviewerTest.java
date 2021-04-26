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
import gov.healthit.chpl.util.ErrorMessageUtil;

public class AccessibilityStandardReviewerTest {
    private static final String MISSING_ACCSTDS = "Accessibility Standards are required.";
    private static final String MISSING_NAME = "A name is required for each Accessibility Standard listed.";

    private ErrorMessageUtil errorMessageUtil;
    private AccessibilityStandardReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.accessibilityStandardsNotFound")))
            .thenReturn(MISSING_ACCSTDS);
        Mockito.when(errorMessageUtil.getMessage("listing.accessibilityStandardMissingName"))
            .thenReturn(MISSING_NAME);
        reviewer = new AccessibilityStandardReviewer(errorMessageUtil);
    }

    @Test
    public void review_nullAccessibilityStandards_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setAccessibilityStandards(null);
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ACCSTDS));
    }

    @Test
    public void review_emptyAccessibilityStandards_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandards(new ArrayList<CertifiedProductAccessibilityStandard>())
                .build();
        reviewer.review(listing);

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
    }
}
