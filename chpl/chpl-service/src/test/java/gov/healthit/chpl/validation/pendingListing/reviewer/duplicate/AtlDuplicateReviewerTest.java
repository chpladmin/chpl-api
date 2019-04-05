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

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTestingLabDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class AtlDuplicateReviewerTest {
    @Autowired
    private MessageSource messageSource;

    @Mock
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private AtlDuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        reviewer = new AtlDuplicateReviewer(msgUtil);

        //TODO - Can this be extracted as some sort of generic method, so it can be used all error messages??
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String message =
                        "Listing contains duplicate Testing Lab: '%s'.  The duplicates have been removed.";
                Object[] args = invocation.getArguments();
                return String.format(message, args[1]);
            }
        }).when(msgUtil).getMessage(ArgumentMatchers.eq("listing.duplicateTestingLab"),
                ArgumentMatchers.anyString());
    }

    @Test
    public void testDuplicateExists() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductTestingLabDTO atl1 = new PendingCertifiedProductTestingLabDTO();
        atl1.setTestingLabName("Atl1");

        PendingCertifiedProductTestingLabDTO atl2 = new PendingCertifiedProductTestingLabDTO();
        atl2.setTestingLabName("Atl1");

        listing.getTestingLabs().add(atl1);
        listing.getTestingLabs().add(atl2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getTestingLabs().size());
    }

    @Test
    public void testDuplicatesDoNotExist() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductTestingLabDTO atl1 = new PendingCertifiedProductTestingLabDTO();
        atl1.setTestingLabName("Atl1");

        PendingCertifiedProductTestingLabDTO atl2 = new PendingCertifiedProductTestingLabDTO();
        atl2.setTestingLabName("Atl2");

        listing.getTestingLabs().add(atl1);
        listing.getTestingLabs().add(atl2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getTestingLabs().size());
    }

    @Test
    public void testDuplicateExistInLargerSet() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductTestingLabDTO atl1 = new PendingCertifiedProductTestingLabDTO();
        atl1.setTestingLabName("Atl1");

        PendingCertifiedProductTestingLabDTO atl2 = new PendingCertifiedProductTestingLabDTO();
        atl2.setTestingLabName("Atl2");

        PendingCertifiedProductTestingLabDTO atl3 = new PendingCertifiedProductTestingLabDTO();
        atl3.setTestingLabName("Atl1");

        PendingCertifiedProductTestingLabDTO atl4 = new PendingCertifiedProductTestingLabDTO();
        atl4.setTestingLabName("Atl3");

        listing.getTestingLabs().add(atl1);
        listing.getTestingLabs().add(atl2);
        listing.getTestingLabs().add(atl3);
        listing.getTestingLabs().add(atl4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(3, listing.getTestingLabs().size());
    }
}