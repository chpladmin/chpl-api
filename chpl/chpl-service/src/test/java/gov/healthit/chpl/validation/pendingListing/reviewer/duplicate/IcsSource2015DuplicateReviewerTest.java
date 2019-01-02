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

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.IcsSource2015DuplicateReviewer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class IcsSource2015DuplicateReviewerTest {
    @Autowired
    private MessageSource messageSource;

    @Mock
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private IcsSource2015DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        reviewer = new IcsSource2015DuplicateReviewer(msgUtil);

        //TODO - Can this be extracted as some sort of generic method, so it can be used all error messages??
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String message =
                        "Listing contains duplicate ICS Source: '%s'.  The duplicates have been removed.";
                Object[] args = invocation.getArguments();
                return String.format(message, args[1]);
            }
        }).when(msgUtil).getMessage(ArgumentMatchers.eq("listing.duplicateIcsSource.2015"),
                ArgumentMatchers.anyString());
    }

    @Test
    public void testDuplicateExists() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        CertifiedProductDetailsDTO ics1 = new CertifiedProductDetailsDTO();
        ics1.setChplProductNumber("Chpl1");

        CertifiedProductDetailsDTO ics2 = new CertifiedProductDetailsDTO();
        ics2.setChplProductNumber("Chpl1");

        listing.getIcsParents().add(ics1);
        listing.getIcsParents().add(ics2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getIcsParents().size());
    }

    @Test
    public void testDuplicatesDoNotExist() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        CertifiedProductDetailsDTO ics1 = new CertifiedProductDetailsDTO();
        ics1.setChplProductNumber("Chpl1");

        CertifiedProductDetailsDTO ics2 = new CertifiedProductDetailsDTO();
        ics2.setChplProductNumber("Chpl2");

        listing.getIcsParents().add(ics1);
        listing.getIcsParents().add(ics2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getIcsParents().size());
    }

    @Test
    public void testDuplicateExistInLargerSet() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        CertifiedProductDetailsDTO ics1 = new CertifiedProductDetailsDTO();
        ics1.setChplProductNumber("Chpl1");

        CertifiedProductDetailsDTO ics2 = new CertifiedProductDetailsDTO();
        ics2.setChplProductNumber("Chpl2");

        CertifiedProductDetailsDTO ics3 = new CertifiedProductDetailsDTO();
        ics3.setChplProductNumber("Chpl1");

        CertifiedProductDetailsDTO ics4 = new CertifiedProductDetailsDTO();
        ics4.setChplProductNumber("Chpl4");

        listing.getIcsParents().add(ics1);
        listing.getIcsParents().add(ics2);
        listing.getIcsParents().add(ics3);
        listing.getIcsParents().add(ics4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(3, listing.getIcsParents().size());
    }
}