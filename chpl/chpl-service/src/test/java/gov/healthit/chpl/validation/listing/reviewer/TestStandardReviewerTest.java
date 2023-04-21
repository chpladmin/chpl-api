package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestStandardReviewerTest {
    private TestStandardDAO testStandardDao;
    private ErrorMessageUtil errorMessageUtil;
    private TestStandardReviewer reviewer;

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
        Mockito.when(testStandardDao.getByIdAndEdition(
                ArgumentMatchers.eq(1L),
                ArgumentMatchers.eq(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())))
                .thenReturn(ts);

        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testStandardIdNotFound"), ArgumentMatchers.any()))
                .thenReturn("Test error message 1!");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestStandardName"), ArgumentMatchers.any()))
                .thenAnswer(i -> String.format("There was no test standard name found for certification criteria %s.", i.getArgument(1), ""));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testStandardNotFound"), ArgumentMatchers.any()))
                .thenReturn("Test error message 3!");

        reviewer = new TestStandardReviewer(testStandardDao, errorMessageUtil);
    }

    @Test
    public void review_UnattestedCriterionWithoutTestStandards_NoError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(buildEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId(), "2015"))
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(buildCriterion(1L, "170.315 (a)(1)"))
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_AttestedCriterionWithoutTestStandards_NoError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(buildEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId(), "2015"))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(buildCriterion(1L, "170.315 (a)(1)"))
                        .build())
                .build();
        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_UnattestedCriterionWithExistingTestStandard_NoError() {
        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(getCertResultTestStandard(1L, "mock"));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(buildEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId(), "2015"))
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(buildCriterion(1L, "170.315 (a)(1)"))
                        .testStandards(testStandards)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_AttestedCriterionWithExistingTestStandard_NoError() {
        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(getCertResultTestStandard(1L, "mock"));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(buildEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId(), "2015"))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(buildCriterion(1L, "170.315 (a)(1)"))
                        .testStandards(testStandards)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_UnattestedCriterionExistingTestStandardWithoutId_NoError() {
        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(getCertResultTestStandard(null, "mock"));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(buildEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId(), "2015"))
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(buildCriterion(1L, "170.315 (a)(1)"))
                        .testStandards(testStandards)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_AttestedCriterionExistingTestStandardWithoutId_NoError() {
        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(getCertResultTestStandard(null, "mock"));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(buildEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId(), "2015"))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(buildCriterion(1L, "170.315 (a)(1)"))
                        .testStandards(testStandards)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_UnattestedCriterionNonexistentTestStandard_HasError() {
        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(getCertResultTestStandard(2L, "does not exist"));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(buildEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId(), "2015"))
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(buildCriterion(1L, "170.315 (a)(1)"))
                        .testStandards(testStandards)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
    }

    @Test
    public void review_AttestedCriterionNonexistentTestStandard_HasError() {
        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(getCertResultTestStandard(2L, "does not exist"));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(buildEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId(), "2015"))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(buildCriterion(1L, "170.315 (a)(1)"))
                        .testStandards(testStandards)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
    }

    @Test
    public void review_UnattestedCriterionNonexistentTestStandardNoId_HasError() {
        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(getCertResultTestStandard(null, "does not exist"));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(buildEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId(), "2015"))
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(buildCriterion(1L, "170.315 (a)(1)"))
                        .testStandards(testStandards)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
    }

    @Test
    public void review_AttestedCriterionNonexistentTestStandardNoId_HasError() {
        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(getCertResultTestStandard(null, "does not exist"));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(buildEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId(), "2015"))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(buildCriterion(1L, "170.315 (a)(1)"))
                        .testStandards(testStandards)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
    }

    @Test
    public void review_UnattestedCriterionNoTestStandardName_NoError() {
        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(getCertResultTestStandard(1L, null));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(buildEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId(), "2015"))
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(buildCriterion(1L, "170.315 (a)(1)"))
                        .testStandards(testStandards)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_AttestedCriterionNoTestStandardName_NoError() {
        List<CertificationResultTestStandard> testStandards = new ArrayList<CertificationResultTestStandard>();
        testStandards.add(getCertResultTestStandard(1L, null));

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(buildEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId(), "2015"))
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(buildCriterion(1L, "170.315 (a)(1)"))
                        .testStandards(testStandards)
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    private CertificationCriterion buildCriterion(Long id, String number) {
        return CertificationCriterion.builder()
                .id(id)
                .number(number)
                .build();
    }

    private CertificationResultTestStandard getCertResultTestStandard(Long id, String name) {
        return CertificationResultTestStandard.builder()
                .testStandardId(id)
                .testStandardName(name)
                .build();
    }

    private Map<String, Object> buildEdition(Long id, String name) {
        Map<String, Object> edition = new HashMap<String, Object>();
        edition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, id);
        edition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, name);
        return edition;
    }
}
