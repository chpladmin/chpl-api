package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.AccessibilityStandard2015DuplicateReviewer;

public class AccessibilityStandard2015DuplicateReviewerTest {
    private static final String ERR_MSG =
            "Listing contains duplicate Accessibility Standard: '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private AccessibilityStandard2015DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateAccessibilityStandard.2015"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), ""));
        reviewer = new AccessibilityStandard2015DuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductAccessibilityStandardDTO as1 = new PendingCertifiedProductAccessibilityStandardDTO();
        as1.setName("AccessibilityStandard1");

        PendingCertifiedProductAccessibilityStandardDTO as2 = new PendingCertifiedProductAccessibilityStandardDTO();
        as2.setName("AccessibilityStandard1");

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
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductAccessibilityStandardDTO as1 = new PendingCertifiedProductAccessibilityStandardDTO();
        as1.setName("AccessibilityStandard1");

        PendingCertifiedProductAccessibilityStandardDTO as2 = new PendingCertifiedProductAccessibilityStandardDTO();
        as2.setName("AccessibilityStandard2");

        listing.getAccessibilityStandards().add(as1);
        listing.getAccessibilityStandards().add(as2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getAccessibilityStandards().size());
    }

    @Test
    public void review_emptyAccessibilityStandards_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        listing.getAccessibilityStandards().clear();

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getAccessibilityStandards().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductAccessibilityStandardDTO as1 = new PendingCertifiedProductAccessibilityStandardDTO();
        as1.setName("AccessibilityStandard1");

        PendingCertifiedProductAccessibilityStandardDTO as2 = new PendingCertifiedProductAccessibilityStandardDTO();
        as2.setName("AccessibilityStandard2");

        PendingCertifiedProductAccessibilityStandardDTO as3 = new PendingCertifiedProductAccessibilityStandardDTO();
        as3.setName("AccessibilityStandard1");

        PendingCertifiedProductAccessibilityStandardDTO as4 = new PendingCertifiedProductAccessibilityStandardDTO();
        as4.setName("AccessibilityStandard3");

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