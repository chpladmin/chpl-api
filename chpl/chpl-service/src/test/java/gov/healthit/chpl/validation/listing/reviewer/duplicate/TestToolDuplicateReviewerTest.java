package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.criteriaattribute.testtool.CertificationResultTestTool;
import gov.healthit.chpl.criteriaattribute.testtool.TestTool;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestToolDuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String DUPLICATE_NAME_AND_VERSION =
            "Certification %s contains duplicate Test Tool: Name '%s', Version '%s'. The duplicates have been removed.";
    private static final String DUPLICATE_NAME =
            "Certification %s contains duplicate Test Tool: Name '%s'.";

    private ErrorMessageUtil msgUtil;
    private TestToolDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestToolNameAndVersion"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DUPLICATE_NAME_AND_VERSION, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestToolName"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DUPLICATE_NAME, i.getArgument(1), i.getArgument(2)));
        reviewer = new TestToolDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultTestTool testTool1 = getTestTool(1L, "Test Tool 1", "v1");
        CertificationResultTestTool testTool2 = getTestTool(1L, "Test Tool 1", "v1");
        cert.getTestToolsUsed().add(testTool1);
        cert.getTestToolsUsed().add(testTool2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "Test Tool 1", "v1")))
                .count());
        assertEquals(1, cert.getTestToolsUsed().size());
    }

    @Test
    public void review_duplicateNameExists_errorFound() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultTestTool testTool1 = getTestTool(1L, "Test Tool 1", "v1");
        CertificationResultTestTool testTool2 = getTestTool(1L, "Test Tool 1", "v2");
        cert.getTestToolsUsed().add(testTool1);
        cert.getTestToolsUsed().add(testTool2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertEquals(1, listing.getErrorMessages().stream()
                .filter(error -> error.equals(String.format(DUPLICATE_NAME, CRITERION_NUMBER, "Test Tool 1")))
                .count());
        assertEquals(2, cert.getTestToolsUsed().size());
    }

    @Test
    public void review_noDuplicateIds_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultTestTool testTool1 = getTestTool(1L, "Test Tool 1", "v1");
        CertificationResultTestTool testTool2 = getTestTool(2L, "Test Tool 2", "v1");
        cert.getTestToolsUsed().add(testTool1);
        cert.getTestToolsUsed().add(testTool2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestToolsUsed().size());
    }

    @Test
    public void review_emptyTestTools_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        cert.getTestToolsUsed().clear();
        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getTestToolsUsed().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultTestTool testTool1 = getTestTool(1L, "Test Tool 1", "v1");
        CertificationResultTestTool testTool2 = getTestTool(2L, "Test Tool 2", "v2");
        CertificationResultTestTool testTool3 = getTestTool(1L, "Test Tool 1", "v1");
        CertificationResultTestTool testTool4 = getTestTool(3L, "Test Tool 3", "v1");
        cert.getTestToolsUsed().add(testTool1);
        cert.getTestToolsUsed().add(testTool2);
        cert.getTestToolsUsed().add(testTool3);
        cert.getTestToolsUsed().add(testTool4);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "Test Tool 1", "v1")))
                .count());
        assertEquals(3, cert.getTestToolsUsed().size());
    }

    private CertificationResultTestTool getTestTool(Long id, String value, String version) {
        return CertificationResultTestTool.builder()
                .testTool(TestTool.builder()
                        .id(id)
                        .value(value)
                        .build())
                .version(version)
                .build();
    }

    private CertificationResult getCertResult() {
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setNumber(CRITERION_NUMBER);
        CertificationResult cert = new CertificationResult();
        cert.setCriterion(criterion);
        return cert;
    }
}