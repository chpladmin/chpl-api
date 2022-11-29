package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
    private static final String NOT_FOUND_AND_REMOVED = "The accessibility standard '%s' was not found in the system and has been removed from the listing.";

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
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.accessibilityStandardNotFoundAndRemoved"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(NOT_FOUND_AND_REMOVED, i.getArgument(1), ""));

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
    public void review_hasNullAccessibilityStandardName_hasWarningAndIsRemoved() {
        List<CertifiedProductAccessibilityStandard> stds = new ArrayList<CertifiedProductAccessibilityStandard>();
        stds.add(CertifiedProductAccessibilityStandard.builder()
                        .id(1L)
                        .accessibilityStandardName(null)
                        .accessibilityStandardId(null)
                        .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandards(stds)
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(NOT_FOUND_AND_REMOVED, "")));
        assertEquals(0, listing.getAccessibilityStandards().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ACCSTDS));
    }

    @Test
    public void review_hasEmptyAccessibilityStandardName_hasWarningAndIsRemoved() {
        List<CertifiedProductAccessibilityStandard> stds = new ArrayList<CertifiedProductAccessibilityStandard>();
        stds.add(CertifiedProductAccessibilityStandard.builder()
                        .id(1L)
                        .accessibilityStandardName("")
                        .accessibilityStandardId(null)
                        .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandards(stds)
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(NOT_FOUND_AND_REMOVED, "")));
        assertEquals(0, listing.getAccessibilityStandards().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ACCSTDS));
    }

    @Test
    public void review_hasAccessibilityStandardNameNoId_hasWarningAndIsRemoved() {
        List<CertifiedProductAccessibilityStandard> stds = new ArrayList<CertifiedProductAccessibilityStandard>();
        stds.add(CertifiedProductAccessibilityStandard.builder()
                        .id(1L)
                        .accessibilityStandardName("test")
                        .accessibilityStandardId(null)
                        .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandards(stds)
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(NOT_FOUND_AND_REMOVED, "test")));
        assertEquals(0, listing.getAccessibilityStandards().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ACCSTDS));
    }

    @Test
    public void review_hasAccessibilityStandardNameAndId_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandards(Stream.of(CertifiedProductAccessibilityStandard.builder()
                        .id(1L)
                        .accessibilityStandardName("test")
                        .accessibilityStandardId(1L)
                        .build()).toList())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getAccessibilityStandards().size());
    }

    @Test
    public void review_hasAccessibilityStandardNameNullIdFindsFuzzyMatch_hasWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .accessibilityStandards(Stream.of(CertifiedProductAccessibilityStandard.builder()
                        .accessibilityStandardName("test")
                        .accessibilityStandardId(1L)
                        .userEnteredAccessibilityStandardName("tst")
                        .build()).toList())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(FUZZY_MATCH_REPLACEMENT, FuzzyType.ACCESSIBILITY_STANDARD.fuzzyType(), "tst", "test")));
        assertEquals(1, listing.getAccessibilityStandards().size());
    }
}
