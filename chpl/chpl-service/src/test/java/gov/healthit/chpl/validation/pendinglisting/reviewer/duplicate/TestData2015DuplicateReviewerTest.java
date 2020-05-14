package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.TestData2015DuplicateReviewer;

public class TestData2015DuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String ERR_MSG =
            "Certification %s contains duplicate Test Data: Name '%s', Version '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private TestData2015DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestData.2015"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        reviewer = new TestData2015DuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

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
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "TestData1", "v1")))
                .count());
        assertEquals(1, cert.getTestData().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertificationResultDTO cert = getCertResult();

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
    public void review_emptyTestData_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        cert.getTestData().clear();

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getTestData().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

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
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "TestData1", "v1")))
                .count());
        assertEquals(3, cert.getTestData().size());
    }

    private PendingCertificationResultDTO getCertResult() {
        CertificationCriterionDTO criterion = new CertificationCriterionDTO();
        criterion.setNumber(CRITERION_NUMBER);
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();
        cert.setCriterion(criterion);
        return cert;
    }
}
