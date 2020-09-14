package gov.healthit.chpl.validation.pendinglisting.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.GapAllowedReviewer;

public class GapAllowedReviewerTest {

    private static final String F3_CRITERIA_NUMBER = "170.315 (f)(3)";
    private static final String F3_CRITERIA_KEY = "criterion.170_315_f_3";
    private static final String F3_GAP_ERROR_KEY = "listing.criteria.f3CannotHaveGap";
    private static final Long DATE_BEFORE_CURES = 1577836800000L; // 01/01/2020
    private static final Long DATE_AFTER_CURES = 1598918400000L; // 09/01/2020

    private GapAllowedReviewer gapAllowedReviewer;
    private CertificationCriterionService certificationCriterionService;
    private ErrorMessageUtil errorMessageUtil;

    @Before
    public void before() throws ParseException {
        certificationCriterionService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(certificationCriterionService.get(F3_CRITERIA_KEY)).thenReturn(getF3Criteria());

        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(F3_GAP_ERROR_KEY)).thenReturn("Test Error Message");

        gapAllowedReviewer = new GapAllowedReviewer(certificationCriterionService, errorMessageUtil, "06/30/2020");
        gapAllowedReviewer.setup();
    }

    @Test
    public void review_CertDateBeforeCuresEffectiveRuleDateAndF3NoGap_NoErrors() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date(DATE_BEFORE_CURES))
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(CertificationCriterionDTO.builder()
                                .number(F3_CRITERIA_NUMBER)
                                .build())
                        .gap(false)
                        .build())
                .build();

        gapAllowedReviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_CertDateBeforeCuresEffectiveRuleDateAndF3WithGap_NoErrors() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date(DATE_BEFORE_CURES))
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(CertificationCriterionDTO.builder()
                                .number(F3_CRITERIA_NUMBER)
                                .build())
                        .gap(true)
                        .build())
                .build();

        gapAllowedReviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_CertDateAfterCuresEffectiveRuleDateAndF3NoGap_NoErrors() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date(DATE_AFTER_CURES))
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(CertificationCriterionDTO.builder()
                                .number(F3_CRITERIA_NUMBER)
                                .build())
                        .gap(false)
                        .build())
                .build();

        gapAllowedReviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
}

    @Test
    public void review_CertDateAfterCuresEffectiveRuleDateAndF3WithGap_ErroMessageExists() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date(DATE_AFTER_CURES))
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(CertificationCriterionDTO.builder()
                                .number(F3_CRITERIA_NUMBER)
                                .build())
                        .gap(true)
                        .build())
                .build();
        listing.setErrorMessages(new HashSet<String>());

        gapAllowedReviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
}

    private CertificationCriterion getF3Criteria() {
        return CertificationCriterion.builder()
                .number(F3_CRITERIA_NUMBER)
                .build();
    }

}
