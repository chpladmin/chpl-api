package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.AccessibilityStandardDuplicateReviewer;

public class AccessibilityStandardDuplicateReviewerTest {
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
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductAccessibilityStandardDTO as1 = getAccessibilityStandard(1L, "AS1");
        PendingCertifiedProductAccessibilityStandardDTO as2 = getAccessibilityStandard(1L, "AS1");
        listing.getAccessibilityStandards().add(as1);
        listing.getAccessibilityStandards().add(as2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "AS1")))
                .count());
        assertEquals(1, listing.getAccessibilityStandards().size());
    }

    @Test
    public void review_duplicateExistsIdNull_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductAccessibilityStandardDTO as1 = getAccessibilityStandard(null, "AS1");
        PendingCertifiedProductAccessibilityStandardDTO as2 = getAccessibilityStandard(null, "AS1");
        listing.getAccessibilityStandards().add(as1);
        listing.getAccessibilityStandards().add(as2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "AS1")))
                .count());
        assertEquals(1, listing.getAccessibilityStandards().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductAccessibilityStandardDTO as1 = getAccessibilityStandard(1L, "AS1");
        PendingCertifiedProductAccessibilityStandardDTO as2 = getAccessibilityStandard(2L, "AS2");

        listing.getAccessibilityStandards().add(as1);
        listing.getAccessibilityStandards().add(as2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getAccessibilityStandards().size());
    }

    @Test
    public void review_noDuplicatesIdNull_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductAccessibilityStandardDTO as1 = getAccessibilityStandard(null, "AS1");
        PendingCertifiedProductAccessibilityStandardDTO as2 = getAccessibilityStandard(null, "AS2");

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
        PendingCertifiedProductAccessibilityStandardDTO as1 = getAccessibilityStandard(1L, "AS1");
        PendingCertifiedProductAccessibilityStandardDTO as2 = getAccessibilityStandard(2L, "AS2");
        PendingCertifiedProductAccessibilityStandardDTO as3 = getAccessibilityStandard(1L, "AS1");
        PendingCertifiedProductAccessibilityStandardDTO as4 = getAccessibilityStandard(3L, "AS3");

        listing.getAccessibilityStandards().add(as1);
        listing.getAccessibilityStandards().add(as2);
        listing.getAccessibilityStandards().add(as3);
        listing.getAccessibilityStandards().add(as4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "AS1")))
                .count());
        assertEquals(3, listing.getAccessibilityStandards().size());
    }

    private PendingCertifiedProductAccessibilityStandardDTO getAccessibilityStandard(Long id, String name) {
        PendingCertifiedProductAccessibilityStandardDTO as = new PendingCertifiedProductAccessibilityStandardDTO();
        as.setId(id);
        as.setName(name);
        return as;
    }
}