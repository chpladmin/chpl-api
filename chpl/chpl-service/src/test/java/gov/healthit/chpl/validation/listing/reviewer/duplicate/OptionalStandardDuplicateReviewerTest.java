package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class OptionalStandardDuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String ERR_MSG =
            "Certification %s contains duplicate Optional Standard: '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private OptionalStandardDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateOptionalStandard"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), i.getArgument(2)));
        reviewer = new OptionalStandardDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertificationResult cert = getCertResult();

        CertificationResultOptionalStandard optionalStandard1 = new CertificationResultOptionalStandard();
        optionalStandard1.setOptionalStandardId(1L);
        optionalStandard1.setCitation("OptionalStandard1");

        CertificationResultOptionalStandard optionalStandard2 = new CertificationResultOptionalStandard();
        optionalStandard2.setOptionalStandardId(1L);
        optionalStandard2.setCitation("OptionalStandard1");

        cert.getOptionalStandards().add(optionalStandard1);
        cert.getOptionalStandards().add(optionalStandard2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "OptionalStandard1")))
                .count());
        assertEquals(1, cert.getOptionalStandards().size());
    }

    @Test
    public void review_duplicateNameExistsWithDifferentIds_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertificationResult cert = getCertResult();

        CertificationResultOptionalStandard optionalStandard1 = new CertificationResultOptionalStandard();
        optionalStandard1.setOptionalStandardId(1L);
        optionalStandard1.setCitation("OptionalStandard1");

        CertificationResultOptionalStandard optionalStandard2 = new CertificationResultOptionalStandard();
        optionalStandard2.setOptionalStandardId(2L);
        optionalStandard2.setCitation("OptionalStandard1");

        cert.getOptionalStandards().add(optionalStandard1);
        cert.getOptionalStandards().add(optionalStandard2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "OptionalStandard1")))
                .count());
        assertEquals(1, cert.getOptionalStandards().size());
    }

    @Test
    public void review_duplicateNameExistsWithNullIds_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertificationResult cert = getCertResult();

        CertificationResultOptionalStandard optionalStandard1 = new CertificationResultOptionalStandard();
        optionalStandard1.setOptionalStandardId(null);
        optionalStandard1.setCitation("OptionalStandard1");

        CertificationResultOptionalStandard optionalStandard2 = new CertificationResultOptionalStandard();
        optionalStandard2.setOptionalStandardId(null);
        optionalStandard2.setCitation("OptionalStandard1");

        cert.getOptionalStandards().add(optionalStandard1);
        cert.getOptionalStandards().add(optionalStandard2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "OptionalStandard1")))
                .count());
        assertEquals(1, cert.getOptionalStandards().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultOptionalStandard optionalStandard1 = new CertificationResultOptionalStandard();
        optionalStandard1.setOptionalStandardId(1L);
        optionalStandard1.setCitation("OptionalStandard1");

        CertificationResultOptionalStandard optionalStandard2 = new CertificationResultOptionalStandard();
        optionalStandard2.setOptionalStandardId(2L);
        optionalStandard2.setCitation("OptionalStandard2");

        cert.getOptionalStandards().add(optionalStandard1);
        cert.getOptionalStandards().add(optionalStandard2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getOptionalStandards().size());
    }

    @Test
    public void review_emptyOptionalStandards_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        cert.getOptionalStandards().clear();

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getOptionalStandards().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultOptionalStandard optionalStandard1 = new CertificationResultOptionalStandard();
        optionalStandard1.setOptionalStandardId(1L);
        optionalStandard1.setCitation("OptionalStandard1");

        CertificationResultOptionalStandard optionalStandard2 = new CertificationResultOptionalStandard();
        optionalStandard2.setOptionalStandardId(2L);
        optionalStandard2.setCitation("OptionalStandard2");

        CertificationResultOptionalStandard optionalStandard3 = new CertificationResultOptionalStandard();
        optionalStandard3.setOptionalStandardId(1L);
        optionalStandard3.setCitation("OptionalStandard1");

        CertificationResultOptionalStandard optionalStandard4 = new CertificationResultOptionalStandard();
        optionalStandard4.setOptionalStandardId(4L);
        optionalStandard4.setCitation("OptionalStandard4");

        cert.getOptionalStandards().add(optionalStandard1);
        cert.getOptionalStandards().add(optionalStandard2);
        cert.getOptionalStandards().add(optionalStandard3);
        cert.getOptionalStandards().add(optionalStandard4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "OptionalStandard1")))
                .count());
        assertEquals(3, cert.getOptionalStandards().size());
    }

    private CertificationResult getCertResult() {
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setNumber(CRITERION_NUMBER);
        CertificationResult cert = new CertificationResult();
        cert.setCriterion(criterion);
        return cert;
    }
}