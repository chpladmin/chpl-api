package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SvapReviewerTest {
    private static final String SVAPS_NOT_APPLICABLE = "Standards Version Advancement Process(es) are not applicable for the criterion %s. They have been removed.";
    private static final String SVAP_NOT_FOUND = "Standards Version Advancement Process %s is not valid for criteria %s.";
    private static final String SVAP_NAME_MISSING = "There was no Regulatory Text Citation found for SVAP(s) on criteria %s.";
    private static final String SVAP_REPLACED = "Standards Version Advancement Process %s for criteria %s has been replaced.";

    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;
    private SvapReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        certResultRules = Mockito.mock(CertificationResultRules.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.svapsNotApplicable"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(SVAPS_NOT_APPLICABLE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.svap.invalidCriteria"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(SVAP_NOT_FOUND, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.svap.missingCitation"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(SVAP_NAME_MISSING, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.svap.replaced"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(SVAP_REPLACED, i.getArgument(1), i.getArgument(2)));

        reviewer = new SvapReviewer(certResultRules, msgUtil);
    }

    @Test
    public void review_nullSvaps_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.SVAP)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .build())
                .build();
        listing.getCertificationResults().get(0).setSvaps(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptySvaps_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.SVAP)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .svaps(new ArrayList<CertificationResultSvap>())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void svapsNotApplicableToCriteria_hasWarningAndStandardsSetNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.SVAP)))
            .thenReturn(false);
        List<CertificationResultSvap> svaps = new ArrayList<CertificationResultSvap>();
        svaps.add(CertificationResultSvap.builder()
                .regulatoryTextCitation("svap1")
                .svapId(1L)
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .svaps(svaps)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(SVAPS_NOT_APPLICABLE, "170.315 (a)(1)")));
        assertNull(listing.getCertificationResults().get(0).getSvaps());
    }

    @Test
    public void review_svapWithoutId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.SVAP)))
            .thenReturn(true);

        List<CertificationResultSvap> svaps = new ArrayList<CertificationResultSvap>();
        svaps.add(CertificationResultSvap.builder()
                .svapId(1L)
                .regulatoryTextCitation("svap1")
                .build());
        svaps.add(CertificationResultSvap.builder()
                .regulatoryTextCitation("bad name")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .svaps(svaps)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(SVAP_NOT_FOUND, "bad name", "170.315 (a)(1)")));
    }

    @Test
    public void review_svapWithoutCitationAndWithoutId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.SVAP)))
            .thenReturn(true);

        List<CertificationResultSvap> svaps = new ArrayList<CertificationResultSvap>();
        svaps.add(CertificationResultSvap.builder()
                .svapId(1L)
                .regulatoryTextCitation("svap1")
                .build());
        svaps.add(CertificationResultSvap.builder()
                .svapId(null)
                .regulatoryTextCitation("")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .svaps(svaps)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(SVAP_NAME_MISSING, "170.315 (a)(1)", "")));
    }

    @Test
    public void review_svapsWithoutCitationWithId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.SVAP)))
        .thenReturn(true);

        List<CertificationResultSvap> svaps = new ArrayList<CertificationResultSvap>();
        svaps.add(CertificationResultSvap.builder()
                .svapId(1L)
                .regulatoryTextCitation("svap1")
                .build());
        svaps.add(CertificationResultSvap.builder()
                .svapId(2L)
                .regulatoryTextCitation("")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .svaps(svaps)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(SVAP_NAME_MISSING, "170.315 (a)(1)", "")));
    }

    @Test
    public void review_validSvapMarkedReplaced_hasWarning() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.SVAP)))
        .thenReturn(true);

        List<CertificationResultSvap> svaps = new ArrayList<CertificationResultSvap>();
        svaps.add(CertificationResultSvap.builder()
                .svapId(1L)
                .regulatoryTextCitation("svap1")
                .replaced(false)
                .build());
        svaps.add(CertificationResultSvap.builder()
                .svapId(2L)
                .regulatoryTextCitation("svap2")
                .replaced(true)
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .svaps(svaps)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(SVAP_REPLACED, "svap2", "170.315 (a)(1)")));
    }

    @Test
    public void review_validSvap_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.SVAP)))
            .thenReturn(true);

        List<CertificationResultSvap> svaps = new ArrayList<CertificationResultSvap>();
        svaps.add(CertificationResultSvap.builder()
                .svapId(1L)
                .regulatoryTextCitation("svap1")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .svaps(svaps)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    private Map<String, Object> create2015EditionMap() {
        Map<String, Object> editionMap = new HashMap<String, Object>();
        editionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        editionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        return editionMap;
    }
}
