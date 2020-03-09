package old.gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.TargetedUser2015DuplicateReviewer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { old.gov.healthit.chpl.CHPLTestConfig.class })
public class TargetedUser2015DuplicateReviewerTest {
    @Autowired
    private MessageSource messageSource;

    @Mock
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private TargetedUser2015DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        reviewer = new TargetedUser2015DuplicateReviewer(msgUtil);

        //TODO - Can this be extracted as some sort of generic method, so it can be used all error messages??
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String message =
                        "Listing contains duplicate Targeted User: '%s'.  The duplicates have been removed.";
                Object[] args = invocation.getArguments();
                return String.format(message, args[1]);
            }
        }).when(msgUtil).getMessage(ArgumentMatchers.eq("listing.duplicateTargetedUser.2015"),
                ArgumentMatchers.anyString());
    }

    @Test
    public void testDuplicateExists() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductTargetedUserDTO tu1 = new PendingCertifiedProductTargetedUserDTO();
        tu1.setName("TargetedUser1");

        PendingCertifiedProductTargetedUserDTO tu2 = new PendingCertifiedProductTargetedUserDTO();
        tu2.setName("TargetedUser1");

        listing.getTargetedUsers().add(tu1);
        listing.getTargetedUsers().add(tu2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getTargetedUsers().size());
    }

    @Test
    public void testDuplicatesDoNotExist() {
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
    public void testDuplicateExistInLargerSet() {
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
