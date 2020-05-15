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
import gov.healthit.chpl.validation.listing.reviewer.edition2015.duplicate.TestProcedure2015DuplicateReviewer;

public class TestProcedure2015DuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String ERR_MSG =
            "Certification %s contains duplicate Test Procedure: Name '%s', Version '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private TestProcedure2015DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestProcedure.2015"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        reviewer = new TestProcedure2015DuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertificationResult cert = getCertResult();

        CertificationResultTestProcedure testProc1 = getTestProcedure("TestProc1", "v1");
        CertificationResultTestProcedure testProc2 = getTestProcedure("TestProc1", "v1");

        cert.getTestProcedures().add(testProc1);
        cert.getTestProcedures().add(testProc2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "TestProc1", "v1")))
                .count());
        assertEquals(1, cert.getTestProcedures().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertificationResult cert = getCertResult();

        CertificationResultTestProcedure testProc1 = getTestProcedure("TestProc1", "v1");
        CertificationResultTestProcedure testProc2 = getTestProcedure("TestProc2", "v1");

        cert.getTestProcedures().add(testProc1);
        cert.getTestProcedures().add(testProc2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestProcedures().size());
    }

    @Test
    public void review_emptyTestProcedures_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        cert.getTestProcedures().clear();

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getTestProcedures().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultTestProcedure testProc1 = getTestProcedure("TestProc1", "v1");
        CertificationResultTestProcedure testProc2 = getTestProcedure("TestProc2", "v1");
        CertificationResultTestProcedure testProc3 = getTestProcedure("TestProc1", "v1");
        CertificationResultTestProcedure testProc4 = getTestProcedure("TestProc4", "v2");

        cert.getTestProcedures().add(testProc1);
        cert.getTestProcedures().add(testProc2);
        cert.getTestProcedures().add(testProc3);
        cert.getTestProcedures().add(testProc4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "TestProc1", "v1")))
                .count());
        assertEquals(3, cert.getTestProcedures().size());
    }

    private CertificationResultTestProcedure getTestProcedure(String name, String version) {
        CertificationResultTestProcedure certTp = new CertificationResultTestProcedure();
        TestProcedure tp = new TestProcedure();
        tp.setName(name);
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