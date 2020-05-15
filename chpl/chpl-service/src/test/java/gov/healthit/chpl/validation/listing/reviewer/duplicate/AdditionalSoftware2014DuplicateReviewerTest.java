package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.duplicate.AdditionalSoftware2014DuplicateReviewer;

public class AdditionalSoftware2014DuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.314 (a)(1)";
    private static final String ERR_MSG_CP =
            "Certification %s contains duplicate Additional Software: CP Source '%s'. The duplicates have been removed.";
    private static final String ERR_MSG_NONCP =
            "Certification %s contains duplicate Additional Software: Non CP Source: '%s', Version '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private AdditionalSoftware2014DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateAdditionalSoftwareCP.2014"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG_CP, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateAdditionalSoftwareNonCP.2014"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG_NONCP, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        reviewer = new AdditionalSoftware2014DuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateCpExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultAdditionalSoftware as1 = new CertificationResultAdditionalSoftware();
        as1.setCertifiedProductNumber("Chpl1");

        CertificationResultAdditionalSoftware as2 = new CertificationResultAdditionalSoftware();
        as2.setCertifiedProductNumber("Chpl1");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG_CP, CRITERION_NUMBER, "Chpl1")))
                .count());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateCps_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultAdditionalSoftware as1 = new CertificationResultAdditionalSoftware();
        as1.setCertifiedProductNumber("Chpl1");

        CertificationResultAdditionalSoftware as2 = new CertificationResultAdditionalSoftware();
        as2.setCertifiedProductNumber("Chpl2");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_emptyTestTools_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        cert.getAdditionalSoftware().clear();
        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_duplicateCpsExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultAdditionalSoftware as1 = new CertificationResultAdditionalSoftware();
        as1.setCertifiedProductNumber("Chpl1");

        CertificationResultAdditionalSoftware as2 = new CertificationResultAdditionalSoftware();
        as2.setCertifiedProductNumber("Chpl2");

        CertificationResultAdditionalSoftware as3 = new CertificationResultAdditionalSoftware();
        as3.setCertifiedProductNumber("Chpl1");

        CertificationResultAdditionalSoftware as4 = new CertificationResultAdditionalSoftware();
        as4.setCertifiedProductNumber("Chpl3");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);
        cert.getAdditionalSoftware().add(as3);
        cert.getAdditionalSoftware().add(as4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG_CP, CRITERION_NUMBER, "Chpl1")))
                .count());
        assertEquals(3, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_duplicateNonCpExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultAdditionalSoftware as1 = new CertificationResultAdditionalSoftware();
        as1.setName("Chpl1");
        as1.setVersion("v1");

        CertificationResultAdditionalSoftware as2 = new CertificationResultAdditionalSoftware();
        as2.setName("Chpl1");
        as2.setVersion("v1");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG_NONCP, CRITERION_NUMBER, "Chpl1", "v1")))
                .count());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateNonCps_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = new CertificationResult();

        CertificationResultAdditionalSoftware as1 = new CertificationResultAdditionalSoftware();
        as1.setName("Chpl1");
        as1.setVersion("v1");

        CertificationResultAdditionalSoftware as2 = new CertificationResultAdditionalSoftware();
        as1.setName("Chpl2");
        as1.setVersion("v2");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_duplicateNonCpsExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();

        CertificationResultAdditionalSoftware as1 = new CertificationResultAdditionalSoftware();
        as1.setName("Chpl1");
        as1.setVersion("v1");

        CertificationResultAdditionalSoftware as2 = new CertificationResultAdditionalSoftware();
        as2.setName("Chpl2");
        as2.setVersion("v2");

        CertificationResultAdditionalSoftware as3 = new CertificationResultAdditionalSoftware();
        as3.setName("Chpl1");
        as3.setVersion("v1");

        CertificationResultAdditionalSoftware as4 = new CertificationResultAdditionalSoftware();
        as4.setName("Chpl3");
        as4.setVersion("v3");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);
        cert.getAdditionalSoftware().add(as3);
        cert.getAdditionalSoftware().add(as4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG_NONCP, CRITERION_NUMBER, "Chpl1", "v1")))
                .count());
        assertEquals(3, cert.getAdditionalSoftware().size());
    }

    private CertificationResult getCertResult() {
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setNumber(CRITERION_NUMBER);
        CertificationResult cert = new CertificationResult();
        cert.setCriterion(criterion);
        return cert;
    }
}