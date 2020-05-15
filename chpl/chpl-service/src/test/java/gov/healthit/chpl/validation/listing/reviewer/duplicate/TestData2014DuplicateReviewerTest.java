package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.duplicate.TestData2014DuplicateReviewer;

public class TestData2014DuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.314 (a)(1)";
    private static final String ERR_MSG =
            "Certification %s contains duplicate Test Data: Version '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private TestData2014DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestData.2014"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), i.getArgument(2)));
        reviewer = new TestData2014DuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultTestData testData1 = new CertificationResultTestData();
        testData1.setVersion("v1");

        CertificationResultTestData testData2 = new CertificationResultTestData();
        testData2.setVersion("v1");

        cert.getTestDataUsed().add(testData1);
        cert.getTestDataUsed().add(testData2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "v1")))
                .count());
        assertEquals(1, cert.getTestDataUsed().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultTestData testData1 = new CertificationResultTestData();
        testData1.setVersion("v1");

        CertificationResultTestData testData2 = new CertificationResultTestData();
        testData2.setVersion("v2");

        cert.getTestDataUsed().add(testData1);
        cert.getTestDataUsed().add(testData2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestDataUsed().size());
    }

    @Test
    public void review_emptyTestData_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        cert.getTestDataUsed().clear();

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getTestDataUsed().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultTestData testData1 = new CertificationResultTestData();
        testData1.setVersion("v1");

        CertificationResultTestData testData2 = new CertificationResultTestData();
        testData2.setVersion("v2");

        CertificationResultTestData testData3 = new CertificationResultTestData();
        testData3.setVersion("v1");

        CertificationResultTestData testData4 = new CertificationResultTestData();
        testData4.setVersion("v3");

        cert.getTestDataUsed().add(testData1);
        cert.getTestDataUsed().add(testData2);
        cert.getTestDataUsed().add(testData3);
        cert.getTestDataUsed().add(testData4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "v1")))
                .count());
        assertEquals(3, cert.getTestDataUsed().size());
    }

    private CertificationResult getCertResult() {
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setNumber(CRITERION_NUMBER);
        CertificationResult cert = new CertificationResult();
        cert.setCriterion(criterion);
        return cert;
    }

}