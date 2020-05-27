package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestProcedureDuplicateReviewer;

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
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultTestProcedureDTO testProc1 = new PendingCertificationResultTestProcedureDTO();
        testProc1.setEnteredName("TestProc1");
        testProc1.setVersion("v1");

        PendingCertificationResultTestProcedureDTO testProc2 = new PendingCertificationResultTestProcedureDTO();
        testProc2.setEnteredName("TestProc1");
        testProc2.setVersion("v1");

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
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultTestProcedureDTO testProc1 = new PendingCertificationResultTestProcedureDTO();
        testProc1.setEnteredName("TestProc1");
        testProc1.setVersion("v1");

        PendingCertificationResultTestProcedureDTO testProc2 = new PendingCertificationResultTestProcedureDTO();
        testProc2.setEnteredName("TestProc1");
        testProc2.setVersion("v2");

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
    public void review_noDuplicates_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultTestProcedureDTO testProc1 = new PendingCertificationResultTestProcedureDTO();
        testProc1.setEnteredName("TestProc1");
        testProc1.setVersion("v1");

        PendingCertificationResultTestProcedureDTO testProc2 = new PendingCertificationResultTestProcedureDTO();
        testProc2.setEnteredName("TestProc2");
        testProc2.setVersion("v1");

        cert.getTestProcedures().add(testProc1);
        cert.getTestProcedures().add(testProc2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestProcedures().size());
    }

    @Test
    public void review_emptyTestProcedures_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        cert.getTestProcedures().clear();

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getTestProcedures().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultTestProcedureDTO testProc1 = new PendingCertificationResultTestProcedureDTO();
        testProc1.setEnteredName("TestProc1");
        testProc1.setVersion("v1");

        PendingCertificationResultTestProcedureDTO testProc2 = new PendingCertificationResultTestProcedureDTO();
        testProc2.setEnteredName("TestProc2");
        testProc2.setVersion("v1");

        PendingCertificationResultTestProcedureDTO testProc3 = new PendingCertificationResultTestProcedureDTO();
        testProc3.setEnteredName("TestProc1");
        testProc3.setVersion("v1");

        PendingCertificationResultTestProcedureDTO testProc4 = new PendingCertificationResultTestProcedureDTO();
        testProc4.setEnteredName("TestProc4");
        testProc4.setVersion("v2");

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

    private PendingCertificationResultDTO getCertResult() {
        CertificationCriterionDTO criterion = new CertificationCriterionDTO();
        criterion.setNumber(CRITERION_NUMBER);
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();
        cert.setCriterion(criterion);
        return cert;
    }
}