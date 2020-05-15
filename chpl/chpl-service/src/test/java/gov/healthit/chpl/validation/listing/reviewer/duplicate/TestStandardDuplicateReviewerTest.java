package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestStandardDuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String ERR_MSG =
            "Certification %s contains duplicate Test Standard: Number '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private TestStandardDuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestStandard"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), i.getArgument(2)));
        reviewer = new TestStandardDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertificationResult cert = getCertResult();

        CertificationResultTestStandard testStandard1 = new CertificationResultTestStandard();
        testStandard1.setTestStandardName("TestStandard1");

        CertificationResultTestStandard testStandard2 = new CertificationResultTestStandard();
        testStandard2.setTestStandardName("TestStandard1");

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
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultTestStandard testStandard1 = new CertificationResultTestStandard();
        testStandard1.setTestStandardName("TestStandard1");

        CertificationResultTestStandard testStandard2 = new CertificationResultTestStandard();
        testStandard2.setTestStandardName("TestStandard2");

        cert.getTestStandards().add(testStandard1);
        cert.getTestStandards().add(testStandard2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestStandards().size());
    }

    @Test
    public void review_emptyTestStandards_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        cert.getTestStandards().clear();

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getTestStandards().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultTestStandard testStandard1 = new CertificationResultTestStandard();
        testStandard1.setTestStandardName("TestStandard1");

        CertificationResultTestStandard testStandard2 = new CertificationResultTestStandard();
        testStandard2.setTestStandardName("TestStandard2");

        CertificationResultTestStandard testStandard3 = new CertificationResultTestStandard();
        testStandard3.setTestStandardName("TestStandard1");

        CertificationResultTestStandard testStandard4 = new CertificationResultTestStandard();
        testStandard4.setTestStandardName("TestStandard4");

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

    private CertificationResult getCertResult() {
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setNumber(CRITERION_NUMBER);
        CertificationResult cert = new CertificationResult();
        cert.setCriterion(criterion);
        return cert;
    }
}