package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertNull;
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
import gov.healthit.chpl.domain.concept.PrivacyAndSecurityFrameworkConcept;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class PrivacyAndSecurityFrameworkReviewerTest {
    private static final String PANDS_NOT_APPLICABLE = "Privacy and Security Framework is not applicable for the criterion %s. It has been removed.";
    private static final String PANDS_REQUIRED_NOT_FOUND = "Privacy and Security Framework is required for certification %s.";
    private static final String PANDS_INVALID_NOT_FOUND = "Certification %s contains Privacy and Security Framework value '%s' which must match one of %s.";

    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;
    private PrivacyAndSecurityFrameworkReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        certResultRules = Mockito.mock(CertificationResultRules.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.privacyAndSecurityFrameworkNotApplicable"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PANDS_NOT_APPLICABLE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingPrivacySecurityFramework"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PANDS_REQUIRED_NOT_FOUND, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidPrivacySecurityFramework"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(PANDS_INVALID_NOT_FOUND, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        reviewer = new PrivacyAndSecurityFrameworkReviewer(certResultRules,
                new ValidationUtils(Mockito.mock(CertificationCriterionService.class)), msgUtil);
    }

    @Test
    public void review_nullPAndSNotRequiredForCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.PRIVACY_SECURITY)))
            .thenReturn(false);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .privacySecurityFramework(null)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyPAndSNotRequiredForCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.PRIVACY_SECURITY)))
            .thenReturn(false);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .privacySecurityFramework("")
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_nullPAndSRequiredForCriteria_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.PRIVACY_SECURITY)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .privacySecurityFramework(null)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(PANDS_REQUIRED_NOT_FOUND, "170.315 (a)(1)")));
    }

    @Test
    public void review_nullPAndSRequiredForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.PRIVACY_SECURITY)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .privacySecurityFramework(null)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyPAndSRequiredForCriteria_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.PRIVACY_SECURITY)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .privacySecurityFramework("")
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(PANDS_REQUIRED_NOT_FOUND, "170.315 (a)(1)")));
    }

    @Test
    public void review_emptyPAndSRequiredForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.PRIVACY_SECURITY)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .privacySecurityFramework("")
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }


    @Test
    public void review_providedPAndSNotValidForCriteria_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.PRIVACY_SECURITY)))
            .thenReturn(false);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .privacySecurityFramework("Approach 1")
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(PANDS_NOT_APPLICABLE, "170.315 (a)(1)")));
        assertNull(listing.getCertificationResults().get(0).getPrivacySecurityFramework());
    }

    @Test
    public void review_providedPAndSNotValidForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.PRIVACY_SECURITY)))
            .thenReturn(false);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .privacySecurityFramework("Approach 1")
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_providedPAndSNotValidValue_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.PRIVACY_SECURITY)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .privacySecurityFramework("junk")
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(PANDS_INVALID_NOT_FOUND, "170.315 (a)(1)", "junk", PrivacyAndSecurityFrameworkConcept.getFormattedValues())));
    }

    @Test
    public void review_providedPAndSNotValidValueForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.PRIVACY_SECURITY)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .privacySecurityFramework("junk")
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_providedPAndSValidValue_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.PRIVACY_SECURITY)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .success(true)
                        .privacySecurityFramework("Approach 1")
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }
}
