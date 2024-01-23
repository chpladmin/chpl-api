package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ChplResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SedG32015ReviewerTest {
    private static final String NO_G3_HAS_SED = "Listing has not attested to (g)(3), but at least one criteria was found attesting to SED.";
    private static final String HAS_G3_NO_SED = "Listing has attested to (g)(3), but no criteria were found attesting to SED.";

    private CertificationCriterion g3;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private SedG32015Reviewer reviewer;

    @Before
    public void before() throws EntityRetrievalException {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.foundSedCriteriaWithoutAttestingSed")))
            .thenReturn(NO_G3_HAS_SED);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.foundNoSedCriteriaButAttestingSed")))
            .thenReturn(HAS_G3_NO_SED);
        resourcePermissions = Mockito.mock(ChplResourcePermissions.class);
        CertificationCriterionService criteriaService = Mockito.mock(CertificationCriterionService.class);
        g3 = CertificationCriterion.builder()
                .id(52L)
                .number("170.315 (g)(3)")
                .title("g3 original")
                .startDay(LocalDate.parse("2023-01-01"))
                .certificationEdition("2015")
                .build();
        Mockito.when(criteriaService.get(ArgumentMatchers.eq("criterion.170_315_g_3"))).thenReturn(g3);

        ResourcePermissionsFactory resourcePermissionsFactory = Mockito.mock(ResourcePermissionsFactory.class);
        Mockito.when(resourcePermissionsFactory.get()).thenReturn(resourcePermissions);

        reviewer = new SedG32015Reviewer(criteriaService, msgUtil, resourcePermissionsFactory);
    }

    @Test
    public void review_noCertificationResults_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_attestsG3AndHasCertificationResultWithSed_noErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .sed(true)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(2)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_noG3AndHasCertificationResultWithSed_hasErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .sed(true)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(NO_G3_HAS_SED));
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_noG3AndHasRemovedCertificationResultWithSed_adminUser_noWarnings() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .sed(true)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_noG3AndHasRemovedCertificationResultWithSed_oncUser_noWarnings() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .sed(true)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_noG3AndHasRemovedCertificationResultWithSed_acbUser_noErrorsOrWarnings() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .sed(true)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_hasG3AndNoCertificationResultWithSed_hasErrors() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .sed(false)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .certificationEdition("2015")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(HAS_G3_NO_SED));
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_hasG3AndRemovedCertificationResultWithSed_adminUser_noWarnings() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .sed(true)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_hasG3AndRemovedCertificationResultWithSed_oncUser_noWarnings() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .sed(true)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_hasG3AndRemovedCertificationResultWithSed_acbUser_noErrorsNoWarnings() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(g3)
                        .build())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .sed(true)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(1)")
                                .startDay(LocalDate.parse("2023-01-01"))
                                .endDay(LocalDate.parse("2023-01-02"))
                                .certificationEdition("2015")
                                .build())
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }
}
