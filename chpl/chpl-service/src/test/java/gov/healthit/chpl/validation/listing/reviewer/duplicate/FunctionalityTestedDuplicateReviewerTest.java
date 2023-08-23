package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.criteriaattribute.functionalitytested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.criteriaattribute.functionalitytested.FunctionalityTested;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class FunctionalityTestedDuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String ERR_MSG =
            "Certification %s contains duplicate Functionality Tested: Number '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private FunctionalityTestedDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateFunctionalityTested"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), i.getArgument(2)));
        reviewer = new FunctionalityTestedDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultFunctionalityTested funcTest2 = CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(2L)
                        .regulatoryTextCitation("FuncTest2")
                        .build())
                .build();

        cert.getFunctionalitiesTested().add(funcTest2);
        cert.getFunctionalitiesTested().add(funcTest2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "FuncTest2")))
                .count());
        assertEquals(1, cert.getFunctionalitiesTested().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultFunctionalityTested funcTest1 = CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(1L)
                        .regulatoryTextCitation("FuncTest1")
                        .build())
                .build();

        CertificationResultFunctionalityTested funcTest2 = CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(2L)
                        .regulatoryTextCitation("FuncTest2")
                        .build())
                .build();

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

        CertificationResultFunctionalityTested funcTest1 = CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(1L)
                        .regulatoryTextCitation("FuncTest1")
                        .build())
                .build();

        CertificationResultFunctionalityTested funcTest2 = CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(2L)
                        .regulatoryTextCitation("FuncTest2")
                        .build())
                .build();

        CertificationResultFunctionalityTested funcTest3 = CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(1L)
                        .regulatoryTextCitation("FuncTest1")
                        .build())
                .build();

        CertificationResultFunctionalityTested funcTest4 = CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(3L)
                        .regulatoryTextCitation("FuncTest3")
                        .build())
                .build();

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