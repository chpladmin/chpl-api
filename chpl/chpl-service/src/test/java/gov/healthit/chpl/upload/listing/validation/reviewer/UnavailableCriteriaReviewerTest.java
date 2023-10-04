package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class UnavailableCriteriaReviewerTest {
    private static final String UNAVAILABLE_CRITERIA_NOT_ALLOWED = "The criterion %s is unavailable for this listing.";
    private static final String CRITERIA_REMOVED_TOO_LONG_AGO = "The criterion %s was removed too long ago and cannot be modified.";

    private ErrorMessageUtil msgUtil;
    private UnavailableCriteriaReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.unavailableCriteriaAddNotAllowed"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(UNAVAILABLE_CRITERIA_NOT_ALLOWED, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.unavailableCriteriaRemovedTooLongAgo"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CRITERIA_REMOVED_TOO_LONG_AGO, i.getArgument(1), ""));
        reviewer = new UnavailableCriteriaReviewer(msgUtil);
    }

    @Test
    public void review_nullCertificationResult_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        reviewer.review(listing, null);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_criteriaBecameInactiveLessThan1YearAgoAfterListingWasCertified_noError() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(DateUtil.toEpochMillis(sixMonthsAgo.minusDays(30)))
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(sixMonthsAgo)
                                .endDay(sixMonthsAgo.plusDays(2))
                                .build())
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing, listing.getCertificationResults().get(0));

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_criteriaBecameInactiveLessThan1YearAgoBeforeListingWasCertified_hasError() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(DateUtil.toEpochMillis(sixMonthsAgo.plusDays(30)))
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(sixMonthsAgo)
                                .endDay(sixMonthsAgo.plusDays(2))
                                .build())
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing, listing.getCertificationResults().get(0));

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(UNAVAILABLE_CRITERIA_NOT_ALLOWED, "170.315 (a)(1)", "")));
    }

    @Test
    public void review_criteriaBecomesActiveAfterListingIsCertifiedAndAfterToday_hasError() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(DateUtil.toEpochMillis(sixMonthsAgo))
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(tomorrow)
                                .build())
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing, listing.getCertificationResults().get(0));

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(UNAVAILABLE_CRITERIA_NOT_ALLOWED, "170.315 (a)(1)", "")));
    }

    @Test
    public void review_criteriaBecameInactiveMoreThan1YearAndOverlapsListingCertificationDate_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2021-12-30")))
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2021-01-01"))
                                .endDay(LocalDate.parse("2021-12-31"))
                                .build())
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing, listing.getCertificationResults().get(0));

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(CRITERIA_REMOVED_TOO_LONG_AGO, "170.315 (a)(1)", "")));
    }

    @Test
    public void review_criteriaIsActiveAndOverlapsListingCertificationDate_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2022-12-31")))
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .build())
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing, listing.getCertificationResults().get(0));

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }
}
