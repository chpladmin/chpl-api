package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.TestProcedure2015DuplicateReviewer;

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

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "TestProc1", "v1")))
                .count());
        assertEquals(1, cert.getTestProcedures().size());
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

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestProcedures().size());
    }

    @Test
    public void review_emptyTestProcedures_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        cert.getTestProcedures().clear();

        reviewer.review(listing, cert);

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

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "TestProc1", "v1")))
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