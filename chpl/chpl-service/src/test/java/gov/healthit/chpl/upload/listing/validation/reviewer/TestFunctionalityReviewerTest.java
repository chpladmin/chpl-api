package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.TestFunctionalityCriteriaMapDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class TestFunctionalityReviewerTest {
    private static final String TEST_FUNCTIONALITY_NOT_APPLICABLE = "Test functionality is not applicable for the criterion %s. It has been removed.";
    private static final String TEST_FUNCTIONALITY_NOT_FOUND_REMOVED = "Criteria %s contains an invalid test functionality '%s'. It has been removed from the pending listing.";
    private static final String MISSING_TEST_FUNCTIONALITY_NAME = "There was no test functionality name found for certification criteria %s.";
    private static final String TEST_FUNCTIONALITY_CRITERION_MISMATCH = "In Criteria %s, Test Functionality %s is for Criteria %s and is not valid for Criteria %s. The invalid Test Functionality has been removed.";

    private ErrorMessageUtil msgUtil;
    private CertificationResultRules certResultRules;
    private TestFunctionalityDAO testFunctionalityDao;
    private TestFunctionalityReviewer reviewer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        certResultRules = Mockito.mock(CertificationResultRules.class);
        testFunctionalityDao = Mockito.mock(TestFunctionalityDAO.class);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(testFunctionalityDao.getTestFunctionalityCriteriaMaps(ArgumentMatchers.eq("2015")))
            .thenReturn(createDefaultTestFunctionalityMaps());
        Mockito.when(testFunctionalityDao.getTestFunctionalityCritieriaMaps())
            .thenReturn(createDefaultTestFunctionalityCriteriaMaps());

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testFunctionalityNotApplicable"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_FUNCTIONALITY_NOT_APPLICABLE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testFunctionalityNotFoundAndRemoved"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_FUNCTIONALITY_NOT_FOUND_REMOVED, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestFunctionalityName"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_TEST_FUNCTIONALITY_NAME, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testFunctionalityCriterionMismatch"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(TEST_FUNCTIONALITY_CRITERION_MISMATCH, i.getArgument(1), i.getArgument(2), i.getArgument(3), i.getArgument(4)));
        reviewer = new TestFunctionalityReviewer(certResultRules,
                new ValidationUtils(Mockito.mock(CertificationCriterionService.class)),
                testFunctionalityDao, msgUtil);
    }

    @Test
    public void review_nullTestFunctionality_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
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
        listing.getCertificationResults().get(0).setFunctionalitiesTested(null);
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptyTestFunctionality_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
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
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testFunctionalityNotApplicableToCriteria_hasWarningAndTestFunctionalitySetNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(false);
        List<CertificationResultTestFunctionality> testFuncs = new ArrayList<CertificationResultTestFunctionality>();
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(1L)
                .name("test func")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .functionalitiesTested(testFuncs)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(TEST_FUNCTIONALITY_NOT_APPLICABLE, "170.315 (a)(1)")));
        assertNull(listing.getCertificationResults().get(0).getFunctionalitiesTested());
    }

    @Test
    public void review_testFunctionalityNotApplicableToRemovedCriteria_noWarning() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(false);
        List<CertificationResultTestFunctionality> testFuncs = new ArrayList<CertificationResultTestFunctionality>();
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(1L)
                .name("test func")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .success(true)
                        .functionalitiesTested(testFuncs)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_removesTestFunctionalityWithoutId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultTestFunctionality> testFuncs = new ArrayList<CertificationResultTestFunctionality>();
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(1L)
                .name("test func")
                .build());
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .name("bad name")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .functionalitiesTested(testFuncs)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(TEST_FUNCTIONALITY_NOT_FOUND_REMOVED, "170.315 (a)(1)", "bad name")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_ignoresTestFunctionalityWithoutIdForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultTestFunctionality> testFuncs = new ArrayList<CertificationResultTestFunctionality>();
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(1L)
                .name("test func")
                .build());
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .name("bad name")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .success(true)
                        .functionalitiesTested(testFuncs)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testFunctionalityWithoutNameWithoutId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultTestFunctionality> testFuncs = new ArrayList<CertificationResultTestFunctionality>();
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(1L)
                .name("test func")
                .build());
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(null)
                .name("")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .functionalitiesTested(testFuncs)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(TEST_FUNCTIONALITY_NOT_FOUND_REMOVED, "170.315 (a)(1)", "")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testFunctionalityWithoutNameWithoutIdForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultTestFunctionality> testFuncs = new ArrayList<CertificationResultTestFunctionality>();
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(1L)
                .name("test func")
                .build());
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(null)
                .name("")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .success(true)
                        .functionalitiesTested(testFuncs)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testFunctionalityWithoutNameWithId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultTestFunctionality> testFuncs = new ArrayList<CertificationResultTestFunctionality>();
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(1L)
                .name("test func")
                .build());
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(2L)
                .name("")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .functionalitiesTested(testFuncs)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(2, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(MISSING_TEST_FUNCTIONALITY_NAME, "170.315 (a)(1)", "")));
    }

    @Test
    public void review_testFunctionalityWithoutNameWithIdForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultTestFunctionality> testFuncs = new ArrayList<CertificationResultTestFunctionality>();
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(1L)
                .name("test func")
                .build());
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(2L)
                .name("")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .success(true)
                        .functionalitiesTested(testFuncs)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testFunctionalityWithIdCriteriaMismatch_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultTestFunctionality> testFuncs = new ArrayList<CertificationResultTestFunctionality>();
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(1L)
                .name("valid func")
                .build());
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(3L)
                .name("mismatch")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                    .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .functionalitiesTested(testFuncs)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(TEST_FUNCTIONALITY_CRITERION_MISMATCH, "170.315 (a)(1)", "mismatch",
                        "170.315 (a)(2)", "170.315 (a)(1)")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_testFunctionalityWithIdRemovedCriteriaMismatch_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultTestFunctionality> testFuncs = new ArrayList<CertificationResultTestFunctionality>();
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(1L)
                .name("valid func")
                .build());
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(3L)
                .name("mismatch")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                    .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(true)
                                .build())
                        .success(true)
                        .functionalitiesTested(testFuncs)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_validTestFunctionality_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultTestFunctionality> testFuncs = new ArrayList<CertificationResultTestFunctionality>();
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(1L)
                .name("valid func")
                .build());
        testFuncs.add(CertificationResultTestFunctionality.builder()
                .testFunctionalityId(4L)
                .name("another test func")
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                    .certificationEdition(create2015EditionMap())
                .certificationResult(CertificationResult.builder()
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .removed(false)
                                .build())
                        .success(true)
                        .functionalitiesTested(testFuncs)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(2, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    private Map<Long, List<TestFunctionalityDTO>> createDefaultTestFunctionalityMaps() {
        Map<Long, List<TestFunctionalityDTO>> testFuncMaps = new HashMap<Long, List<TestFunctionalityDTO>>();
        testFuncMaps.put(1L, new ArrayList<TestFunctionalityDTO>());
        testFuncMaps.get(1L).add(TestFunctionalityDTO.builder()
                .id(1L)
                .name("test func")
                .number("test func")
                .build());
        testFuncMaps.get(1L).add(TestFunctionalityDTO.builder()
                .id(2L)
                .name("")
                .number("")
                .build());
        testFuncMaps.get(1L).add(TestFunctionalityDTO.builder()
                .id(4L)
                .name("another test func")
                .number("another test func")
                .build());
        return testFuncMaps;
    }

    private List<TestFunctionalityCriteriaMapDTO> createDefaultTestFunctionalityCriteriaMaps() {
        List<TestFunctionalityCriteriaMapDTO> maps = new ArrayList<TestFunctionalityCriteriaMapDTO>();
        maps.add(TestFunctionalityCriteriaMapDTO.builder()
                .criteria(CertificationCriterionDTO.builder()
                        .certificationEdition("2015")
                        .certificationEditionId(1L)
                        .number("170.315 (a)(2)")
                        .id(2L)
                        .build())
                .id(1L)
                .testFunctionality(TestFunctionalityDTO.builder()
                        .id(3L)
                        .name("mismatch")
                        .number("mismatch")
                        .build())
                .build());
        return maps;
    }

    private Map<String, Object> create2015EditionMap() {
        Map<String, Object> editionMap = new HashMap<String, Object>();
        editionMap.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        editionMap.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");
        return editionMap;
    }
}
