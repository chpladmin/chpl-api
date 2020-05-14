package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestFunctionalityDuplicateReviewer;

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
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultTestFunctionalityDTO testFunc1 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc1.setNumber("TestFunc1");

        PendingCertificationResultTestFunctionalityDTO testFunc2 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc2.setNumber("TestFunc1");

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
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultTestFunctionalityDTO testFunc1 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc1.setNumber("TestFunc1");

        PendingCertificationResultTestFunctionalityDTO testFunc2 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc2.setNumber("TestFunc2");

        cert.getTestFunctionality().add(testFunc1);
        cert.getTestFunctionality().add(testFunc2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getTestFunctionality().size());
    }

    @Test
    public void review_emptyTestFunctionality_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        cert.getTestFunctionality().clear();

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getTestFunctionality().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultTestFunctionalityDTO testFunc1 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc1.setNumber("TestFunc1");

        PendingCertificationResultTestFunctionalityDTO testFunc2 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc2.setNumber("TestFunc2");

        PendingCertificationResultTestFunctionalityDTO testFunc3 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc3.setNumber("TestFunc1");

        PendingCertificationResultTestFunctionalityDTO testFunc4 = new PendingCertificationResultTestFunctionalityDTO();
        testFunc4.setNumber("TestFunc3");

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

    private PendingCertificationResultDTO getCertResult() {
        CertificationCriterionDTO criterion = new CertificationCriterionDTO();
        criterion.setNumber(CRITERION_NUMBER);
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();
        cert.setCriterion(criterion);
        return cert;
    }
}