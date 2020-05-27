package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TargetedUserDuplicateReviewer;

public class TargetedUserDuplicateReviewerTest {
    private static final String ERR_MSG =
            "Listing contains duplicate Targeted User: '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private TargetedUserDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateTargetedUser"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), ""));
        reviewer = new TargetedUserDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductTargetedUserDTO tu1 = getTargetedUser(1L, "TargetedUser1");
        PendingCertifiedProductTargetedUserDTO tu2 = getTargetedUser(1L, "TargetedUser1");
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
    public void review_duplicateExistsIdNull_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductTargetedUserDTO tu1 = getTargetedUser(null, "TargetedUser1");
        PendingCertifiedProductTargetedUserDTO tu2 = getTargetedUser(null, "TargetedUser1");
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
        PendingCertifiedProductTargetedUserDTO tu1 = getTargetedUser(1L, "TargetedUser1");
        PendingCertifiedProductTargetedUserDTO tu2 = getTargetedUser(2L, "TargetedUser2");
        listing.getTargetedUsers().add(tu1);
        listing.getTargetedUsers().add(tu2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getTargetedUsers().size());
    }

    @Test
    public void review_noDuplicatesIdNull_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductTargetedUserDTO tu1 = getTargetedUser(null, "TargetedUser1");
        PendingCertifiedProductTargetedUserDTO tu2 = getTargetedUser(null, "TargetedUser2");
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
        PendingCertifiedProductTargetedUserDTO tu1 = getTargetedUser(1L, "TargetedUser1");
        PendingCertifiedProductTargetedUserDTO tu2 = getTargetedUser(2L, "TargetedUser2");
        PendingCertifiedProductTargetedUserDTO tu3 = getTargetedUser(1L, "TargetedUser1");
        PendingCertifiedProductTargetedUserDTO tu4 = getTargetedUser(3L, "TargetedUser3");

        listing.getTargetedUsers().add(tu1);
        listing.getTargetedUsers().add(tu2);
        listing.getTargetedUsers().add(tu3);
        listing.getTargetedUsers().add(tu4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(3, listing.getTargetedUsers().size());
    }

    private PendingCertifiedProductTargetedUserDTO getTargetedUser(Long id, String name) {
        PendingCertifiedProductTargetedUserDTO tu = new PendingCertifiedProductTargetedUserDTO();
        tu.setId(id);
        tu.setName(name);
        return tu;
    }
}