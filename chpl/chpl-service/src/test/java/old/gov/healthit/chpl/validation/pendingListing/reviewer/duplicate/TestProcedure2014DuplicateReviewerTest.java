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

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.duplicate.TestProcedure2014DuplicateReviewer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { old.gov.healthit.chpl.CHPLTestConfig.class })
public class TestProcedure2014DuplicateReviewerTest {
    @Autowired
    private MessageSource messageSource;

    @Mock
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private TestProcedure2014DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        reviewer = new TestProcedure2014DuplicateReviewer(msgUtil);

        //TODO - Can this be extracted as some sort of generic method, so it can be used all error messages??
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String message =
                        "Certification %s contains duplicate Test Procedure: Version '%s'.  The duplicates have been removed.";
                Object[] args = invocation.getArguments();
                return String.format(message, args[1], args[2]);
            }
        }).when(msgUtil).getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestProcedure.2014"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    }

    @Test
    public void testDuplicateExists() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultTestProcedureDTO testProc1 = new PendingCertificationResultTestProcedureDTO();
        testProc1.setVersion("v1");

        PendingCertificationResultTestProcedureDTO testProc2 = new PendingCertificationResultTestProcedureDTO();
        testProc2.setVersion("v1");

        cert.getTestProcedures().add(testProc1);
        cert.getTestProcedures().add(testProc2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, cert.getTestProcedures().size());
    }

    @Test
    public void testDuplicatesDoNotExist() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultTestProcedureDTO testProc1 = new PendingCertificationResultTestProcedureDTO();
        testProc1.setVersion("v1");

        PendingCertificationResultTestProcedureDTO testProc2 = new PendingCertificationResultTestProcedureDTO();
        testProc2.setVersion("v2");

        cert.getTestProcedures().add(testProc1);
        cert.getTestProcedures().add(testProc2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestProcedures().size());
    }

    @Test
    public void testDuplicateExistInLargerSet() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultTestProcedureDTO testProc1 = new PendingCertificationResultTestProcedureDTO();
        testProc1.setVersion("v1");

        PendingCertificationResultTestProcedureDTO testProc2 = new PendingCertificationResultTestProcedureDTO();
        testProc2.setVersion("v2");

        PendingCertificationResultTestProcedureDTO testProc3 = new PendingCertificationResultTestProcedureDTO();
        testProc3.setVersion("v1");

        PendingCertificationResultTestProcedureDTO testProc4 = new PendingCertificationResultTestProcedureDTO();
        testProc4.setVersion("v3");

        cert.getTestProcedures().add(testProc1);
        cert.getTestProcedures().add(testProc2);
        cert.getTestProcedures().add(testProc3);
        cert.getTestProcedures().add(testProc4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(3, cert.getTestProcedures().size());
    }

}
