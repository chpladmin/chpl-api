package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.TargetedUser2015DuplicateReviewer;

public class TargetedUser2015DuplicateReviewerTest {
    private static final String ERR_MSG =
            "Listing contains duplicate Targeted User: '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private TargetedUser2015DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateTargetedUser.2015"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), ""));
        reviewer = new TargetedUser2015DuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductTargetedUserDTO tu1 = new PendingCertifiedProductTargetedUserDTO();
        tu1.setName("TargetedUser1");

        PendingCertifiedProductTargetedUserDTO tu2 = new PendingCertifiedProductTargetedUserDTO();
        tu2.setName("TargetedUser1");

        listing.getTargetedUsers().add(tu1);
        listing.getTargetedUsers().add(tu2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "TargetedUser1")))
                .count());
        assertEquals(1, listing.getTargetedUsers().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductTargetedUserDTO tu1 = new PendingCertifiedProductTargetedUserDTO();
        tu1.setName("TargetedUser1");

        PendingCertifiedProductTargetedUserDTO tu2 = new PendingCertifiedProductTargetedUserDTO();
        tu2.setName("TargetedUser2");

        listing.getTargetedUsers().add(tu1);
        listing.getTargetedUsers().add(tu2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getTargetedUsers().size());
    }

    @Test
    public void review_emptyTargetedUsers_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        listing.getTargetedUsers().clear();

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getTargetedUsers().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductTargetedUserDTO tu1 = new PendingCertifiedProductTargetedUserDTO();
        tu1.setName("AccessibilityStandard1");

        PendingCertifiedProductTargetedUserDTO tu2 = new PendingCertifiedProductTargetedUserDTO();
        tu2.setName("AccessibilityStandard2");

        PendingCertifiedProductTargetedUserDTO tu3 = new PendingCertifiedProductTargetedUserDTO();
        tu3.setName("AccessibilityStandard1");

        PendingCertifiedProductTargetedUserDTO tu4 = new PendingCertifiedProductTargetedUserDTO();
        tu4.setName("AccessibilityStandard3");

        listing.getTargetedUsers().add(tu1);
        listing.getTargetedUsers().add(tu2);
        listing.getTargetedUsers().add(tu3);
        listing.getTargetedUsers().add(tu4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(3, listing.getTargetedUsers().size());
    }
}