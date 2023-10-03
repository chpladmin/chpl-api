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
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class UnavailableCriteriaReviewerTest {
    private static final String UNAVAILABLE_CRITERIA_NOT_ALLOWED = "The criterion %s is unavailable for this listing.";

    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private UnavailableCriteriaReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);

        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.unavailableCriteriaAddNotAllowed"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(UNAVAILABLE_CRITERIA_NOT_ALLOWED, i.getArgument(1), ""));
        reviewer = new UnavailableCriteriaReviewer(msgUtil, resourcePermissions);
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
    public void review_criteriaRemovedLessThan1YearAgo_noError() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(DateUtil.toEpochMillis(sixMonthsAgo))
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
    public void review_criteriaRemovedMoreThan1YearAgo_hasError() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        LocalDate eighteenMonthsAgo = LocalDate.now().minusMonths(18);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationDate(DateUtil.toEpochMillis(sixMonthsAgo))
                .decertificationDay(sixMonthsAgo.plusDays(5))
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(eighteenMonthsAgo)
                                .endDay(eighteenMonthsAgo.plusDays(2))
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
