package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class AttestedCriteriaCqmReviewerTest {
    private static final String MISSING_CQM = "Certification criterion '%s' was found but no matching Clinical Quality Measurement was found.";
    private CertificationCriterion c1;
    private CertificationCriterion c1Cures;
    private CertificationCriterion c2;
    private CertificationCriterion c3;

    private CertificationCriterionService criteriaService;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;
    private AttestedCriteriaCqmReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        c1 = buildCriterion(1L, "170.315 (c)(1)", "C1", true);
        c1Cures = buildCriterion(2L, "170.315 (c)(1)", "C1 (Cures Update)", false);
        c2 = buildCriterion(3L, "170.315 (c)(2)", "C2", false);
        c3 = buildCriterion(4L, "170.315 (c)(3)", "C3", false);

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(1L))).thenReturn(c1);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(2L))).thenReturn(c1Cures);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(3L))).thenReturn(c2);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(4L))).thenReturn(c3);

        validationUtils = new ValidationUtils(criteriaService);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingCqmForCriteria"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_CQM, i.getArgument(1), ""));
        reviewer = new AttestedCriteriaCqmReviewer(validationUtils, criteriaService, msgUtil, "1,2,3,4");
    }

    @Test
    public void review_nullCqmsNoCertificationResults_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        listing.setCqmResults(null);
        reviewer.review(listing);

        assertNull(listing.getCqmResults());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_EmptyCqmsNoCertificationResults_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listing);

        assertNotNull(listing.getCqmResults());
        assertEquals(0, listing.getCqmResults().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_EmptyCqmsUnattestedCertificationResult_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(c1)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_EmptyCqmsNotCqmCertificationResult_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(buildCriterion(5L, "170.315 (a)(1)", "A1", false))
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_ListingAttestsC5AndHasC1Cqm_noError() {
        CQMResultDetails cqmResult1 = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS1")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(buildCriterion(5L, "170.315 (a)(1)", "A1", false))
                        .build())
                .cqmResults(Stream.of(cqmResult1).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_ListingAttestsC2ButCqmsNeedC1_hasErrors() {
        CQMResultDetails cqmResult1 = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS1")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();
        CQMResultDetails cqmResult2 = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS2")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(c2)
                        .build())
                .cqmResults(Stream.of(cqmResult1, cqmResult2).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_CQM, "170.315 (c)(2)")));
    }

    @Test
    public void review_ListingAttestsC1RemovedAndHasNoCqms_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(c1)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_ListingAttestsC1RemovedAndCqmHasC2_noErrors() {
        CQMResultDetails cqmResult1 = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS1")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c2.getId())
                        .certificationNumber(c2.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();
        CQMResultDetails cqmResult2 = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS2")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c2.getId())
                        .certificationNumber(c2.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(c1)
                        .build())
                .cqmResults(Stream.of(cqmResult1, cqmResult2).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_ListingAttestsC1RemovedAndCqmHasC1_noErrors() {
        CQMResultDetails cqmResult1 = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS1")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();
        CQMResultDetails cqmResult2 = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS2")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(c1)
                        .build())
                .cqmResults(Stream.of(cqmResult1, cqmResult2).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_ListingAttestsC1CuresAndCqmHasC1_hasError() {
        CQMResultDetails cqmResult1 = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS1")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();
        CQMResultDetails cqmResult2 = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS2")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(c1Cures)
                        .build())
                .cqmResults(Stream.of(cqmResult1, cqmResult2).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_CQM, "170.315 (c)(1) (Cures Update)")));
    }

    @Test
    public void review_ListingAttestsC1CuresAndCqmHasC1Cures_noError() {
        CQMResultDetails cqmResult1 = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS1")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1Cures.getId())
                        .certificationNumber(c1Cures.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();
        CQMResultDetails cqmResult2 = CQMResultDetails.builder()
                .success(true)
                .cmsId("CMS2")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1Cures.getId())
                        .certificationNumber(c1Cures.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(c1Cures)
                        .build())
                .cqmResults(Stream.of(cqmResult1, cqmResult2).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    private CertificationCriterion buildCriterion(Long id, String number, String title, boolean removed) {
        return CertificationCriterion.builder()
                .id(id)
                .number(number)
                .title(title)
                .removed(removed)
                .build();
    }
}
