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

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class TestToolDuplicateReviewerTest {
    @Autowired
    private MessageSource messageSource;

    @Mock
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private TestToolDuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        reviewer = new TestToolDuplicateReviewer(msgUtil);

        //TODO - Can this be extracted as some sort of generic method, so it can be used all error messages??
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String message =
                        "Certification %s contains duplicate Test Tool: Name '%s', Version '%s'.  The duplicates have been removed.";
                Object[] args = invocation.getArguments();
                return String.format(message, args[1], args[2], args[3]);
            }
        }).when(msgUtil).getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestTool"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    }

    @Test
    public void testDuplicateExists() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultTestToolDTO testTool1 = new PendingCertificationResultTestToolDTO();
        testTool1.setName("TestTool1");
        testTool1.setVersion("v1");

        PendingCertificationResultTestToolDTO testTool2 = new PendingCertificationResultTestToolDTO();
        testTool2.setName("TestTool1");
        testTool2.setVersion("v1");

        cert.getTestTools().add(testTool1);
        cert.getTestTools().add(testTool2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, cert.getTestTools().size());
    }

    @Test
    public void testDuplicatesDoNotExist() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultTestToolDTO testTool1 = new PendingCertificationResultTestToolDTO();
        testTool1.setName("TestTool1");
        testTool1.setVersion("v1");

        PendingCertificationResultTestToolDTO testTool2 = new PendingCertificationResultTestToolDTO();
        testTool2.setName("TestTool2");
        testTool2.setVersion("v1");

        cert.getTestTools().add(testTool1);
        cert.getTestTools().add(testTool2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestTools().size());
    }

    @Test
    public void testDuplicateExistInLargerSet() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultTestToolDTO testTool1 = new PendingCertificationResultTestToolDTO();
        testTool1.setName("TestTool1");
        testTool1.setVersion("v1");

        PendingCertificationResultTestToolDTO testTool2 = new PendingCertificationResultTestToolDTO();
        testTool2.setName("TestTool2");
        testTool2.setVersion("v1");

        PendingCertificationResultTestToolDTO testTool3 = new PendingCertificationResultTestToolDTO();
        testTool3.setName("TestTool1");
        testTool3.setVersion("v1");

        PendingCertificationResultTestToolDTO testTool4 = new PendingCertificationResultTestToolDTO();
        testTool4.setName("TestTool4");
        testTool4.setVersion("v2");

        cert.getTestTools().add(testTool1);
        cert.getTestTools().add(testTool2);
        cert.getTestTools().add(testTool3);
        cert.getTestTools().add(testTool4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(3, cert.getTestTools().size());
    }

}
