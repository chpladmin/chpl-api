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
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.TestData2015DuplicateReviewer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { old.gov.healthit.chpl.CHPLTestConfig.class })
public class TestData2015DuplicateReviewerTest {
    @Autowired
    private MessageSource messageSource;

    @Mock
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private TestData2015DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        reviewer = new TestData2015DuplicateReviewer(msgUtil);

        //TODO - Can this be extracted as some sort of generic method, so it can be used all error messages??
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String message =
                        "Certification %s contains duplicate Test Data: Name '%s', Version '%s'.  The duplicates have been removed.";
                Object[] args = invocation.getArguments();
                return String.format(message, args[1], args[2], args[3]);
            }
        }).when(msgUtil).getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestData.2015"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    }

    @Test
    public void testDuplicateExists() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultTestDataDTO testData1 = new PendingCertificationResultTestDataDTO();
        testData1.setEnteredName("TestData1");
        testData1.setVersion("v1");

        PendingCertificationResultTestDataDTO testData2 = new PendingCertificationResultTestDataDTO();
        testData2.setEnteredName("TestData1");
        testData2.setVersion("v1");

        cert.getTestData().add(testData1);
        cert.getTestData().add(testData2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, cert.getTestData().size());
    }

    @Test
    public void testDuplicatesDoNotExist() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultTestDataDTO testData1 = new PendingCertificationResultTestDataDTO();
        testData1.setEnteredName("TestData1");
        testData1.setVersion("v1");

        PendingCertificationResultTestDataDTO testData2 = new PendingCertificationResultTestDataDTO();
        testData2.setEnteredName("TestData2");
        testData2.setVersion("v1");

        cert.getTestData().add(testData1);
        cert.getTestData().add(testData2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestData().size());
    }

    @Test
    public void testDuplicateExistInLargerSet() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultTestDataDTO testData1 = new PendingCertificationResultTestDataDTO();
        testData1.setEnteredName("TestData1");
        testData1.setVersion("v1");

        PendingCertificationResultTestDataDTO testData2 = new PendingCertificationResultTestDataDTO();
        testData2.setEnteredName("TestData2");
        testData2.setVersion("v1");

        PendingCertificationResultTestDataDTO testData3 = new PendingCertificationResultTestDataDTO();
        testData3.setEnteredName("TestData1");
        testData3.setVersion("v1");

        PendingCertificationResultTestDataDTO testData4 = new PendingCertificationResultTestDataDTO();
        testData4.setEnteredName("TestData4");
        testData4.setVersion("v2");

        cert.getTestData().add(testData1);
        cert.getTestData().add(testData2);
        cert.getTestData().add(testData3);
        cert.getTestData().add(testData4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(3, cert.getTestData().size());
    }

}
