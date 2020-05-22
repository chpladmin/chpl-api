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

public class AdditionalSoftwareDuplicateReviewerTest {
    private static final String CRITERION_NUMBER = "170.315 (a)(1)";
    private static final String WARNING_MSG_CP =
            "Certification %s contains duplicate Additional Software: CP Source '%s', Grouping '%s'. The duplicates have been removed.";
    private static final String WARNING_MSG_NONCP =
            "Certification %s contains duplicate Additional Software: Non CP Source: '%s', Version '%s', Grouping '%s'.  The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private AdditionalSoftwareDuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateAdditionalSoftwareCP"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(WARNING_MSG_CP, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.duplicateAdditionalSoftwareNonCP"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(WARNING_MSG_NONCP, i.getArgument(1), i.getArgument(2),
                        i.getArgument(3), i.getArgument(4)));

        reviewer = new AdditionalSoftwareDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateCpExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultAdditionalSoftware as1 = getAdditionalSoftwareListing(1L, "Chpl1", "a");
        CertificationResultAdditionalSoftware as2 = getAdditionalSoftwareListing(1L, "Chpl1", "a");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(WARNING_MSG_CP, CRITERION_NUMBER, "Chpl1", "a")))
                .count());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_duplicateCpExists_noChplId_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultAdditionalSoftware as1 = getAdditionalSoftwareListing(null, "Chpl1", "a");
        CertificationResultAdditionalSoftware as2 = getAdditionalSoftwareListing(null, "Chpl1", "a");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(WARNING_MSG_CP, CRITERION_NUMBER, "Chpl1", "a")))
                .count());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_duplicateCpExists_noChplIdNoGroup_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultAdditionalSoftware as1 = getAdditionalSoftwareListing(null, "Chpl1", null);
        CertificationResultAdditionalSoftware as2 = getAdditionalSoftwareListing(null, "Chpl1", null);
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(WARNING_MSG_CP, CRITERION_NUMBER, "Chpl1", "")))
                .count());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateCps_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultAdditionalSoftware as1 = getAdditionalSoftwareListing(1L, "Chpl1", "a");
        CertificationResultAdditionalSoftware as2 = getAdditionalSoftwareListing(2L, "Chpl2", "b");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateCps_noChplId_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultAdditionalSoftware as1 = getAdditionalSoftwareListing(null, "Chpl1", "a");
        CertificationResultAdditionalSoftware as2 = getAdditionalSoftwareListing(null, "Chpl1", "b");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateCps_noChplIdNoGroup_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultAdditionalSoftware as1 = getAdditionalSoftwareListing(null, "Chpl1", null);
        CertificationResultAdditionalSoftware as2 = getAdditionalSoftwareListing(null, "Chpl2", null);
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_emptyAdditionalSoftware_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        cert.getAdditionalSoftware().clear();
        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_duplicateCpsExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultAdditionalSoftware as1 = getAdditionalSoftwareListing(null, "Chpl1", "a");
        CertificationResultAdditionalSoftware as2 = getAdditionalSoftwareListing(null, "Chpl2", "a");
        CertificationResultAdditionalSoftware as3 = getAdditionalSoftwareListing(null, "Chpl1", "a");
        CertificationResultAdditionalSoftware as4 = getAdditionalSoftwareListing(null, "Chpl1", "b");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);
        cert.getAdditionalSoftware().add(as3);
        cert.getAdditionalSoftware().add(as4);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(WARNING_MSG_CP, CRITERION_NUMBER, "Chpl1", "a")))
                .count());
        assertEquals(3, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_duplicateNonCpExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultAdditionalSoftware as1 = getAdditionalSoftwareOther("Excel", "365", "a");
        CertificationResultAdditionalSoftware as2 = getAdditionalSoftwareOther("Excel", "365", "a");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(WARNING_MSG_NONCP, CRITERION_NUMBER, "Excel", "365", "a")))
                .count());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_duplicateNonCpExists_noVersion_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultAdditionalSoftware as1 = getAdditionalSoftwareOther("Excel", null, "a");
        CertificationResultAdditionalSoftware as2 = getAdditionalSoftwareOther("Excel", null, "a");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(WARNING_MSG_NONCP, CRITERION_NUMBER, "Excel", "", "a")))
                .count());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_duplicateNonCpExists_noVersionNoGroup_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultAdditionalSoftware as1 = getAdditionalSoftwareOther("Excel", null, null);
        CertificationResultAdditionalSoftware as2 = getAdditionalSoftwareOther("Excel", null, null);
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(WARNING_MSG_NONCP, CRITERION_NUMBER, "Excel", "", "")))
                .count());
        assertEquals(1, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateNonCps_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultAdditionalSoftware as1 = getAdditionalSoftwareOther("Excel", "365", "a");
        CertificationResultAdditionalSoftware as2 = getAdditionalSoftwareOther("Excel", "366", "a");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateNonCps_noVersion_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultAdditionalSoftware as1 = getAdditionalSoftwareOther("Excel", null, "a");
        CertificationResultAdditionalSoftware as2 = getAdditionalSoftwareOther("Word", null, "a");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_noDuplicateNonCps_noVersionNoGroup_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultAdditionalSoftware as1 = getAdditionalSoftwareOther("Excel", null, null);
        CertificationResultAdditionalSoftware as2 = getAdditionalSoftwareOther("Word", null, null);
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, cert.getAdditionalSoftware().size());
    }

    @Test
    public void review_duplicateNonCpsExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertificationResult cert = getCertResult();
        CertificationResultAdditionalSoftware as1 = getAdditionalSoftwareOther("Excel", "365", "a");
        CertificationResultAdditionalSoftware as2 = getAdditionalSoftwareOther("Excel", "365", "b");
        CertificationResultAdditionalSoftware as3 = getAdditionalSoftwareOther("Excel", "365", "a");
        CertificationResultAdditionalSoftware as4 = getAdditionalSoftwareOther("Word", "365", "b");
        cert.getAdditionalSoftware().add(as1);
        cert.getAdditionalSoftware().add(as2);
        cert.getAdditionalSoftware().add(as3);
        cert.getAdditionalSoftware().add(as4);

        reviewer.review(listing, cert);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(WARNING_MSG_NONCP, CRITERION_NUMBER, "Excel", "365", "a")))
                .count());
        assertEquals(3, cert.getAdditionalSoftware().size());
    }

    private CertificationResultAdditionalSoftware getAdditionalSoftwareListing(Long chplId,
            String chplProductNumber, String grouping) {
        CertificationResultAdditionalSoftware addtlSoftware = new CertificationResultAdditionalSoftware();
        addtlSoftware.setCertifiedProductId(chplId);
        addtlSoftware.setCertifiedProductNumber(chplProductNumber);
        addtlSoftware.setGrouping(grouping);
        return addtlSoftware;
    }

    private CertificationResultAdditionalSoftware getAdditionalSoftwareOther(String name, String version, String grouping) {
        CertificationResultAdditionalSoftware addtlSoftware = new CertificationResultAdditionalSoftware();
        addtlSoftware.setName(name);
        addtlSoftware.setVersion(version);
        addtlSoftware.setGrouping(grouping);
        return addtlSoftware;
    }

    private CertificationResult getCertResult() {
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setNumber(CRITERION_NUMBER);
        CertificationResult cert = new CertificationResult();
        cert.setCriterion(criterion);
        return cert;
    }
}