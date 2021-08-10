package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class UcdProcessReviewerTest {
    private static final String UCD_NOT_APPLICABLE = "UCD Processes are not applicable for the criterion %s.";
    private static final String UCD_NOT_FOUND_AND_REMOVED = "UCD Process '%s' referenced by criteria %s was not found and has been removed.";
    private static final String MISSING_UCD_PROCESS = "Certification %s requires at least one UCD process.";

    private CertificationResultRules certResultRules;
    private CertificationCriterionService criteriaService;
    private ErrorMessageUtil errorMessageUtil;
    private ResourcePermissions resourcePermissions;
    private CertificationCriterion a1, a2, a3;
    private UcdProcessReviewer reviewer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.ucdProcessNotApplicable"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(UCD_NOT_APPLICABLE, i.getArgument(1), ""));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.ucdProcessNotFoundAndRemoved"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(UCD_NOT_FOUND_AND_REMOVED, i.getArgument(1), i.getArgument(2)));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingUcdProcess"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_UCD_PROCESS, i.getArgument(1), ""));

        criteriaService = Mockito.mock(CertificationCriterionService.class);
        a1 = CertificationCriterion.builder().id(1L).number("170.315 (a)(1)").title("a1").build();
        a2 = CertificationCriterion.builder().id(2L).number("170.315 (a)(2)").title("a2").build();
        a3 = CertificationCriterion.builder().id(3L).number("170.315 (a)(3)").title("a3").build();
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(a1.getId()))).thenReturn(a1);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(a2.getId()))).thenReturn(a2);
        Mockito.when(criteriaService.get(ArgumentMatchers.eq(a3.getId()))).thenReturn(a3);

        certResultRules = Mockito.mock(CertificationResultRules.class);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(a1.getNumber()), ArgumentMatchers.eq(CertificationResultRules.UCD_FIELDS)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(a2.getNumber()), ArgumentMatchers.eq(CertificationResultRules.UCD_FIELDS)))
            .thenReturn(true);
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(a3.getNumber()), ArgumentMatchers.eq(CertificationResultRules.UCD_FIELDS)))
            .thenReturn(false);

        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        reviewer = new UcdProcessReviewer(criteriaService, new ValidationUtils(), certResultRules, "1,2",
                errorMessageUtil, resourcePermissions);
    }

    @Test
    public void review_nullUcdProcessesNoCertificationResults_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder()
                        .build())
                .build();
        listing.getSed().setUcdProcesses(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyUcdProcessesNoCertificationResults_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .sed(CertifiedProductSed.builder()
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_ucdProcessHasNotAllowedCriteria_certResultHasSedTrue_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a3)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder()
                        .ucdProcess(UcdProcess.builder()
                                .id(1L)
                                .criterion(a3)
                                .name("UCD Name")
                                .details("some details")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(UCD_NOT_APPLICABLE, "170.315 (a)(3)")));
    }

    @Test
    public void review_ucdProcessHasNotAllowedCriteria_certResultHasSedFalse_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a3)
                        .sed(false)
                        .build())
                .sed(CertifiedProductSed.builder()
                        .ucdProcess(UcdProcess.builder()
                                .id(1L)
                                .criterion(a3)
                                .name("UCD Name")
                                .details("some details")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(UCD_NOT_APPLICABLE, "170.315 (a)(3)")));
    }

    @Test
    public void review_ucdProcessHasNotAllowedCriteria_certResultUnattested_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(a3)
                        .sed(false)
                        .build())
                .sed(CertifiedProductSed.builder()
                        .ucdProcess(UcdProcess.builder()
                                .id(1L)
                                .criterion(a3)
                                .name("UCD Name")
                                .details("some details")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(UCD_NOT_APPLICABLE, "170.315 (a)(3)")));
    }

    @Test
    public void review_ucdProcessWithoutIdIsRemoved_certResultAttested_hasWarningAndError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder()
                        .build())
                .build();
        UcdProcess ucdNotFound = UcdProcess.builder()
                .criterion(a1)
                .name("UCD Name")
                .details("some details")
                .build();
        listing.getSed().setUcdProcesses(Stream.of(ucdNotFound).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(UCD_NOT_FOUND_AND_REMOVED, "UCD Name", "170.315 (a)(1)")));
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_UCD_PROCESS, "170.315 (a)(1)")));
        assertEquals(0, listing.getSed().getUcdProcesses().size());
    }

    @Test
    public void review_ucdProcessWithoutIdIsRemoved_certResultUnattested_hasWarningNoErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder()
                        .build())
                .build();
        UcdProcess ucdNotFound = UcdProcess.builder()
                .criterion(a1)
                .name("UCD Name")
                .details("some details")
                .build();
        listing.getSed().setUcdProcesses(Stream.of(ucdNotFound).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(UCD_NOT_FOUND_AND_REMOVED, "UCD Name", "170.315 (a)(1)")));
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getSed().getUcdProcesses().size());
    }

    @Test
    public void review_ucdProcessWithoutIdIsRemoved_multipleCriteria_hasWarningAndErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a2)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder()
                        .build())
                .build();
        UcdProcess ucdNotFound = UcdProcess.builder()
                .criterion(a1)
                .criterion(a2)
                .name("UCD Name")
                .details("some details")
                .build();
        listing.getSed().setUcdProcesses(Stream.of(ucdNotFound).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(UCD_NOT_FOUND_AND_REMOVED, "UCD Name", "170.315 (a)(1),170.315 (a)(2)")));
        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_UCD_PROCESS , "170.315 (a)(1)")));
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_UCD_PROCESS , "170.315 (a)(2)")));
        assertEquals(0, listing.getSed().getUcdProcesses().size());
    }

    @Test
    public void review_ucdProcessWithoutIdIsRemoved_criteriaDoesNotAllowUcd_hasWarningNoErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a3)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder()
                        .build())
                .build();
        UcdProcess ucdNotFound = UcdProcess.builder()
                .criterion(a3)
                .name("UCD Name")
                .details("some details")
                .build();
        listing.getSed().setUcdProcesses(Stream.of(ucdNotFound).collect(Collectors.toList()));
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(UCD_NOT_FOUND_AND_REMOVED, "UCD Name", "170.315 (a)(3)")));
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getSed().getUcdProcesses().size());
    }

    @Test
    public void review_criteriaWithSedIsMissingUcdProcess_hasErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .sed(CertifiedProductSed.builder()
                        .ucdProcess(UcdProcess.builder()
                            .id(1L)
                            .name("UCD Name")
                            .details("some details")
                            .build())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(MISSING_UCD_PROCESS , "170.315 (a)(1)")));
    }

    @Test
    public void review_ucdProcessIncludesUnattestedCriteria_hasErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a3)
                        .sed(false)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(a1)
                        .sed(false)
                        .build())
                .sed(CertifiedProductSed.builder()
                        .ucdProcess(UcdProcess.builder()
                            .id(1L)
                            .criterion(a1)
                            .name("UCD Name")
                            .details("some details")
                            .build())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(UCD_NOT_APPLICABLE , "170.315 (a)(1)")));
    }

    @Test
    public void review_ucdProcessesValid_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a1)
                        .sed(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a2)
                        .sed(true)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(a3)
                        .sed(false)
                        .build())
                .sed(CertifiedProductSed.builder()
                        .ucdProcess(UcdProcess.builder()
                                .id(1L)
                                .criterion(a1)
                                .criterion(a2)
                                .name("UCD Name 1")
                                .details("some details")
                                .build())
                        .ucdProcess(UcdProcess.builder()
                                .id(2L)
                                .criterion(a2)
                                .name("UCD Name 2")
                                .details("some details")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }
}
