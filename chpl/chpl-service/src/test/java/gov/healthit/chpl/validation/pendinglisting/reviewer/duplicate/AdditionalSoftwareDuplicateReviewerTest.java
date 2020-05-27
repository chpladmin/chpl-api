package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.AdditionalSoftwareDuplicateReviewer;

public class AdditionalSoftwareDuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String ERR_MSG_CP =
            "Certification %s contains duplicate Additional Software: CP Source '%s', Grouping '%s'. The duplicates have been removed.";
    private static final String ERR_MSG_NONCP =
            "Certification %s contains duplicate Additional Software: Non CP Source: '%s', Version '%s', Grouping '%s'.  The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private AdditionalSoftwareDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateAdditionalSoftwareCP"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG_CP, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateAdditionalSoftwareNonCP"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG_NONCP, i.getArgument(1), i.getArgument(2),
                        i.getArgument(3), i.getArgument(4)));
        reviewer = new AdditionalSoftwareDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateCpExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultAdditionalSoftwareDTO as1 = getAdditionalSoftwareListing(1L, "Chpl1", "a");
        PendingCertificationResultAdditionalSoftwareDTO as2 = getAdditionalSoftwareListing(1L, "Chpl1", "a");
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
    public void review_duplicateCpExistsNoChplId_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultAdditionalSoftwareDTO as1 = getAdditionalSoftwareListing(null, "Chpl1", "a");
        PendingCertificationResultAdditionalSoftwareDTO as2 = getAdditionalSoftwareListing(null, "Chpl1", "a");
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
    public void review_duplicateCpExists_NoChplIdNoGroup_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultAdditionalSoftwareDTO as1 = getAdditionalSoftwareListing(null, "Chpl1", null);
        PendingCertificationResultAdditionalSoftwareDTO as2 = getAdditionalSoftwareListing(null, "Chpl1", null);
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG_CP, CRITERION_NUMBER, "Chpl1", "")))
                .count());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateCps_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultAdditionalSoftwareDTO as1 = getAdditionalSoftwareListing(1L, "Chpl1", "a");
        PendingCertificationResultAdditionalSoftwareDTO as2 = getAdditionalSoftwareListing(2L, "Chpl2", "b");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateCpsNoChplId_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultAdditionalSoftwareDTO as1 = getAdditionalSoftwareListing(null, "Chpl1", "a");
        PendingCertificationResultAdditionalSoftwareDTO as2 = getAdditionalSoftwareListing(null, "Chpl2", "b");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateCpsNoChplIdNoGroup_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultAdditionalSoftwareDTO as1 = getAdditionalSoftwareListing(null, "Chpl1", null);
        PendingCertificationResultAdditionalSoftwareDTO as2 = getAdditionalSoftwareListing(null, "Chpl2", null);
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_emptyAdditionalSoftware_noWarning() {
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
        PendingCertificationResultAdditionalSoftwareDTO as1 = getAdditionalSoftwareListing(null, "Chpl1", "a");
        PendingCertificationResultAdditionalSoftwareDTO as2 = getAdditionalSoftwareListing(null, "Chpl2", "a");
        PendingCertificationResultAdditionalSoftwareDTO as3 = getAdditionalSoftwareListing(null, "Chpl1", "a");
        PendingCertificationResultAdditionalSoftwareDTO as4 = getAdditionalSoftwareListing(null, "Chpl3", "b");

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
        PendingCertificationResultAdditionalSoftwareDTO as1 = getAdditionalSoftwareOther("Excel", "365", "a");
        PendingCertificationResultAdditionalSoftwareDTO as2 = getAdditionalSoftwareOther("Excel", "365", "a");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG_NONCP, CRITERION_NUMBER, "Excel", "365", "a")))
                .count());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_duplicateNonCpExists_NoVersion_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultAdditionalSoftwareDTO as1 = getAdditionalSoftwareOther("Excel", null, "a");
        PendingCertificationResultAdditionalSoftwareDTO as2 = getAdditionalSoftwareOther("Excel", null, "a");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG_NONCP, CRITERION_NUMBER, "Excel", "", "a")))
                .count());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_duplicateNonCpExists_NoVersionNoGroup_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultAdditionalSoftwareDTO as1 = getAdditionalSoftwareOther("Excel", null, null);
        PendingCertificationResultAdditionalSoftwareDTO as2 = getAdditionalSoftwareOther("Excel", null, null);
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG_NONCP, CRITERION_NUMBER, "Excel", "", "")))
                .count());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateNonCps_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultAdditionalSoftwareDTO as1 = getAdditionalSoftwareOther("Excel", "365", "a");
        PendingCertificationResultAdditionalSoftwareDTO as2 = getAdditionalSoftwareOther("Excel", "366", "a");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateNonCps_NoVersion_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultAdditionalSoftwareDTO as1 = getAdditionalSoftwareOther("Excel", null, "a");
        PendingCertificationResultAdditionalSoftwareDTO as2 = getAdditionalSoftwareOther("Word", null, "a");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateNonCps_NoVersionNoGroup_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO cert = getCertResult();
        PendingCertificationResultAdditionalSoftwareDTO as1 = getAdditionalSoftwareOther("Excel", null, null);
        PendingCertificationResultAdditionalSoftwareDTO as2 = getAdditionalSoftwareOther("Word", null, null);
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
        PendingCertificationResultAdditionalSoftwareDTO as1 = getAdditionalSoftwareOther("Excel", "365", "a");
        PendingCertificationResultAdditionalSoftwareDTO as2 = getAdditionalSoftwareOther("Excel", "365", "b");
        PendingCertificationResultAdditionalSoftwareDTO as3 = getAdditionalSoftwareOther("Excel", "365", "a");
        PendingCertificationResultAdditionalSoftwareDTO as4 = getAdditionalSoftwareOther("Word", "365", "b");

        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);
        cert.getAdditionalSoftware().add(as3);
        cert.getAdditionalSoftware().add(as4);

        reviewer.review(listing, cert);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG_NONCP, CRITERION_NUMBER, "Excel", "365", "a")))
                .count());
        assertEquals(3, cert.getAdditionalSoftware().size());
    }

    private PendingCertificationResultAdditionalSoftwareDTO getAdditionalSoftwareListing(Long chplId,
            String chplProductNumber, String grouping) {
        PendingCertificationResultAdditionalSoftwareDTO addtlSoftware = new PendingCertificationResultAdditionalSoftwareDTO();
        addtlSoftware.setCertifiedProductId(chplId);
        addtlSoftware.setChplId(chplProductNumber);
        addtlSoftware.setGrouping(grouping);
        return addtlSoftware;
    }

    private PendingCertificationResultAdditionalSoftwareDTO getAdditionalSoftwareOther(String name, String version, String grouping) {
        PendingCertificationResultAdditionalSoftwareDTO addtlSoftware = new PendingCertificationResultAdditionalSoftwareDTO();
        addtlSoftware.setName(name);
        addtlSoftware.setVersion(version);
        addtlSoftware.setGrouping(grouping);
        return addtlSoftware;
    }

    private PendingCertificationResultDTO getCertResult() {
        CertificationCriterionDTO criterion = new CertificationCriterionDTO();
        criterion.setNumber(CRITERION_NUMBER);
        PendingCertificationResultDTO cert = new PendingCertificationResultDTO();
        cert.setCriterion(criterion);
        return cert;
    }
}