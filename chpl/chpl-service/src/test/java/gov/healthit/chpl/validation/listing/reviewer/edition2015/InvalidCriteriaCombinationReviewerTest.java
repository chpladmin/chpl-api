package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.InvalidCriteriaCombination;

public class InvalidCriteriaCombinationReviewerTest {
    private static final String INVALID_COMBINATION = "Cannot select both %s and %s.";

    private ErrorMessageUtil msgUtil;
    private InvalidCriteriaCombination invalidCriteriaCombination;
    private InvalidCriteriaCombinationReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        invalidCriteriaCombination = new InvalidCriteriaCombination(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                30, 31, 32, 33, 34);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidCombination"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(INVALID_COMBINATION, i.getArgument(1), i.getArgument(2)));
        reviewer = new InvalidCriteriaCombinationReviewer(invalidCriteriaCombination, msgUtil);
    }

    @Test
    public void review_emptyCertificationResults_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_noAttestedCertificationResults_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (b)(1)")
                                .title("Criteria B1 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(3L)
                                .number("170.315 (b)(1)")
                                .title("Criteria B1 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_nullAttestedCertificationResults_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(null)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (b)(1)")
                                .title("Criteria B1 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(3L)
                                .number("170.315 (b)(1)")
                                .title("Criteria B1 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_oneAttestedOriginalAndNoAttestedCuresCertificationResults_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (b)(1)")
                                .title("Criteria B1 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(3L)
                                .number("170.315 (b)(1)")
                                .title("Criteria B1 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_noAttestedOriginalAndOneAttestedCuresCertificationResults_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (b)(1)")
                                .title("Criteria B1 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(3L)
                                .number("170.315 (b)(1)")
                                .title("Criteria B1 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_b1OriginalAndB1CuresAttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (b)(1)")
                                .title("Criteria B1 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(3L)
                                .number("170.315 (b)(1)")
                                .title("Criteria B1 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (b)(1)", "170.315 (b)(1) (Cures Update)")));
    }

    @Test
    public void review_b2OriginalAndB2CuresAttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(4L)
                                .number("170.315 (b)(2)")
                                .title("Criteria B2 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(5L)
                                .number("170.315 (b)(2)")
                                .title("Criteria B2 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (b)(2)", "170.315 (b)(2) (Cures Update)")));
    }

    @Test
    public void review_b3OriginalAndB3CuresAttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(6L)
                                .number("170.315 (b)(3)")
                                .title("Criteria B3 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(7L)
                                .number("170.315 (b)(3)")
                                .title("Criteria B3 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (b)(3)", "170.315 (b)(3) (Cures Update)")));
    }

    @Test
    public void review_b6AndB10CuresAttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(8L)
                                .number("170.315 (b)(6)")
                                .title("Criteria B6")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(15L)
                                .number("170.315 (b)(10)")
                                .title("Criteria B10")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (b)(6)", "170.315 (b)(10)")));
    }

    @Test
    public void review_b7OriginalAndB7CuresAttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(9L)
                                .number("170.315 (b)(7)")
                                .title("Criteria B7 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(10L)
                                .number("170.315 (b)(7)")
                                .title("Criteria B7 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (b)(7)", "170.315 (b)(7) (Cures Update)")));
    }

    @Test
    public void review_b8OriginalAndB8CuresAttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(11L)
                                .number("170.315 (b)(8)")
                                .title("Criteria B8 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(12L)
                                .number("170.315 (b)(8)")
                                .title("Criteria B8 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (b)(8)", "170.315 (b)(8) (Cures Update)")));
    }

    @Test
    public void review_b9OriginalAndB9CuresAttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(13L)
                                .number("170.315 (b)(9)")
                                .title("Criteria B9 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(14L)
                                .number("170.315 (b)(9)")
                                .title("Criteria B9 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (b)(9)", "170.315 (b)(9) (Cures Update)")));
    }

    @Test
    public void review_c3OriginalAndC3CuresAttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(17L)
                                .number("170.315 (c)(3)")
                                .title("Criteria C3 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(18L)
                                .number("170.315 (c)(3)")
                                .title("Criteria C3 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (c)(3)", "170.315 (c)(3) (Cures Update)")));
    }

    @Test
    public void review_d2OriginalAndD2CuresAttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(19L)
                                .number("170.315 (d)(2)")
                                .title("Criteria D2 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(20L)
                                .number("170.315 (d)(2)")
                                .title("Criteria D2 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (d)(2)", "170.315 (d)(2) (Cures Update)")));
    }

    @Test
    public void review_d3OriginalAndD3CuresAttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(21L)
                                .number("170.315 (d)(3)")
                                .title("Criteria D3 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(22L)
                                .number("170.315 (d)(3)")
                                .title("Criteria D3 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (d)(3)", "170.315 (d)(3) (Cures Update)")));
    }

    @Test
    public void review_d10OriginalAndD10CuresAttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(23L)
                                .number("170.315 (d)(10)")
                                .title("Criteria D10 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(24L)
                                .number("170.315 (d)(10)")
                                .title("Criteria D10 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (d)(10)", "170.315 (d)(10) (Cures Update)")));
    }

    @Test
    public void review_e1OriginalAndE1CuresAttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(25L)
                                .number("170.315 (e)(1)")
                                .title("Criteria E1 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(26L)
                                .number("170.315 (e)(1)")
                                .title("Criteria E1 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (e)(1)", "170.315 (e)(1) (Cures Update)")));
    }

    @Test
    public void review_f5OriginalAndF5CuresAttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(27L)
                                .number("170.315 (f)(5)")
                                .title("Criteria F5 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(28L)
                                .number("170.315 (f)(5)")
                                .title("Criteria F5 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (f)(5)", "170.315 (f)(5) (Cures Update)")));
    }

    @Test
    public void review_g6OriginalAndG6CuresAttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(29L)
                                .number("170.315 (g)(6)")
                                .title("Criteria G6 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(30L)
                                .number("170.315 (g)(6)")
                                .title("Criteria G6 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (g)(6)", "170.315 (g)(6) (Cures Update)")));
    }

    @Test
    public void review_g8AndG10AttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(31L)
                                .number("170.315 (g)(8)")
                                .title("Criteria G8 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(34L)
                                .number("170.315 (g)(10)")
                                .title("Criteria G10")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (g)(8)", "170.315 (g)(10)")));
    }

    @Test
    public void review_g9OriginalAndG9CuresAttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(32L)
                                .number("170.315 (g)(9)")
                                .title("Criteria G9 Original")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(33L)
                                .number("170.315 (g)(9)")
                                .title("Criteria G9 (Cures Update)")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (g)(9)", "170.315 (g)(9) (Cures Update)")));
    }

    @Test
    public void review_a9AndB11AttestedCertificationResults_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(9)")
                                .title("Criteria A9")
                                .build())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(16L)
                                .number("170.315 (b)(11)")
                                .title("Criteria B11")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_COMBINATION, "170.315 (a)(9)", "170.315 (b)(11)")));
    }
}
