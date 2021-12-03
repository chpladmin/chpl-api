package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SvapDuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String ERR_MSG =
            "Certification %s contains duplicate SVAP: '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private SvapDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateSvap"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), i.getArgument(2)));
        reviewer = new SvapDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertificationResult cert = getCertResult();

        CertificationResultSvap svap1 = new CertificationResultSvap();
        svap1.setSvapId(1L);
        svap1.setRegulatoryTextCitation("Svap1");

        CertificationResultSvap svap2 = new CertificationResultSvap();
        svap2.setSvapId(1L);
        svap2.setRegulatoryTextCitation("Svap1");

        cert.getSvaps().add(svap1);
        cert.getSvaps().add(svap2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "Svap1")))
                .count());
        assertEquals(1, cert.getSvaps().size());
    }

    @Test
    public void review_duplicateNameExistsWithDifferentIds_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertificationResult cert = getCertResult();

        CertificationResultSvap svap1 = new CertificationResultSvap();
        svap1.setSvapId(1L);
        svap1.setRegulatoryTextCitation("Svap1");

        CertificationResultSvap svap2 = new CertificationResultSvap();
        svap2.setSvapId(2L);
        svap2.setRegulatoryTextCitation("Svap1");

        cert.getSvaps().add(svap1);
        cert.getSvaps().add(svap2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "Svap1")))
                .count());
        assertEquals(1, cert.getSvaps().size());
    }

    @Test
    public void review_duplicateNameExistsWithNullIds_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertificationResult cert = getCertResult();

        CertificationResultSvap svap1 = new CertificationResultSvap();
        svap1.setSvapId(null);
        svap1.setRegulatoryTextCitation("Svap1");

        CertificationResultSvap svap2 = new CertificationResultSvap();
        svap2.setSvapId(null);
        svap2.setRegulatoryTextCitation("Svap1");

        cert.getSvaps().add(svap1);
        cert.getSvaps().add(svap2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "Svap1")))
                .count());
        assertEquals(1, cert.getSvaps().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultSvap svap1 = new CertificationResultSvap();
        svap1.setSvapId(1L);
        svap1.setRegulatoryTextCitation("Svap1");

        CertificationResultSvap svap2 = new CertificationResultSvap();
        svap2.setSvapId(2L);
        svap2.setRegulatoryTextCitation("Svap2");

        cert.getSvaps().add(svap1);
        cert.getSvaps().add(svap2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getSvaps().size());
    }

    @Test
    public void review_emptyOptionalStandards_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        cert.getSvaps().clear();

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getSvaps().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultSvap svap1 = new CertificationResultSvap();
        svap1.setSvapId(1L);
        svap1.setRegulatoryTextCitation("Svap1");

        CertificationResultSvap svap2 = new CertificationResultSvap();
        svap2.setSvapId(2L);
        svap2.setRegulatoryTextCitation("Svap2");

        CertificationResultSvap svap3 = new CertificationResultSvap();
        svap3.setSvapId(1L);
        svap3.setRegulatoryTextCitation("Svap1");

        CertificationResultSvap svap4 = new CertificationResultSvap();
        svap4.setSvapId(4L);
        svap4.setRegulatoryTextCitation("Svap4");

        cert.getSvaps().add(svap1);
        cert.getSvaps().add(svap2);
        cert.getSvaps().add(svap3);
        cert.getSvaps().add(svap4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, CRITERION_NUMBER, "Svap1")))
                .count());
        assertEquals(3, cert.getSvaps().size());
    }

    private CertificationResult getCertResult() {
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setNumber(CRITERION_NUMBER);
        CertificationResult cert = new CertificationResult();
        cert.setCriterion(criterion);
        return cert;
    }
}