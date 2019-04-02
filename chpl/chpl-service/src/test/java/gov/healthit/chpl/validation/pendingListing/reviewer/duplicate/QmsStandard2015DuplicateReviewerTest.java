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
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.QmsStandard2015DuplicateReviewer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class QmsStandard2015DuplicateReviewerTest {
    @Autowired
    private MessageSource messageSource;

    @Mock
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private QmsStandard2015DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        reviewer = new QmsStandard2015DuplicateReviewer(msgUtil);

        //TODO - Can this be extracted as some sort of generic method, so it can be used all error messages??
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String message =
                        "Listing contains duplicate QMS Standard: '%s, Criteria: %s'.  The duplicates have been removed.";
                Object[] args = invocation.getArguments();
                return String.format(message, args[1], args[2]);
            }
        }).when(msgUtil).getMessage(ArgumentMatchers.eq("listing.duplicateQmsStandard.2015"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    }

    @Test
    public void testDuplicateExists() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductQmsStandardDTO qms1 = new PendingCertifiedProductQmsStandardDTO();
        qms1.setName("Qms1");
        qms1.setApplicableCriteria("Crit1");

        PendingCertifiedProductQmsStandardDTO qms2 = new PendingCertifiedProductQmsStandardDTO();
        qms2.setName("Qms1");
        qms2.setApplicableCriteria("Crit1");

        listing.getQmsStandards().add(qms1);
        listing.getQmsStandards().add(qms2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getQmsStandards().size());
    }

    @Test
    public void testDuplicatesDoNotExist() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductQmsStandardDTO qms1 = new PendingCertifiedProductQmsStandardDTO();
        qms1.setName("Qms1");
        qms1.setApplicableCriteria("Crit1");

        PendingCertifiedProductQmsStandardDTO qms2 = new PendingCertifiedProductQmsStandardDTO();
        qms2.setName("Qms2");
        qms2.setApplicableCriteria("Crit1");

        listing.getQmsStandards().add(qms1);
        listing.getQmsStandards().add(qms2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getQmsStandards().size());
    }

    @Test
    public void testDuplicateExistInLargerSet() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductQmsStandardDTO qms1 = new PendingCertifiedProductQmsStandardDTO();
        qms1.setName("Qms1");
        qms1.setApplicableCriteria("Crit1");

        PendingCertifiedProductQmsStandardDTO qms2 = new PendingCertifiedProductQmsStandardDTO();
        qms2.setName("Qms2");
        qms2.setApplicableCriteria("Crit1");

        PendingCertifiedProductQmsStandardDTO qms3 = new PendingCertifiedProductQmsStandardDTO();
        qms3.setName("Qms1");
        qms3.setApplicableCriteria("Crit1");

        PendingCertifiedProductQmsStandardDTO qms4 = new PendingCertifiedProductQmsStandardDTO();
        qms4.setName("Qms3");
        qms4.setApplicableCriteria("Crit3");

        listing.getQmsStandards().add(qms1);
        listing.getQmsStandards().add(qms2);
        listing.getQmsStandards().add(qms3);
        listing.getQmsStandards().add(qms4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(3, listing.getQmsStandards().size());
    }
}