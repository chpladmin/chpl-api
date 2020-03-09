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
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestFunctionalityDuplicateReviewer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { old.gov.healthit.chpl.CHPLTestConfig.class })
public class TestFunctionalityDuplicateReviewerTest {
    @Autowired
    private MessageSource messageSource;

    @Mock
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private TestFunctionalityDuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        reviewer = new TestFunctionalityDuplicateReviewer(msgUtil);

        //TODO - Can this be extracted as some sort of generic method, so it can be used all error messages??
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String message =
                        "Certification %s contains duplicate Test Functionality: Number '%s'.  The duplicates have been removed.";
                Object[] args = invocation.getArguments();
                return String.format(message, args[1], args[2]);
            }
        }).when(msgUtil).getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestFunctionality"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    }

    @Test
    public void testDuplicateExists() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultTestFunctionalityDTO testFunc1 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc1.setNumber("TestFunc1");

        PendingCertificationResultTestFunctionalityDTO testFunc2 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc2.setNumber("TestFunc1");

        cert.getTestFunctionality().add(testFunc1);
        cert.getTestFunctionality().add(testFunc2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, cert.getTestFunctionality().size());
    }

    @Test
    public void testDuplicatesDoNotExist() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultTestFunctionalityDTO testFunc1 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc1.setNumber("TestFunc1");

        PendingCertificationResultTestFunctionalityDTO testFunc2 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc2.setNumber("TestFunc2");

        cert.getTestFunctionality().add(testFunc1);
        cert.getTestFunctionality().add(testFunc2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestFunctionality().size());
    }

    @Test
    public void testDuplicateExistInLargerSet() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();

        PendingCertificationResultTestFunctionalityDTO testFunc1 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc1.setNumber("TestFunc1");

        PendingCertificationResultTestFunctionalityDTO testFunc2 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc2.setNumber("TestFunc2");

        PendingCertificationResultTestFunctionalityDTO testFunc3 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc3.setNumber("TestFunc1");

        PendingCertificationResultTestFunctionalityDTO testFunc4 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc4.setNumber("TestFunc3");

        cert.getTestFunctionality().add(testFunc1);
        cert.getTestFunctionality().add(testFunc2);
        cert.getTestFunctionality().add(testFunc3);
        cert.getTestFunctionality().add(testFunc4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(3, cert.getTestFunctionality().size());
    }

}
