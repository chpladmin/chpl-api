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

public class CqmAttestedCriteriaReviewerTest {
    private static final String MISSING_CRITERION = "Clinical Quality Measurement %s was found under Certification criterion %s, but the product does not attest to that criterion.";
    private CertificationCriterion c1;
    private CertificationCriterion c1Cures;
    private CertificationCriterion c2;

    private CertificationCriterionService criteriaService;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;
    private CqmAttestedCriteriaReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        c1 = buildCriterion(1L, "170.315 (c)(1)", "C1");
        c1Cures = buildCriterion(2L, "170.315 (c)(1)", "C1 (Cures Update)");
        c2 = buildCriterion(3L, "170.315 (c)(2)", "C2");

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        criteriaService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criteriaService.getByNumber(ArgumentMatchers.eq("170.315 (c)(1)")))
            .thenReturn(Stream.of(c1, c1Cures).collect(Collectors.toList()));
        Mockito.when(criteriaService.getByNumber(ArgumentMatchers.eq("170.315 (c)(2)")))
            .thenReturn(Stream.of(c2).collect(Collectors.toList()));
        validationUtils = new ValidationUtils(criteriaService);

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingCriteriaForCqm"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_CRITERION, i.getArgument(1), i.getArgument(2)));
        reviewer = new CqmAttestedCriteriaReviewer(criteriaService, validationUtils, msgUtil);
    }

    @Test
    public void review_nullCqms_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        listing.setCqmResults(null);
        reviewer.review(listing);

        assertNull(listing.getCqmResults());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_EmptyCqms_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listing);

        assertNotNull(listing.getCqmResults());
        assertEquals(0, listing.getCqmResults().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_SingleCqmWithEmptyListingCertifiationResults_hasError() {
        CQMResultDetails cqmResult = CQMResultDetails.builder()
                .cmsId("CMS1")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .cqmResults(Stream.of(cqmResult).collect(Collectors.toList()))
                .build();
        assertNotNull(listing.getCertificationResults());
        assertEquals(0, listing.getCertificationResults().size());

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_CRITERION, "CMS1", c1.getNumber())));
    }

    @Test
    public void review_MultipleCqmsWithEmptyListingCertifiationResults_hasErrors() {
        CQMResultDetails cqmResult1 = CQMResultDetails.builder()
                .cmsId("CMS1")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();
        CQMResultDetails cqmResult2 = CQMResultDetails.builder()
                .cmsId("CMS2")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .cqmResults(Stream.of(cqmResult1, cqmResult2).collect(Collectors.toList()))
                .build();
        assertNotNull(listing.getCertificationResults());
        assertEquals(0, listing.getCertificationResults().size());

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_CRITERION, "CMS1", c1.getNumber())));
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_CRITERION, "CMS2", c1.getNumber())));
    }

    @Test
    public void review_MultipleCqmsListingDoesNotAttestToCriteria_hasErrors() {
        CQMResultDetails cqmResult1 = CQMResultDetails.builder()
                .cmsId("CMS1")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();
        CQMResultDetails cqmResult2 = CQMResultDetails.builder()
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
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(c1)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(c1Cures)
                        .build())
                .cqmResults(Stream.of(cqmResult1, cqmResult2).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_CRITERION, "CMS1", c1.getNumber())));
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_CRITERION, "CMS2", c1.getNumber())));
    }

    @Test
    public void review_SingleCqmsListingAttestsToCriteria_noErrors() {
        CQMResultDetails cqmResult1 = CQMResultDetails.builder()
                .cmsId("CMS1")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(c2)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(c1)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(c1Cures)
                        .build())
                .cqmResults(Stream.of(cqmResult1).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_SingleCqmsListingAttestsToCuresCriteria_noErrors() {
        CQMResultDetails cqmResult1 = CQMResultDetails.builder()
                .cmsId("CMS1")
                .criteria(Stream.of(CQMResultCertification.builder()
                        .certificationId(c1.getId())
                        .certificationNumber(c1.getNumber())
                        .build()).collect(Collectors.toList()))
                .build();

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(c2)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(c1)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(c1Cures)
                        .build())
                .cqmResults(Stream.of(cqmResult1).collect(Collectors.toList()))
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    private CertificationCriterion buildCriterion(Long id, String number, String title) {
        return CertificationCriterion.builder()
                .id(id)
                .number(number)
                .title(title)
                .build();
    }
}
