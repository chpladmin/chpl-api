package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

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

import gov.healthit.chpl.dto.PendingCertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.AccessibilityStandard2015DuplicateReviewer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class AccessibilityStandard2015DuplicateReviewerTest {
    @Autowired
    private MessageSource messageSource;

    @Mock
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private AccessibilityStandard2015DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        reviewer = new AccessibilityStandard2015DuplicateReviewer(msgUtil);

        //TODO - Can this be extracted as some sort of generic method, so it can be used all error messages??
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String message =
                        "Listing contains duplicate Accessibility Standard: '%s'.  The duplicates have been removed.";
                Object[] args = invocation.getArguments();
                return String.format(message, args[1]);
            }
        }).when(msgUtil).getMessage(ArgumentMatchers.eq("listing.duplicateAccessibilityStandard.2015"),
                ArgumentMatchers.anyString());
    }

    @Test
    public void testDuplicateExists() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductAccessibilityStandardDTO as1 = new PendingCertifiedProductAccessibilityStandardDTO();
        as1.setName("AccessibilityStandard1");

        PendingCertifiedProductAccessibilityStandardDTO as2 = new PendingCertifiedProductAccessibilityStandardDTO();
        as2.setName("AccessibilityStandard1");

        listing.getAccessibilityStandards().add(as1);
        listing.getAccessibilityStandards().add(as2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getAccessibilityStandards().size());
    }

    @Test
    public void testDuplicatesDoNotExist() {
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
    public void testDuplicateExistInLargerSet() {
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
        assertEquals(3, listing.getAccessibilityStandards().size());
    }
}