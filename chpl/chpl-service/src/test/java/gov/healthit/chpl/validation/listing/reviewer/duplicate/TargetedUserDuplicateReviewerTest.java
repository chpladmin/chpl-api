package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.util.ErrorMessageUtil;

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
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertifiedProductTargetedUser tu1 = new CertifiedProductTargetedUser();
        tu1.setTargetedUserName("TargetedUser1");

        CertifiedProductTargetedUser tu2 = new CertifiedProductTargetedUser();
        tu2.setTargetedUserName("TargetedUser1");

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
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertifiedProductTargetedUser tu1 = new CertifiedProductTargetedUser();
        tu1.setTargetedUserName("TargetedUser1");

        CertifiedProductTargetedUser tu2 = new CertifiedProductTargetedUser();
        tu2.setTargetedUserName("TargetedUser2");

        listing.getTargetedUsers().add(tu1);
        listing.getTargetedUsers().add(tu2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getTargetedUsers().size());
    }

    @Test
    public void review_emptyTargetedUsers_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getTargetedUsers().clear();

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getTargetedUsers().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertifiedProductTargetedUser tu1 = new CertifiedProductTargetedUser();
        tu1.setTargetedUserName("AccessibilityStandard1");

        CertifiedProductTargetedUser tu2 = new CertifiedProductTargetedUser();
        tu2.setTargetedUserName("AccessibilityStandard2");

        CertifiedProductTargetedUser tu3 = new CertifiedProductTargetedUser();
        tu3.setTargetedUserName("AccessibilityStandard1");

        CertifiedProductTargetedUser tu4 = new CertifiedProductTargetedUser();
        tu4.setTargetedUserName("AccessibilityStandard3");

        listing.getTargetedUsers().add(tu1);
        listing.getTargetedUsers().add(tu2);
        listing.getTargetedUsers().add(tu3);
        listing.getTargetedUsers().add(tu4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(3, listing.getTargetedUsers().size());
    }
}