package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestDataDuplicateReviewer;

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
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultTestDataDTO testData1 = getTestData(1L, "TestData1", "v1");
        PendingCertificationResultTestDataDTO testData2 = getTestData(1L, "TestData1", "v1");
        cert.getTestData().add(testData1);
        cert.getTestData().add(testData2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "TestData1", "v1")))
                .count());
        assertEquals(1, cert.getTestData().size());
    }

    @Test
    public void review_duplicateNameExists_errorFound() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultTestDataDTO testData1 = getTestData(1L, "TestData1", "v1");
        PendingCertificationResultTestDataDTO testData2 = getTestData(1L, "TestData1", "v2");
        cert.getTestData().add(testData1);
        cert.getTestData().add(testData2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertEquals(1, listing.getErrorMessages().stream()
                .filter(error -> error.equals(String.format(DUPLICATE_NAME, CRITERION_NUMBER, "TestData1")))
                .count());
        assertEquals(2, cert.getTestData().size());
    }

    @Test
    public void review_duplicateNameNullVersion_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultTestDataDTO testData1 = getTestData(1L, "TestData1", null);
        PendingCertificationResultTestDataDTO testData2 = getTestData(1L, "TestData1", null);
        cert.getTestData().add(testData1);
        cert.getTestData().add(testData2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "TestData1", "")))
                .count());
        assertEquals(1, cert.getTestData().size());
    }

    @Test
    public void review_duplicateNameEmptyVersion_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultTestDataDTO testData1 = getTestData(1L, "TestData1", "");
        PendingCertificationResultTestDataDTO testData2 = getTestData(1L, "TestData1", "");
        cert.getTestData().add(testData1);
        cert.getTestData().add(testData2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "TestData1", "")))
                .count());
        assertEquals(1, cert.getTestData().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultTestDataDTO testData1 = getTestData(1L, "TestData1", "v1");
        PendingCertificationResultTestDataDTO testData2 = getTestData(2L, "TestData2", "v2");
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
        PendingCertificationResultTestDataDTO testData1 = getTestData(1L, "TestData1", "v1");
        PendingCertificationResultTestDataDTO testData2 = getTestData(2L, "TestData2", "v1");
        PendingCertificationResultTestDataDTO testData3 = getTestData(1L, "TestData1", "v1");
        PendingCertificationResultTestDataDTO testData4 = getTestData(3L, "TestData3", "v1");

        cert.getTestData().add(testData1);
        cert.getTestData().add(testData2);
        cert.getTestData().add(testData3);
        cert.getTestData().add(testData4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "TestData1", "v1")))
                .count());
        assertEquals(3, cert.getTestData().size());
    }

    private PendingCertificationResultTestDataDTO getTestData(Long id, String name, String version) {
        PendingCertificationResultTestDataDTO certTestData = new PendingCertificationResultTestDataDTO();
        certTestData.setTestDataId(id);
        certTestData.setEnteredName(name);
        TestDataDTO td = new TestDataDTO();
        td.setId(id);
        td.setName(name);
        certTestData.setTestData(td);
        certTestData.setVersion(version);
        return certTestData;
    }

    private PendingCertificationResultDTO getCertResult() {
        CertificationCriterionDTO criterion = new CertificationCriterionDTO();
        criterion.setNumber(CRITERION_NUMBER);
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();
        cert.setCriterion(criterion);
        return cert;
    }
}
