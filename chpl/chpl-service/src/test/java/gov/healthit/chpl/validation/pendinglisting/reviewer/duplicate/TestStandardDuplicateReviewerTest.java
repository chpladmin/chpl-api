package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestStandardDuplicateReviewer;

public class TestStandardDuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String ERR_MSG =
            "Certification %s contains duplicate Test Standard: Number '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private TestStandardDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestStandard"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), i.getArgument(2)));
        reviewer = new TestStandardDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateIdExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultTestStandardDTO testStandard1 = getTestStandard(1L, "TestStandard1");
        PendingCertificationResultTestStandardDTO testStandard2 = getTestStandard(1L, "TestStandard1");
        cert.getTestStandards().add(testStandard1);
        cert.getTestStandards().add(testStandard2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "TestStandard1")))
                .count());
        assertEquals(1, cert.getTestStandards().size());
    }

    @Test
    public void review_duplicateNameExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultTestStandardDTO testStandard1 = getTestStandard(null, "TestStandard1");
        PendingCertificationResultTestStandardDTO testStandard2 = getTestStandard(null, "TestStandard1");
        cert.getTestStandards().add(testStandard1);
        cert.getTestStandards().add(testStandard2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "TestStandard1")))
                .count());
        assertEquals(1, cert.getTestStandards().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultTestStandardDTO testStandard1 = getTestStandard(1L, "TestStandard1");
        PendingCertificationResultTestStandardDTO testStandard2 = getTestStandard(2L, "TestStandard2");
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
        PendingCertificationResultTestStandardDTO testStandard1 = getTestStandard(1L, "TestStandard1");
        PendingCertificationResultTestStandardDTO testStandard2 = getTestStandard(2L, "TestStandard2");
        PendingCertificationResultTestStandardDTO testStandard3 = getTestStandard(1L, "TestStandard1");
        PendingCertificationResultTestStandardDTO testStandard4 = getTestStandard(3L, "TestStandard3");
        cert.getTestStandards().add(testStandard1);
        cert.getTestStandards().add(testStandard2);
        cert.getTestStandards().add(testStandard3);
        cert.getTestStandards().add(testStandard4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "TestStandard1")))
                .count());
        assertEquals(3, cert.getTestStandards().size());
    }

    private PendingCertificationResultTestStandardDTO getTestStandard(Long id, String name) {
        PendingCertificationResultTestStandardDTO testStandard = new PendingCertificationResultTestStandardDTO();
        testStandard.setTestStandardId(id);
        testStandard.setName(name);
        return testStandard;
    }

    private PendingCertificationResultDTO getCertResult() {
        CertificationCriterionDTO criterion = new CertificationCriterionDTO();
        criterion.setNumber(CRITERION_NUMBER);
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();
        cert.setCriterion(criterion);
        return cert;
    }
}