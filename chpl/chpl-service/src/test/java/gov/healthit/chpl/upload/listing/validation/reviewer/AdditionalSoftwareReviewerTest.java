package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class AdditionalSoftwareReviewerTest {
    private static final String ADDITIONAL_SOFTWARE_NOT_APPLICABLE = "Additional Software is not applicable for the criterion %s. It has been removed.";
    private static final String HAS_ADDITIONAL_SOFTWARE_BUT_SHOULD_NOT = "Criteria %s contains additional software but it is not expected.";
    private static final String NO_ADDITIONAL_SOFTWARE_BUT_SHOULD = "Criteria %s contains no additional software but it is expected.";
    private static final String ADDITIONAL_SOFTWARE_INVALID = "No CHPL product was found matching additional software %s for %s.";
    private static final String ADDITIONAL_SOFTWARE_BOTH_FIELDS_HAVE_DATA = "Additional Software for %s has both a listing and software name/version specified. Only one is expected.";

    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;
    private AdditionalSoftwareReviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        certResultRules = Mockito.mock(CertificationResultRules.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.additionalSoftwareNotApplicable"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(ADDITIONAL_SOFTWARE_NOT_APPLICABLE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.hasAdditionalSoftwareMismatch"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(HAS_ADDITIONAL_SOFTWARE_BUT_SHOULD_NOT, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.noAdditionalSoftwareMismatch"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(NO_ADDITIONAL_SOFTWARE_BUT_SHOULD, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.additionalSoftwareHasNameAndListingData"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(ADDITIONAL_SOFTWARE_BOTH_FIELDS_HAVE_DATA, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.invalidAdditionalSoftware"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(ADDITIONAL_SOFTWARE_INVALID, i.getArgument(1), i.getArgument(2)));
        reviewer = new AdditionalSoftwareReviewer(certResultRules,
                new ValidationUtils(Mockito.mock(CertificationCriterionService.class)),
                msgUtil);
    }

    @Test
    public void review_nullAdditionalSoftware_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .build())
                .build();
        listing.getCertificationResults().get(0).setAdditionalSoftware(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyAdditionalSoftware_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .additionalSoftware(new ArrayList<CertificationResultAdditionalSoftware>())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_additionalSoftwareNotAllowedForCriteria_hasWarningAdditionalSoftwareSetNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(false);

        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .name("Windows")
                .version("2020")
                .grouping("A")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(ADDITIONAL_SOFTWARE_NOT_APPLICABLE, "170.315 (a)(1)")));
        assertNull(listing.getCertificationResults().get(0).getAdditionalSoftware());
    }

    @Test
    public void review_additionalSoftwareNotAllowedForRemovedCriteria_noErrors() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(false);

        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .name("Windows")
                .version("2020")
                .grouping("A")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .success(true)
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_additionalSoftwareNotExpected_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .name("Windows")
                .version("2020")
                .grouping("A")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .hasAdditionalSoftware(false)
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(HAS_ADDITIONAL_SOFTWARE_BUT_SHOULD_NOT, "170.315 (a)(1)")));
    }

    @Test
    public void review_additionalSoftwareNotExpectedRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .name("Windows")
                .version("2020")
                .grouping("A")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .success(true)
                        .hasAdditionalSoftware(false)
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_additionalSoftwareExpected_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .hasAdditionalSoftware(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(NO_ADDITIONAL_SOFTWARE_BUT_SHOULD, "170.315 (a)(1)")));
    }

    @Test
    public void review_additionalSoftwareExpectedRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .success(true)
                        .hasAdditionalSoftware(true)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_additionalSoftwareExpectedAndHasAdditionalSoftware_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .name("Windows")
                .version("2020")
                .grouping("A")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .hasAdditionalSoftware(true)
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_noAdditionalSoftwareExpectedAndHasNone_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .hasAdditionalSoftware(false)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_additionalSoftwareInvalidChplId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .certifiedProductNumber("15.05.05")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(ADDITIONAL_SOFTWARE_INVALID, "15.05.05", "170.315 (a)(1)")));
    }

    @Test
    public void review_additionalSoftwareInvalidChplIdRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .certifiedProductNumber("15.05.05")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .success(true)
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_additionalSoftwareValidChplId_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .certifiedProductId(1L)
                .certifiedProductNumber("15.05.05")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_additionalSoftwareBothChplIdAndNameWithData_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .certifiedProductId(1L)
                .certifiedProductNumber("15.05.05")
                .name("Windows")
                .grouping("A")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(ADDITIONAL_SOFTWARE_BOTH_FIELDS_HAVE_DATA, "170.315 (a)(1)")));
    }

    @Test
    public void review_additionalSoftwareBothChplIdAndNameWithDataRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .certifiedProductId(1L)
                .certifiedProductNumber("15.05.05")
                .name("Windows")
                .grouping("A")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .success(true)
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_additionalSoftwareBothChplIdAndVersionWithData_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .certifiedProductId(1L)
                .certifiedProductNumber("15.05.05")
                .version("1")
                .grouping("A")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(ADDITIONAL_SOFTWARE_BOTH_FIELDS_HAVE_DATA, "170.315 (a)(1)")));
    }

    @Test
    public void review_additionalSoftwareBothChplIdAndVersionWithDataRemovedCriteria_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .certifiedProductId(1L)
                .certifiedProductNumber("15.05.05")
                .version("1")
                .grouping("A")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .success(true)
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_additionalSoftwareValidName_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);

        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        additionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .name("test")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .success(true)
                        .additionalSoftware(additionalSoftware)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }
}
