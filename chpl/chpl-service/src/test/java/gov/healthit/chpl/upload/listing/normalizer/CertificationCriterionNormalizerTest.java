package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.util.CertificationResultRules;

public class CertificationCriterionNormalizerTest {

    private CertificationCriterionDAO criterionDao;
    private CertificationResultRules certResultRules;
    private CertificationCriterionNormalizer normalizer;

    @Before
    public void setup() {
        criterionDao = Mockito.mock(CertificationCriterionDAO.class);
        certResultRules = Mockito.mock(CertificationResultRules.class);
        //mock that no criteria can have any fields, will change in specific tests
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn(false);
        normalizer = new CertificationCriterionNormalizer(criterionDao, certResultRules);
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAsUnattestedAndAllFieldsNull() {
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            assertFalse(certResult.isSuccess());
            assertNull(certResult.getAdditionalSoftware());
            assertNull(certResult.getApiDocumentation());
            assertNull(certResult.getAttestationAnswer());
            assertNull(certResult.getConformanceMethods());
            assertNull(certResult.getDocumentationUrl());
            assertNull(certResult.getExportDocumentation());
            assertNull(certResult.isGap());
            assertNull(certResult.isG1Success());
            assertNull(certResult.isG2Success());
            assertNull(certResult.getOptionalStandards());
            assertNull(certResult.getPrivacySecurityFramework());
            assertNull(certResult.getServiceBaseUrlList());
            assertNull(certResult.getSvaps());
            assertNull(certResult.getTestDataUsed());
            assertNull(certResult.getTestFunctionality());
            assertNull(certResult.getTestProcedures());
            assertNull(certResult.getTestStandards());
            assertNull(certResult.getTestToolsUsed());
            assertNull(certResult.getUseCases());
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsAdditionalSoftware() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getAdditionalSoftware());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getAdditionalSoftware());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsApiDocumentation() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.API_DOCUMENTATION)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getApiDocumentation());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getApiDocumentation());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsAttestationAnswer() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.ATTESTATION_ANSWER)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getAttestationAnswer());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getAttestationAnswer());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsConformanceMethods() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.CONFORMANCE_METHOD)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getConformanceMethods());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getConformanceMethods());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsDocumentationUrl() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.DOCUMENTATION_URL)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getDocumentationUrl());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getDocumentationUrl());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsExportDocumentation() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.EXPORT_DOCUMENTATION)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getExportDocumentation());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getExportDocumentation());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsFunctionalityTested() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getTestFunctionality());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getTestFunctionality());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsG1() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.G1_SUCCESS)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.isG1Success());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.isG1Success());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsG2() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.G2_SUCCESS)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.isG2Success());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.isG2Success());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsGap() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.isGap());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.isGap());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsOptionalStandards() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.OPTIONAL_STANDARD)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getOptionalStandards());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getOptionalStandards());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsPrivacyAndSecurity() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.PRIVACY_SECURITY)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getPrivacySecurityFramework());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getPrivacySecurityFramework());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsSed() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.SED)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.isSed());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.isSed());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsServiceBaseUrl() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.SERVICE_BASE_URL_LIST)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getServiceBaseUrlList());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getServiceBaseUrlList());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsTestStandards() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.STANDARDS_TESTED)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getTestStandards());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getTestStandards());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsSvaps() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.SVAP)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getSvaps());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getSvaps());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsTestData() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getTestDataUsed());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getTestDataUsed());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsTestProcedure() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.TEST_PROCEDURE)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getTestProcedures());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getTestProcedures());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsTestTools() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getTestToolsUsed());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getTestToolsUsed());
            }
        }
    }

    @Test
    public void normalize_noCriteriaInListing_allCriteriaAddedAndA1AllowsUseCases() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq("170.315 (a)(1)"),
                ArgumentMatchers.eq(CertificationResultRules.USE_CASES)))
            .thenReturn(true);
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(new ArrayList<CertificationResult>());
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertNotNull(certResult.getUseCases());
            } else if (certResult.getCriterion().getNumber().equals("170.315 (a)(2)")) {
                assertNull(certResult.getUseCases());
            }
        }
    }

    @Test
    public void normalize_oneCriteriaInListingAndNoFieldsAllowed_oneCriteriaAddedAsUnattestedAndAllFieldsNull() {
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(Stream.of(CertificationResult.builder()
                        .id(100L)
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .title("a1")
                                .build())
                        .additionalSoftware(new ArrayList<CertificationResultAdditionalSoftware>())
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            assertFalse(certResult.isSuccess());
            assertNull(certResult.getAdditionalSoftware());
            assertNull(certResult.getApiDocumentation());
            assertNull(certResult.getAttestationAnswer());
            assertNull(certResult.getConformanceMethods());
            assertNull(certResult.getDocumentationUrl());
            assertNull(certResult.getExportDocumentation());
            assertNull(certResult.isGap());
            assertNull(certResult.isG1Success());
            assertNull(certResult.isG2Success());
            assertNull(certResult.getOptionalStandards());
            assertNull(certResult.getPrivacySecurityFramework());
            assertNull(certResult.getServiceBaseUrlList());
            assertNull(certResult.getSvaps());
            assertNull(certResult.getTestDataUsed());
            assertNull(certResult.getTestFunctionality());
            assertNull(certResult.getTestProcedures());
            assertNull(certResult.getTestStandards());
            assertNull(certResult.getTestToolsUsed());
            assertNull(certResult.getUseCases());
        }
    }

    @Test
    public void normalize_oneAttestedCriteriaInListingAndNoFieldsAllowed_attestedCriteriaIsUnchanged() {
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();
        listing.setCertificationResults(Stream.of(CertificationResult.builder()
                        .id(100L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .title("a1")
                                .build())
                        .additionalSoftware(new ArrayList<CertificationResultAdditionalSoftware>())
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getAdditionalSoftware());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getAdditionalSoftware());
            }
        }
    }

    @Test
    public void normalize_allCriteriaInListingAndNoFieldsAllowed_allFieldsNull() {
        Mockito.when(criterionDao.findByCertificationEditionYear(ArgumentMatchers.anyString()))
            .thenReturn(Stream.of(
                    buildCriterionDto(1L, "170.315 (a)(1)", "a1"),
                    buildCriterionDto(2L, "170.315 (a)(2)", "a2"))
                    .collect(Collectors.toList()));
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(100L)
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .title("a1")
                                .build())
                        .additionalSoftware(new ArrayList<CertificationResultAdditionalSoftware>())
                        .build())
                .certificationResult(CertificationResult.builder()
                        .id(200L)
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (a)(2)")
                                .title("a2")
                                .build())
                        .additionalSoftware(new ArrayList<CertificationResultAdditionalSoftware>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(2, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            assertFalse(certResult.isSuccess());
            assertNull(certResult.getAdditionalSoftware());
            assertNull(certResult.getApiDocumentation());
            assertNull(certResult.getAttestationAnswer());
            assertNull(certResult.getConformanceMethods());
            assertNull(certResult.getDocumentationUrl());
            assertNull(certResult.getExportDocumentation());
            assertNull(certResult.isGap());
            assertNull(certResult.isG1Success());
            assertNull(certResult.isG2Success());
            assertNull(certResult.getOptionalStandards());
            assertNull(certResult.getPrivacySecurityFramework());
            assertNull(certResult.getServiceBaseUrlList());
            assertNull(certResult.getSvaps());
            assertNull(certResult.getTestDataUsed());
            assertNull(certResult.getTestFunctionality());
            assertNull(certResult.getTestProcedures());
            assertNull(certResult.getTestStandards());
            assertNull(certResult.getTestToolsUsed());
            assertNull(certResult.getUseCases());
        }
    }

    private CertificationCriterionDTO buildCriterionDto(Long id, String number, String title) {
        return CertificationCriterionDTO.builder()
                .id(id)
                .number(number)
                .title(title)
                .build();
    }
}