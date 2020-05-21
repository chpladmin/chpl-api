package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestToolDuplicateReviewer;

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
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestToolNameAndVersion"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        reviewer = new TestToolDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultTestToolDTO testTool1 = new PendingCertificationResultTestToolDTO();
        testTool1.setName("TestTool1");
        testTool1.setVersion("v1");

        PendingCertificationResultTestToolDTO testTool2 = new PendingCertificationResultTestToolDTO();
        testTool2.setName("TestTool1");
        testTool2.setVersion("v1");

        cert.getTestTools().add(testTool1);
        cert.getTestTools().add(testTool2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "TestTool1", "v1")))
                .count());
        assertEquals(1, cert.getTestTools().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultTestToolDTO testTool1 = new PendingCertificationResultTestToolDTO();
        testTool1.setName("TestTool1");
        testTool1.setVersion("v1");

        PendingCertificationResultTestToolDTO testTool2 = new PendingCertificationResultTestToolDTO();
        testTool2.setName("TestTool2");
        testTool2.setVersion("v1");

        cert.getTestTools().add(testTool1);
        cert.getTestTools().add(testTool2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestTools().size());
    }

    @Test
    public void review_emptyTestTools_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        cert.getTestTools().clear();
        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getTestTools().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultTestToolDTO testTool1 = new PendingCertificationResultTestToolDTO();
        testTool1.setName("TestTool1");
        testTool1.setVersion("v1");

        PendingCertificationResultTestToolDTO testTool2 = new PendingCertificationResultTestToolDTO();
        testTool2.setName("TestTool2");
        testTool2.setVersion("v1");

        PendingCertificationResultTestToolDTO testTool3 = new PendingCertificationResultTestToolDTO();
        testTool3.setName("TestTool1");
        testTool3.setVersion("v1");

        PendingCertificationResultTestToolDTO testTool4 = new PendingCertificationResultTestToolDTO();
        testTool4.setName("TestTool4");
        testTool4.setVersion("v2");

        cert.getTestTools().add(testTool1);
        cert.getTestTools().add(testTool2);
        cert.getTestTools().add(testTool3);
        cert.getTestTools().add(testTool4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "TestTool1", "v1")))
                .count());
        assertEquals(3, cert.getTestTools().size());
    }

    private PendingCertificationResultDTO getCertResult() {
        CertificationCriterionDTO criterion = new CertificationCriterionDTO();
        criterion.setNumber(CRITERION_NUMBER);
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();
        cert.setCriterion(criterion);
        return cert;
    }
}