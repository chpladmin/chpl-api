package gov.healthit.chpl.validation.pendinglisting.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.HashSet;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.TestStandardReviewer;

public class TestStandardReviewerTest {
    private TestStandardDAO testStandardDao;
    private ErrorMessageUtil errorMessageUtil;
    private TestStandardReviewer reviewer;
    private FF4j ff4j;

    @Before
    public void before() throws EntityRetrievalException {
        testStandardDao = Mockito.mock(TestStandardDAO.class);
        TestStandardDTO ts = new TestStandardDTO();
        ts.setId(1L);
        ts.setCertificationEditionId(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId());
        ts.setName("mock");
        Mockito.when(testStandardDao.getByNumberAndEdition(
                ArgumentMatchers.eq("mock"),
                ArgumentMatchers.eq(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())))
        .thenReturn(ts);

        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestStandardName"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("There was no test standard name found for certification criteria %s.",
                        i.getArgument(1), ""));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria."),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("Criteria %s contains a test standard '%s' which does not "
                        + "currently exist for edition %s.",
                        i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(FeatureList.OPTIONAL_STANDARDS))
        .thenReturn(false);
        Mockito.when(ff4j.check(FeatureList.OPTIONAL_STANDARDS_ERROR))
        .thenReturn(false);

        reviewer = new TestStandardReviewer(testStandardDao, errorMessageUtil, ff4j);
    }

    @Test
    public void review_UnattestedCriterionWithoutTestStandards_NoError() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date())
                .certificationEditionId(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(false)
                        .criterion(getCriterionDTO(1L, "170.315 (a)(1)"))
                        .clearTestStandards()
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_AttestedCriterionWithoutTestStandards_NoError() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date())
                .certificationEditionId(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(getCriterionDTO(1L, "170.315 (a)(1)"))
                        .clearTestStandards()
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_UnattestedCriterionWithExistingTestStandard_NoError() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date())
                .certificationEditionId(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(false)
                        .criterion(getCriterionDTO(1L, "170.315 (a)(1)"))
                        .testStandard(getCertResultTestStandardDTO(1L, "mock"))
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_AttestedCriterionWithExistingTestStandard_NoError() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date())
                .certificationEditionId(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(getCriterionDTO(1L, "170.315 (a)(1)"))
                        .testStandard(getCertResultTestStandardDTO(1L, "mock"))
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_UnattestedCriterionExistingTestStandardWithoutId_NoError() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date())
                .certificationEditionId(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(false)
                        .criterion(getCriterionDTO(1L, "170.315 (a)(1)"))
                        .testStandard(getCertResultTestStandardDTO(null, "mock"))
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_AttestedCriterionExistingTestStandardWithoutId_NoError() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date())
                .certificationEditionId(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(getCriterionDTO(1L, "170.315 (a)(1)"))
                        .testStandard(getCertResultTestStandardDTO(null, "mock"))
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_UnttestedCriterionNonexistentTestStandard_NoError() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date())
                .certificationEditionId(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(false)
                        .criterion(getCriterionDTO(1L, "170.315 (a)(1)"))
                        .testStandard(getCertResultTestStandardDTO(2L, "does not exist"))
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_AttestedCriterionNonexistentTestStandard_HasError() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date())
                .certificationEditionId(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(getCriterionDTO(1L, "170.315 (a)(1)"))
                        .testStandard(getCertResultTestStandardDTO(2L, "does not exist"))
                        .build())
                .build();
        listing.setErrorMessages(new HashSet<String>());

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
    }

    @Test
    public void review_UnattestedCriterionNonexistentTestStandardNoId_NoError() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date())
                .certificationEditionId(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(false)
                        .criterion(getCriterionDTO(1L, "170.315 (a)(1)"))
                        .testStandard(getCertResultTestStandardDTO(null, "does not exist"))
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_AttestedCriterionNonexistentTestStandardNoId_HasError() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date())
                .certificationEditionId(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(getCriterionDTO(1L, "170.315 (a)(1)"))
                        .testStandard(getCertResultTestStandardDTO(null, "does not exist"))
                        .build())
                .build();
        listing.setErrorMessages(new HashSet<String>());

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
    }

    @Test
    public void review_UnattestedCriterionNoTestStandardName_NoError() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date())
                .certificationEditionId(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(false)
                        .criterion(getCriterionDTO(1L, "170.315 (a)(1)"))
                        .testStandard(getCertResultTestStandardDTO(1L, null))
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_AttestedCriterionNoTestStandardName_HasError() {
        PendingCertifiedProductDTO listing = PendingCertifiedProductDTO.builder()
                .certificationDate(new Date())
                .certificationEditionId(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                .certificationCriterionSingle(PendingCertificationResultDTO.builder()
                        .meetsCriteria(true)
                        .criterion(getCriterionDTO(1L, "170.315 (a)(1)"))
                        .testStandard(getCertResultTestStandardDTO(1L, null))
                        .build())
                .build();
        listing.setErrorMessages(new HashSet<String>());

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
    }

    private CertificationCriterionDTO getCriterionDTO(Long id, String number) {
        return CertificationCriterionDTO.builder()
                .id(id)
                .number(number)
                .build();
    }

    private PendingCertificationResultTestStandardDTO getCertResultTestStandardDTO(Long id, String name) {
        return PendingCertificationResultTestStandardDTO.builder()
                .testStandardId(id)
                .name(name)
                .build();
    }
}
