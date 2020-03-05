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
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestStandardDuplicateReviewer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { old.gov.healthit.chpl.CHPLTestConfig.class })
public class TestStandardDuplicateReviewerTest {
    @Autowired
    private MessageSource messageSource;

    @Mock
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private TestStandardDuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        reviewer = new TestStandardDuplicateReviewer(msgUtil);

        //TODO - Can this be extracted as some sort of generic method, so it can be used all error messages??
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String message =
                        "Certification %s contains duplicate Test Standard: Number '%s'.  The duplicates have been removed.";
                Object[] args = invocation.getArguments();
                return String.format(message, args[1], args[2]);
            }
        }).when(msgUtil).getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestStandard"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    }

    @Test
    public void testDuplicateExists() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultTestStandardDTO testStandard1 = new PendingCertificationResultTestStandardDTO();
        testStandard1.setName("TestStandard1");

        PendingCertificationResultTestStandardDTO testStandard2 = new PendingCertificationResultTestStandardDTO();
        testStandard2.setName("TestStandard1");

        cert.getTestStandards().add(testStandard1);
        cert.getTestStandards().add(testStandard2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, cert.getTestStandards().size());
    }

    @Test
    public void testDuplicatesDoNotExist() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultTestStandardDTO testStandard1 = new PendingCertificationResultTestStandardDTO();
        testStandard1.setName("TestStandard1");

        PendingCertificationResultTestStandardDTO testStandard2 = new PendingCertificationResultTestStandardDTO();
        testStandard2.setName("TestStandard2");

        cert.getTestStandards().add(testStandard1);
        cert.getTestStandards().add(testStandard2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestStandards().size());
    }

    @Test
    public void testDuplicateExistInLargerSet() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultTestStandardDTO testStandard1 = new PendingCertificationResultTestStandardDTO();
        testStandard1.setName("TestStandard1");

        PendingCertificationResultTestStandardDTO testStandard2 = new PendingCertificationResultTestStandardDTO();
        testStandard2.setName("TestStandard2");

        PendingCertificationResultTestStandardDTO testStandard3 = new PendingCertificationResultTestStandardDTO();
        testStandard3.setName("TestStandard1");

        PendingCertificationResultTestStandardDTO testStandard4 = new PendingCertificationResultTestStandardDTO();
        testStandard4.setName("TestStandard4");

        cert.getTestStandards().add(testStandard1);
        cert.getTestStandards().add(testStandard2);
        cert.getTestStandards().add(testStandard3);
        cert.getTestStandards().add(testStandard4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(3, cert.getTestStandards().size());
    }

}
