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

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertificationResultRules;

public class CertificationCriterionNormalizerTest {

    private CertificationResultRules certResultRules;
    private CertificationCriterionNormalizer normalizer;

    @Before
    public void setup() {
        certResultRules = Mockito.mock(CertificationResultRules.class);
        //mock that no criteria can have any fields, will change in specific tests
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString()))
            .thenReturn(false);
        normalizer = new CertificationCriterionNormalizer(certResultRules);
    }

    @Test
    public void normalize_oneCriteriaInListingAndNoFieldsAllowed_oneCriteriaAddedAsUnattestedAndAllFieldsNull() {
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
        assertEquals(1, listing.getCertificationResults().size());
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
            assertNull(certResult.getFunctionalitiesTested());
            assertNull(certResult.getTestProcedures());
            assertNull(certResult.getTestStandards());
            assertNull(certResult.getTestToolsUsed());
            assertNull(certResult.getUseCases());
        }
    }

    @Test
    public void normalize_oneAttestedCriteriaInListingAndNoFieldsAllowed_AllCriteriaAddedAndAttestedCriteriaIsUnchanged() {
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
        assertEquals(1, listing.getCertificationResults().size());
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
    public void normalize_a1AttestedAndAdditionaLSoftwareAllowed_a2AddedAndA1AdditionalSoftwareNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.ADDITIONAL_SOFTWARE)))
            .thenReturn(true);
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
                        .additionalSoftware(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
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
    public void normalize_a1AttestedAndApiDocumentationAllowed_a2AddedAndA1ApiDocumentationNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.API_DOCUMENTATION)))
            .thenReturn(true);
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
                        .apiDocumentation(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getApiDocumentation());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getApiDocumentation());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndAttestationAnswerAllowed_a2AddedAndA1AttestationAnswerNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.ATTESTATION_ANSWER)))
            .thenReturn(true);
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
                        .attestationAnswer(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getAttestationAnswer());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getAttestationAnswer());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndDocumentationUrlAllowed_a2AddedAndA1DocumentationUrlNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.DOCUMENTATION_URL)))
            .thenReturn(true);
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
                        .documentationUrl(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getDocumentationUrl());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getDocumentationUrl());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndExportDocumentationAllowed_a2AddedAndA1ExportDocumentationNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.EXPORT_DOCUMENTATION)))
            .thenReturn(true);
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
                        .exportDocumentation(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getExportDocumentation());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getExportDocumentation());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndFunctionalityTestedAllowed_a2AddedAndA1FunctionalityTestedNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);
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
                        .functionalitiesTested(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getFunctionalitiesTested());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getFunctionalitiesTested());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndG1SuccessAllowed_a2AddedAndA1G1SuccessNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.G1_SUCCESS)))
            .thenReturn(true);
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
                        .g1Success(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNull(certResult.isG1Success());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.isG1Success());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndG2SuccessAllowed_a2AddedAndA1G2SuccessNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.G2_SUCCESS)))
            .thenReturn(true);
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
                        .g2Success(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNull(certResult.isG2Success());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.isG2Success());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndGapAllowed_a2AddedAndA1GapNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.GAP)))
            .thenReturn(true);
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
                        .gap(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNull(certResult.isGap());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.isGap());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndOptionalStandardAllowed_a2AddedAndA1OptionalStandardNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.OPTIONAL_STANDARD)))
            .thenReturn(true);
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
                        .optionalStandards(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getOptionalStandards());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getOptionalStandards());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndPrivacySecurityAllowed_a2AddedAndA1PrivacySecurityNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.PRIVACY_SECURITY)))
            .thenReturn(true);
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
                        .privacySecurityFramework(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getPrivacySecurityFramework());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getPrivacySecurityFramework());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndSedAllowed_a2AddedAndA1SedNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.SED)))
            .thenReturn(true);
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
                        .sed(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNull(certResult.isSed());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.isSed());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndServiceBaseUrlAllowed_a2AddedAndA1ServiceBaseUrlNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.SERVICE_BASE_URL_LIST)))
            .thenReturn(true);
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
                        .serviceBaseUrlList(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getServiceBaseUrlList());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getServiceBaseUrlList());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndTestStandardsAllowed_a2AddedAndA1TestStandardsNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.STANDARDS_TESTED)))
            .thenReturn(true);
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
                        .testStandards(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getTestStandards());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getTestStandards());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndSvapAllowed_a2AddedAndA1SvapNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.SVAP)))
            .thenReturn(true);
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
                        .svaps(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getSvaps());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getSvaps());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndTestDataAllowed_a2AddedAndA1TestDataNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.TEST_DATA)))
            .thenReturn(true);
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
                        .testDataUsed(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getTestDataUsed());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getTestDataUsed());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndTestProcedureAllowed_a2AddedAndA1TestProcedureNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.TEST_PROCEDURE)))
            .thenReturn(true);
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
                        .testProcedures(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getTestProcedures());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getTestProcedures());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndConformanceMethodsAllowed_a2AddedAndA1ConformanceMethodsNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.CONFORMANCE_METHOD)))
            .thenReturn(true);
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
                        .conformanceMethods(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getConformanceMethods());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getConformanceMethods());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndTestToolsAllowed_a2AddedAndA1TestToolsNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED)))
            .thenReturn(true);
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
                        .testToolsUsed(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getTestToolsUsed());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getTestToolsUsed());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndUseCasesAllowed_a2AddedAndA1UseCasesNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.USE_CASES)))
            .thenReturn(true);
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
                        .useCases(null)
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertNotNull(certResult.getUseCases());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getUseCases());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndServiceBaseUrlPopulatedButNotAllowed_a2AddedAndA1ServiceBaseUrlNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.SERVICE_BASE_URL_LIST)))
            .thenReturn(false);
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
                        .serviceBaseUrlList("data")
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertEquals("data", certResult.getServiceBaseUrlList());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getServiceBaseUrlList());
            }
        }
    }

    @Test
    public void normalize_a1AttestedAndServiceBaseUrlPopulatedAndAllowed_a2AddedAndA1ServiceBaseUrlNotNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationResultRules.SERVICE_BASE_URL_LIST)))
            .thenReturn(true);
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
                        .serviceBaseUrlList("data")
                        .build()).collect(Collectors.toList()));
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults());
        assertEquals(1, listing.getCertificationResults().size());
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getCriterion().getNumber().equals("170.315 (a)(1)")) {
                assertTrue(certResult.isSuccess());
                assertEquals("data", certResult.getServiceBaseUrlList());
            } else {
                assertFalse(certResult.isSuccess());
                assertNull(certResult.getServiceBaseUrlList());
            }
        }
    }

    @Test
    public void normalize_allCriteriaInListingAndNoFieldsAllowed_allFieldsNull() {
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
            assertNull(certResult.getFunctionalitiesTested());
            assertNull(certResult.getTestProcedures());
            assertNull(certResult.getTestStandards());
            assertNull(certResult.getTestToolsUsed());
            assertNull(certResult.getUseCases());
        }
    }

    private CertificationCriterion buildCriterionDto(Long id, String number, String title) {
        return CertificationCriterion.builder()
                .id(id)
                .number(number)
                .title(title)
                .build();
    }
}
