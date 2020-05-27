package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestData;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestDataDuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String DUPLICATE_NAME_AND_VERSION =
            "Certification %s contains duplicate Test Data: Name '%s', Version '%s'. The duplicates have been removed.";
    private static final String DUPLICATE_NAME =
            "Certification %s contains duplicate Test Data: Name '%s'.";

    private ErrorMessageUtil msgUtil;
    private TestDataDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestDataNameAndVersion"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DUPLICATE_NAME_AND_VERSION, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestDataName"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DUPLICATE_NAME, i.getArgument(1), i.getArgument(2)));
        reviewer = new TestDataDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultTestData testData1 = getTestData(1L, "TestData1", "v1");
        CertificationResultTestData testData2 = getTestData(1L, "TestData1", "v1");
        cert.getTestDataUsed().add(testData1);
        cert.getTestDataUsed().add(testData2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "TestData1", "v1")))
                .count());
        assertEquals(1, cert.getTestDataUsed().size());
    }

    @Test
    public void review_duplicateNameExists_errorFound() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultTestData testData1 = getTestData(1L, "TestData1", "v1");
        CertificationResultTestData testData2 = getTestData(1L, "TestData1", "v2");
        cert.getTestDataUsed().add(testData1);
        cert.getTestDataUsed().add(testData2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertEquals(1, listing.getErrorMessages().stream()
                .filter(error -> error.equals(String.format(DUPLICATE_NAME, CRITERION_NUMBER, "TestData1")))
                .count());
        assertEquals(2, cert.getTestDataUsed().size());
    }

    @Test
    public void review_duplicateNameNullVersion_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultTestData testData1 = getTestData(1L, "TestData1", null);
        CertificationResultTestData testData2 = getTestData(1L, "TestData1", null);
        cert.getTestDataUsed().add(testData1);
        cert.getTestDataUsed().add(testData2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "TestData1", "")))
                .count());
        assertEquals(1, cert.getTestDataUsed().size());
    }

    @Test
    public void review_duplicateNameEmptyVersion_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultTestData testData1 = getTestData(1L, "TestData1", "");
        CertificationResultTestData testData2 = getTestData(1L, "TestData1", "");
        cert.getTestDataUsed().add(testData1);
        cert.getTestDataUsed().add(testData2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "TestData1", "")))
                .count());
        assertEquals(1, cert.getTestDataUsed().size());
    }

    @Test
    public void review_noDuplicateIds_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultTestData testData1 = getTestData(1L, "TestData1", "v1");
        CertificationResultTestData testData2 = getTestData(2L, "TestData2", "v1");
        cert.getTestDataUsed().add(testData1);
        cert.getTestDataUsed().add(testData2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestDataUsed().size());
    }

    @Test
    public void review_emptyTestData_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        cert.getTestDataUsed().clear();

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getTestDataUsed().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultTestData testData1 = getTestData(1L, "TestData1", "v1");
        CertificationResultTestData testData2 = getTestData(2L, "TestData2", "v1");
        CertificationResultTestData testData3 = getTestData(1L, "TestData1", "v1");
        CertificationResultTestData testData4 = getTestData(3L, "TestData4", "v2");

        cert.getTestDataUsed().add(testData1);
        cert.getTestDataUsed().add(testData2);
        cert.getTestDataUsed().add(testData3);
        cert.getTestDataUsed().add(testData4);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "TestData1", "v1")))
                .count());
        assertEquals(3, cert.getTestDataUsed().size());
    }

    private CertificationResultTestData getTestData(Long id, String name, String version) {
        CertificationResultTestData certTestData = new CertificationResultTestData();
        TestData td = new TestData();
        td.setId(id);
        td.setName(name);
        certTestData.setTestData(td);
        certTestData.setVersion(version);
        return certTestData;
    }

    private CertificationResult getCertResult() {
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setNumber(CRITERION_NUMBER);
        CertificationResult cert = new CertificationResult();
        cert.setCriterion(criterion);
        return cert;
    }
}
