package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.AdditionalSoftware2015DuplicateReviewer;

public class AdditionalSoftware2015DuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String ERR_MSG_CP =
            "Certification %s contains duplicate Additional Software: CP Source '%s', Grouping '%s'. The duplicates have been removed.";
    private static final String ERR_MSG_NONCP =
            "Certification %s contains duplicate Additional Software: Non CP Source: '%s', Version '%s', Grouping '%s'.  The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private AdditionalSoftware2015DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateAdditionalSoftwareCP.2015"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG_CP, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateAdditionalSoftwareNonCP.2015"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG_NONCP, i.getArgument(1), i.getArgument(2),
                        i.getArgument(3), i.getArgument(4)));
        reviewer = new AdditionalSoftware2015DuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateCpExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultAdditionalSoftwareDTO as1 = new PendingCertificationResultAdditionalSoftwareDTO();
        as1.setChplId("Chpl1");
        as1.setGrouping("a");

        PendingCertificationResultAdditionalSoftwareDTO as2 = new PendingCertificationResultAdditionalSoftwareDTO();
        as2.setChplId("Chpl1");
        as2.setGrouping("a");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG_CP, CRITERION_NUMBER, "Chpl1", "a")))
                .count());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateCps_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultAdditionalSoftwareDTO as1 = new PendingCertificationResultAdditionalSoftwareDTO();
        as1.setChplId("Chpl1");
        as1.setGrouping("a");

        PendingCertificationResultAdditionalSoftwareDTO as2 = new PendingCertificationResultAdditionalSoftwareDTO();
        as2.setChplId("Chpl1");
        as2.setGrouping("b");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_emptyTestTools_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        cert.getAdditionalSoftware().clear();
        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_duplicateCpsExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultAdditionalSoftwareDTO as1 = new PendingCertificationResultAdditionalSoftwareDTO();
        as1.setChplId("Chpl1");
        as1.setGrouping("a");

        PendingCertificationResultAdditionalSoftwareDTO as2 = new PendingCertificationResultAdditionalSoftwareDTO();
        as2.setChplId("Chpl2");
        as2.setGrouping("a");

        PendingCertificationResultAdditionalSoftwareDTO as3 = new PendingCertificationResultAdditionalSoftwareDTO();
        as3.setChplId("Chpl1");
        as3.setGrouping("a");

        PendingCertificationResultAdditionalSoftwareDTO as4 = new PendingCertificationResultAdditionalSoftwareDTO();
        as4.setChplId("Chpl3");
        as4.setGrouping("b");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);
        cert.getAdditionalSoftware().add(as3);
        cert.getAdditionalSoftware().add(as4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG_CP, CRITERION_NUMBER, "Chpl1", "a")))
                .count());
        assertEquals(3, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_duplicateNonCpExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultAdditionalSoftwareDTO as1 = new PendingCertificationResultAdditionalSoftwareDTO();
        as1.setName("Chpl1");
        as1.setGrouping("a");
        as1.setVersion("v1");

        PendingCertificationResultAdditionalSoftwareDTO as2 = new PendingCertificationResultAdditionalSoftwareDTO();
        as2.setName("Chpl1");
        as2.setGrouping("a");
        as2.setVersion("v1");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG_NONCP, CRITERION_NUMBER, "Chpl1", "v1", "a")))
                .count());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateNonCps_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultAdditionalSoftwareDTO as1 = new PendingCertificationResultAdditionalSoftwareDTO();
        as1.setName("Chpl1");
        as1.setGrouping("a");
        as1.setVersion("v1");

        PendingCertificationResultAdditionalSoftwareDTO as2 = new PendingCertificationResultAdditionalSoftwareDTO();
        as1.setName("Chpl2");
        as1.setGrouping("a");
        as1.setVersion("v2");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_duplicateNonCpsExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();

        PendingCertificationResultAdditionalSoftwareDTO as1 = new PendingCertificationResultAdditionalSoftwareDTO();
        as1.setName("Chpl1");
        as1.setGrouping("a");
        as1.setVersion("v1");

        PendingCertificationResultAdditionalSoftwareDTO as2 = new PendingCertificationResultAdditionalSoftwareDTO();
        as2.setName("Chpl2");
        as2.setGrouping("b");
        as2.setVersion("v2");

        PendingCertificationResultAdditionalSoftwareDTO as3 = new PendingCertificationResultAdditionalSoftwareDTO();
        as3.setName("Chpl1");
        as3.setGrouping("a");
        as3.setVersion("v1");

        PendingCertificationResultAdditionalSoftwareDTO as4 = new PendingCertificationResultAdditionalSoftwareDTO();
        as4.setName("Chpl3");
        as4.setGrouping("a");
        as4.setVersion("v3");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);
        cert.getAdditionalSoftware().add(as3);
        cert.getAdditionalSoftware().add(as4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG_NONCP, CRITERION_NUMBER, "Chpl1", "v1", "a")))
                .count());
        assertEquals(3, cert.getAdditionalSoftware().size());
    }

    private PendingCertificationResultDTO getCertResult() {
        CertificationCriterionDTO criterion = new CertificationCriterionDTO();
        criterion.setNumber(CRITERION_NUMBER);
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();
        cert.setCriterion(criterion);
        return cert;
    }
}