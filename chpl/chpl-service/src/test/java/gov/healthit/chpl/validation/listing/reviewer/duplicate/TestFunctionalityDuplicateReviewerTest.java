package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestFunctionalityDuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String ERR_MSG =
            "Certification %s contains duplicate Test Functionality: Number '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private TestFunctionalityDuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestFunctionality"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), i.getArgument(2)));
        reviewer = new TestFunctionalityDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultTestFunctionality testFunc1 = new CertificationResultTestFunctionality();
        testFunc1.setTestFunctionalityId(1L);
        testFunc1.setName("TestFunc1");

        CertificationResultTestFunctionality testFunc2 = new CertificationResultTestFunctionality();
        testFunc2.setTestFunctionalityId(1L);
        testFunc2.setName("TestFunc1");

        cert.getTestFunctionality().add(testFunc1);
        cert.getTestFunctionality().add(testFunc2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "TestFunc1")))
                .count());
        assertEquals(1, cert.getTestFunctionality().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultTestFunctionality testFunc1 = new CertificationResultTestFunctionality();
        testFunc1.setTestFunctionalityId(1L);
        testFunc1.setName("TestFunc1");

        CertificationResultTestFunctionality testFunc2 = new CertificationResultTestFunctionality();
        testFunc2.setTestFunctionalityId(2L);
        testFunc2.setName("TestFunc2");

        cert.getTestFunctionality().add(testFunc1);
        cert.getTestFunctionality().add(testFunc2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestFunctionality().size());
    }

    @Test
    public void review_emptyTestFunctionality_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        cert.getTestFunctionality().clear();

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getTestFunctionality().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultTestFunctionality testFunc1 = new CertificationResultTestFunctionality();
        testFunc1.setTestFunctionalityId(1L);
        testFunc1.setName("TestFunc1");

        CertificationResultTestFunctionality testFunc2 = new CertificationResultTestFunctionality();
        testFunc2.setTestFunctionalityId(2L);
        testFunc2.setName("TestFunc2");

        CertificationResultTestFunctionality testFunc3 = new CertificationResultTestFunctionality();
        testFunc3.setTestFunctionalityId(1L);
        testFunc3.setName("TestFunc1");

        CertificationResultTestFunctionality testFunc4 = new CertificationResultTestFunctionality();
        testFunc4.setTestFunctionalityId(3L);
        testFunc4.setName("TestFunc3");

        cert.getTestFunctionality().add(testFunc1);
        cert.getTestFunctionality().add(testFunc2);
        cert.getTestFunctionality().add(testFunc3);
        cert.getTestFunctionality().add(testFunc4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "TestFunc1")))
                .count());
        assertEquals(3, cert.getTestFunctionality().size());
    }

    private CertificationResult getCertResult() {
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setNumber(CRITERION_NUMBER);
        CertificationResult cert = new CertificationResult();
        cert.setCriterion(criterion);
        return cert;
    }
}