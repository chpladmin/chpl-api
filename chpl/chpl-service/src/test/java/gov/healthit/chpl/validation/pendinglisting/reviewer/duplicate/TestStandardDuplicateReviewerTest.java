package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestStandardDuplicateReviewer;

public class TestStandardDuplicateReviewerTest {
    private ErrorMessageUtil msgUtil;
    private TestStandardDuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestStandard"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("Certification %s contains duplicate Test Standard: Number '%s'.  The duplicates have been removed.",
                        i.getArgument(1), i.getArgument(2)));
        reviewer = new TestStandardDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultTestStandardDTO testStandard1 = new PendingCertificationResultTestStandardDTO();
        testStandard1.setName("TestStandard1");

        PendingCertificationResultTestStandardDTO testStandard2 = new PendingCertificationResultTestStandardDTO();
        testStandard2.setName("TestStandard1");

        cert.getTestStandards().add(testStandard1);
        cert.getTestStandards().add(testStandard2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, cert.getTestStandards().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultTestStandardDTO testStandard1 = new PendingCertificationResultTestStandardDTO();
        testStandard1.setName("TestStandard1");

        PendingCertificationResultTestStandardDTO testStandard2 = new PendingCertificationResultTestStandardDTO();
        testStandard2.setName("TestStandard2");

        cert.getTestStandards().add(testStandard1);
        cert.getTestStandards().add(testStandard2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestStandards().size());
    }

    @Test
    public void review_emptyTestStandards_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        cert.getTestStandards().clear();

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getTestStandards().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultTestStandardDTO testStandard1 = new PendingCertificationResultTestStandardDTO();
        testStandard1.setName("TestStandard1");

        PendingCertificationResultTestStandardDTO testStandard2 = new PendingCertificationResultTestStandardDTO();
        testStandard2.setName("TestStandard2");

        PendingCertificationResultTestStandardDTO testStandard3 = new PendingCertificationResultTestStandardDTO();
        testStandard3.setName("TestStandard1");

        PendingCertificationResultTestStandardDTO testStandard4 = new PendingCertificationResultTestStandardDTO();
        testStandard4.setName("TestStandard4");

        cert.getTestStandards().add(testStandard1);
        cert.getTestStandards().add(testStandard2);
        cert.getTestStandards().add(testStandard3);
        cert.getTestStandards().add(testStandard4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(3, cert.getTestStandards().size());
    }

    private PendingCertificationResultDTO getCertResult() {
        CertificationCriterionDTO criterion = new CertificationCriterionDTO();
        criterion.setNumber("170.315 (a)(1)");
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();
        cert.setCriterion(criterion);
        return cert;
    }
}