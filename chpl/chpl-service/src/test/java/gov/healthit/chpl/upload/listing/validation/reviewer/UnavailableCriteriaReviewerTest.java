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
    private static final String REMOVED_CRITERIA_NOT_ALLOWED = "The criterion %s was active between %s and %s and may not be added to the listing.";

    private ErrorMessageUtil msgUtil;
    private UnavailableCriteriaReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.unavailableCriteriaAddNotAllowed"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(REMOVED_CRITERIA_NOT_ALLOWED, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
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
    public void review_criteriaRemoved_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(DateUtil.toEpochMillis(LocalDate.parse("2023-02-01")))
                .decertificationDay(LocalDate.parse("2023-03-02"))
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .build())
                        .success(true)
                        .build())
                .build();
        reviewer.review(listing, listing.getCertificationResults().get(0));

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(REMOVED_CRITERIA_NOT_ALLOWED, "170.315 (a)(1)", "2023-01-01", "2023-01-02")));
    }

    @Test
    public void review_criteriaNotRemoved_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
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
