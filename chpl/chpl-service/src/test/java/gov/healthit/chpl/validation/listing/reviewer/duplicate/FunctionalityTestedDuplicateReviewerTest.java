package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class FunctionalityTestedDuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String ERR_MSG =
            "Certification %s contains duplicate Test Functionality: Number '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private FunctionalityTestedDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateTestFunctionality"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), i.getArgument(2)));
        reviewer = new FunctionalityTestedDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultFunctionalityTested funcTest1 = new CertificationResultFunctionalityTested();
        funcTest1.setFunctionalityTestedId(1L);
        funcTest1.setName("FuncTest1");

        CertificationResultFunctionalityTested funcTest2 = new CertificationResultFunctionalityTested();
        funcTest2.setFunctionalityTestedId(1L);
        funcTest2.setName("FuncTest1");

        cert.getFunctionalitiesTested().add(funcTest2);
        cert.getFunctionalitiesTested().add(funcTest2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "FuncTest1")))
                .count());
        assertEquals(1, cert.getFunctionalitiesTested().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultFunctionalityTested funcTest1 = new CertificationResultFunctionalityTested();
        funcTest1.setFunctionalityTestedId(1L);
        funcTest1.setName("FuncTest1");

        CertificationResultFunctionalityTested funcTest2 = new CertificationResultFunctionalityTested();
        funcTest2.setFunctionalityTestedId(2L);
        funcTest2.setName("FuncTest2");

        cert.getFunctionalitiesTested().add(funcTest1);
        cert.getFunctionalitiesTested().add(funcTest2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getFunctionalitiesTested().size());
    }

    @Test
    public void review_emptyFunctionalityTested_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        cert.getFunctionalitiesTested().clear();

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getFunctionalitiesTested().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultFunctionalityTested funcTest1 = new CertificationResultFunctionalityTested();
        funcTest1.setFunctionalityTestedId(1L);
        funcTest1.setName("FuncTest1");

        CertificationResultFunctionalityTested funcTest2 = new CertificationResultFunctionalityTested();
        funcTest2.setFunctionalityTestedId(2L);
        funcTest2.setName("FuncTest2");

        CertificationResultFunctionalityTested funcTest3 = new CertificationResultFunctionalityTested();
        funcTest3.setFunctionalityTestedId(1L);
        funcTest3.setName("FuncTest1");

        CertificationResultFunctionalityTested funcTest4 = new CertificationResultFunctionalityTested();
        funcTest4.setFunctionalityTestedId(3L);
        funcTest4.setName("FuncTest3");

        cert.getFunctionalitiesTested().add(funcTest1);
        cert.getFunctionalitiesTested().add(funcTest2);
        cert.getFunctionalitiesTested().add(funcTest3);
        cert.getFunctionalitiesTested().add(funcTest4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "FuncTest1")))
                .count());
        assertEquals(3, cert.getFunctionalitiesTested().size());
    }

    private CertificationResult getCertResult() {
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setNumber(CRITERION_NUMBER);
        CertificationResult cert = new CertificationResult();
        cert.setCriterion(criterion);
        return cert;
    }
}