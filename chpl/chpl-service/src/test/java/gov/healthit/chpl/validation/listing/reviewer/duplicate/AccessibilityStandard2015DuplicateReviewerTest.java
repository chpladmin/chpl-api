package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class AccessibilityStandard2015DuplicateReviewerTest {
    private static final String ERR_MSG =
            "Listing contains duplicate Accessibility Standard: '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private AccessibilityStandardDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateAccessibilityStandard"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), ""));
        reviewer = new AccessibilityStandardDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertifiedProductAccessibilityStandard as1 = new CertifiedProductAccessibilityStandard();
        as1.setAccessibilityStandardName("AccessibilityStandard1");

        CertifiedProductAccessibilityStandard as2 = new CertifiedProductAccessibilityStandard();
        as2.setAccessibilityStandardName("AccessibilityStandard1");

        listing.getAccessibilityStandards().add(as1);
        listing.getAccessibilityStandards().add(as2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "AccessibilityStandard1")))
                .count());
        assertEquals(1, listing.getAccessibilityStandards().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertifiedProductAccessibilityStandard as1 = new CertifiedProductAccessibilityStandard();
        as1.setAccessibilityStandardName("AccessibilityStandard1");

        CertifiedProductAccessibilityStandard as2 = new CertifiedProductAccessibilityStandard();
        as2.setAccessibilityStandardName("AccessibilityStandard2");

        listing.getAccessibilityStandards().add(as1);
        listing.getAccessibilityStandards().add(as2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getAccessibilityStandards().size());
    }

    @Test
    public void review_emptyAccessibilityStandards_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getAccessibilityStandards().clear();

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getAccessibilityStandards().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertifiedProductAccessibilityStandard as1 = new CertifiedProductAccessibilityStandard();
        as1.setAccessibilityStandardName("AccessibilityStandard1");

        CertifiedProductAccessibilityStandard as2 = new CertifiedProductAccessibilityStandard();
        as2.setAccessibilityStandardName("AccessibilityStandard2");

        CertifiedProductAccessibilityStandard as3 = new CertifiedProductAccessibilityStandard();
        as3.setAccessibilityStandardName("AccessibilityStandard1");

        CertifiedProductAccessibilityStandard as4 = new CertifiedProductAccessibilityStandard();
        as4.setAccessibilityStandardName("AccessibilityStandard3");

        listing.getAccessibilityStandards().add(as1);
        listing.getAccessibilityStandards().add(as2);
        listing.getAccessibilityStandards().add(as3);
        listing.getAccessibilityStandards().add(as4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "AccessibilityStandard1")))
                .count());
        assertEquals(3, listing.getAccessibilityStandards().size());
    }
}