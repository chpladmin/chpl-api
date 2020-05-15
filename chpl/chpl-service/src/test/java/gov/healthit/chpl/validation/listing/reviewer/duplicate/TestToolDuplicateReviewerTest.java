package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestToolDuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String ERR_MSG =
            "Certification %s contains duplicate Test Tool: Name '%s', Version '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private TestToolDuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestTool"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        reviewer = new TestToolDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultTestTool testTool1 = new CertificationResultTestTool();
        testTool1.setTestToolName("TestTool1");
        testTool1.setTestToolVersion("v1");

        CertificationResultTestTool testTool2 = new CertificationResultTestTool();
        testTool2.setTestToolName("TestTool1");
        testTool2.setTestToolVersion("v1");

        cert.getTestToolsUsed().add(testTool1);
        cert.getTestToolsUsed().add(testTool2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "TestTool1", "v1")))
                .count());
        assertEquals(1, cert.getTestToolsUsed().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultTestTool testTool1 = new CertificationResultTestTool();
        testTool1.setTestToolName("TestTool1");
        testTool1.setTestToolVersion("v1");

        CertificationResultTestTool testTool2 = new CertificationResultTestTool();
        testTool2.setTestToolName("TestTool2");
        testTool2.setTestToolVersion("v1");

        cert.getTestToolsUsed().add(testTool1);
        cert.getTestToolsUsed().add(testTool2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestToolsUsed().size());
    }

    @Test
    public void review_emptyTestTools_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        cert.getTestToolsUsed().clear();
        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getTestToolsUsed().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultTestTool testTool1 = new CertificationResultTestTool();
        testTool1.setTestToolName("TestTool1");
        testTool1.setTestToolVersion("v1");

        CertificationResultTestTool testTool2 = new CertificationResultTestTool();
        testTool2.setTestToolName("TestTool2");
        testTool2.setTestToolVersion("v1");

        CertificationResultTestTool testTool3 = new CertificationResultTestTool();
        testTool3.setTestToolName("TestTool1");
        testTool3.setTestToolVersion("v1");

        CertificationResultTestTool testTool4 = new CertificationResultTestTool();
        testTool4.setTestToolName("TestTool4");
        testTool4.setTestToolVersion("v2");

        cert.getTestToolsUsed().add(testTool1);
        cert.getTestToolsUsed().add(testTool2);
        cert.getTestToolsUsed().add(testTool3);
        cert.getTestToolsUsed().add(testTool4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "TestTool1", "v1")))
                .count());
        assertEquals(3, cert.getTestToolsUsed().size());
    }

    private CertificationResult getCertResult() {
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setNumber(CRITERION_NUMBER);
        CertificationResult cert = new CertificationResult();
        cert.setCriterion(criterion);
        return cert;
    }
}