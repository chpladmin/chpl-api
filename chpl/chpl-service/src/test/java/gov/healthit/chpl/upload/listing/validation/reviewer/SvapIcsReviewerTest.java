package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class SvapIcsReviewerTest {
    private static final String SVAP_REPLACED = "Standards Version Advancement Process %s for criteria %s has been replaced and is only allowable for listings with ICS.";

    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;
    private SvapIcsReviewer reviewer;
    private CertificationEdition edition2015;

    @Before
    public void before() throws EntityRetrievalException {
        edition2015 = CertificationEdition.builder()
                .id(3L)
                .name("2015")
                .build();
        certResultRules = Mockito.mock(CertificationResultRules.class);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.SVAP)))
            .thenReturn(true);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.svap.replacedWithIcs"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(SVAP_REPLACED, i.getArgument(1), i.getArgument(2)));

        reviewer = new SvapIcsReviewer(certResultRules,
                new ValidationUtils(Mockito.mock(CertificationCriterionService.class)),
                msgUtil);
    }

    @Test
    public void review_nullSvaps_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
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
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
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
    public void review_validSvapMarkedReplaced_hasError() {
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
                .edition(edition2015)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .svaps(svaps)
                        .build())
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(SVAP_REPLACED, "svap2", "170.315 (a)(1)")));
    }

    @Test
    public void review_validSvapMarkedReplacedWithIcs_noError() {
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
                .edition(edition2015)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .svaps(svaps)
                        .build())
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_validSvapMarkedReplacedForRemovedCriteria_noWarning() {
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
                .edition(edition2015)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .svaps(svaps)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_validSvap_noError() {
        List<CertificationResultSvap> svaps = new ArrayList<CertificationResultSvap>();
        svaps.add(CertificationResultSvap.builder()
                .svapId(1L)
                .regulatoryTextCitation("svap1")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(edition2015)
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .svaps(svaps)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }
}
