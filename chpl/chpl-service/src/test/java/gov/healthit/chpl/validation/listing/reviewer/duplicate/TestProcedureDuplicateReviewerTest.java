package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestProcedure;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestProcedureDuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String DUPLICATE_NAME_AND_VERSION =
            "Certification %s contains duplicate Test Procedure: Name '%s', Version '%s'. The duplicates have been removed.";
    private static final String DUPLICATE_NAME =
            "Certification %s contains duplicate Test Procedure: '%s'.";

    private ErrorMessageUtil msgUtil;
    private TestProcedureDuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestProcedureNameAndVersion"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DUPLICATE_NAME_AND_VERSION, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestProcedureName"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DUPLICATE_NAME, i.getArgument(1), i.getArgument(2)));
        reviewer = new TestProcedureDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultTestProcedure testProc1 = getTestProcedure(1L, "TestProc1", "v1");
        CertificationResultTestProcedure testProc2 = getTestProcedure(1L, "TestProc1", "v1");
        cert.getTestProcedures().add(testProc1);
        cert.getTestProcedures().add(testProc2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "TestProc1", "v1")))
                .count());
        assertEquals(1, cert.getTestProcedures().size());
    }

    @Test
    public void review_duplicateNameExists_errorFound() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultTestProcedure testProc1 = getTestProcedure(1L, "TestProc1", "v1");
        CertificationResultTestProcedure testProc2 = getTestProcedure(1L, "TestProc1", "v2");
        cert.getTestProcedures().add(testProc1);
        cert.getTestProcedures().add(testProc2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertEquals(1, listing.getErrorMessages().stream()
                .filter(error -> error.equals(String.format(DUPLICATE_NAME, CRITERION_NUMBER, "TestProc1")))
                .count());
        assertEquals(2, cert.getTestProcedures().size());
    }

    @Test
    public void review_duplicateNameNullVersion_errorFound() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        //2014 listings allow null tp version
        CertificationResultTestProcedure testProc1 = getTestProcedure(1L, "TestProc1", null);
        CertificationResultTestProcedure testProc2 = getTestProcedure(1L, "TestProc1", null);
        cert.getTestProcedures().add(testProc1);
        cert.getTestProcedures().add(testProc2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME, CRITERION_NUMBER, "TestProc1")))
                .count());
        assertEquals(1, cert.getTestProcedures().size());
    }

    @Test
    public void review_duplicateNameEmptyVersion_errorFound() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        //2014 listings allow empty tp version
        CertificationResultTestProcedure testProc1 = getTestProcedure(1L, "TestProc1", "");
        CertificationResultTestProcedure testProc2 = getTestProcedure(1L, "TestProc1", "");
        cert.getTestProcedures().add(testProc1);
        cert.getTestProcedures().add(testProc2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME, CRITERION_NUMBER, "TestProc1")))
                .count());
        assertEquals(1, cert.getTestProcedures().size());
    }

    @Test
    public void review_noDuplicateIds_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultTestProcedure testProc1 = getTestProcedure(1L, "TestProc1", "v1");
        CertificationResultTestProcedure testProc2 = getTestProcedure(2L, "TestProc2", "v1");
        cert.getTestProcedures().add(testProc1);
        cert.getTestProcedures().add(testProc2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(2, cert.getTestProcedures().size());
    }

    @Test
    public void review_emptyTestProcedures_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        cert.getTestProcedures().clear();
        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, cert.getTestProcedures().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultTestProcedure testProc1 = getTestProcedure(1L, "TestProc1", "v1");
        CertificationResultTestProcedure testProc2 = getTestProcedure(2L, "TestProc2", "v1");
        CertificationResultTestProcedure testProc3 = getTestProcedure(1L, "TestProc1", "v1");
        CertificationResultTestProcedure testProc4 = getTestProcedure(4L, "TestProc4", "v2");
        cert.getTestProcedures().add(testProc1);
        cert.getTestProcedures().add(testProc2);
        cert.getTestProcedures().add(testProc3);
        cert.getTestProcedures().add(testProc4);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPLICATE_NAME_AND_VERSION, CRITERION_NUMBER, "TestProc1", "v1")))
                .count());
        assertEquals(3, cert.getTestProcedures().size());
    }

    private CertificationResultTestProcedure getTestProcedure(Long id, String tpName, String version) {
        CertificationResultTestProcedure certTp = new CertificationResultTestProcedure();
        TestProcedure tp = new TestProcedure();
        tp.setId(id);
        tp.setName(tpName);
        certTp.setTestProcedure(tp);
        certTp.setTestProcedureVersion(version);
        return certTp;
    }
    private CertificationResult getCertResult() {
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setNumber(CRITERION_NUMBER);
        CertificationResult cert = new CertificationResult();
        cert.setCriterion(criterion);
        return cert;
    }
}