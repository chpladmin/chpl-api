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

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedCriteriaMap;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedDAO;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class FunctionalityTestedReviewerTest {
    private static final String FUNCTIONALITIES_TESTED_NOT_APPLICABLE = "Test functionality is not applicable for the criterion %s. It has been removed.";
    private static final String FUNCTIONALITIES_TESTED_NOT_FOUND_REMOVED = "Criteria %s contains an invalid test functionality '%s'. It has been removed from the pending listing.";
    private static final String MISSING_FUNCTIONALITY_TESTED_NAME = "There was no test functionality name found for certification criteria %s.";
    private static final String FUNCTIONALITY_TESTED_CRITERION_MISMATCH = "In Criteria %s, Test Functionality %s is for Criteria %s and is not valid for Criteria %s. The invalid Test Functionality has been removed.";

    private ErrorMessageUtil msgUtil;
    private CertificationResultRules certResultRules;
    private FunctionalityTestedDAO functionalityTestedDao;
    private FunctionalityTestedReviewer reviewer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        certResultRules = Mockito.mock(CertificationResultRules.class);
        functionalityTestedDao = Mockito.mock(FunctionalityTestedDAO.class);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps(ArgumentMatchers.eq("2015")))
            .thenReturn(createDefaultFunctionalityTestedMaps());
        Mockito.when(functionalityTestedDao.getFunctionalitiesTestedCritieriaMaps())
            .thenReturn(createDefaultFunctionalityTestedCriteriaMaps());

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testFunctionalityNotApplicable"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(FUNCTIONALITIES_TESTED_NOT_APPLICABLE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testFunctionalityNotFoundAndRemoved"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(FUNCTIONALITIES_TESTED_NOT_FOUND_REMOVED, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingTestFunctionalityName"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_FUNCTIONALITY_TESTED_NAME, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.testFunctionalityCriterionMismatch"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(FUNCTIONALITY_TESTED_CRITERION_MISMATCH, i.getArgument(1), i.getArgument(2), i.getArgument(3), i.getArgument(4)));
        reviewer = new FunctionalityTestedReviewer(certResultRules,
                new ValidationUtils(Mockito.mock(CertificationCriterionService.class)),
                functionalityTestedDao, msgUtil);
    }

    @Test
    public void review_nullFunctionalitiesTested_noError() {
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
    public void review_emptyFunctionalitiesTested_noError() {
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
    public void review_functionalitiesTestedNotApplicableToCriteria_hasWarningAndFunctionalitiesTestedSetNull() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(false);
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(1L)
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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(FUNCTIONALITIES_TESTED_NOT_APPLICABLE, "170.315 (a)(1)")));
        assertNull(listing.getCertificationResults().get(0).getFunctionalitiesTested());
    }

    @Test
    public void review_functionalitiesTestedNotApplicableToRemovedCriteria_noWarning() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(false);
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(1L)
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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_removesFunctionalityTestedWithoutId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(1L)
                .name("test func")
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(FUNCTIONALITIES_TESTED_NOT_FOUND_REMOVED, "170.315 (a)(1)", "bad name")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_ignoresFunctionalityTestedWithoutIdForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(1L)
                .name("test func")
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_functionalityTestedWithoutNameWithoutId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(1L)
                .name("test func")
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(null)
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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(FUNCTIONALITIES_TESTED_NOT_FOUND_REMOVED, "170.315 (a)(1)", "")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_functionalityTestedWithoutNameWithoutIdForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(1L)
                .name("test func")
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(null)
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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_functionalityTestedWithoutNameWithId_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(1L)
                .name("test func")
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(2L)
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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(2, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                String.format(MISSING_FUNCTIONALITY_TESTED_NAME, "170.315 (a)(1)", "")));
    }

    @Test
    public void review_functionalityTestedWithoutNameWithIdForRemovedCriteria_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(1L)
                .name("test func")
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(2L)
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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_functionalityTestedWithIdCriteriaMismatch_hasError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(1L)
                .name("valid func")
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(3L)
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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(
                String.format(FUNCTIONALITY_TESTED_CRITERION_MISMATCH, "170.315 (a)(1)", "mismatch",
                        "170.315 (a)(2)", "170.315 (a)(1)")));
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_functionalityTestedWithIdRemovedCriteriaMismatch_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(1L)
                .name("valid func")
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(3L)
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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_validFunctionalitiesTested_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(1L)
                .name("valid func")
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTestedId(4L)
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
                        .functionalitiesTested(functionalitiesTested)
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(2, listing.getCertificationResults().get(0).getFunctionalitiesTested().size());
        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getErrorMessages().size());
    }

    private Map<Long, List<FunctionalityTested>> createDefaultFunctionalityTestedMaps() {
        Map<Long, List<FunctionalityTested>> functionalityTestedMaps = new HashMap<Long, List<FunctionalityTested>>();
        functionalityTestedMaps.put(1L, new ArrayList<FunctionalityTested>());
        functionalityTestedMaps.get(1L).add(FunctionalityTested.builder()
                .id(1L)
                .name("test func")
                .description("test func")
                .build());
        functionalityTestedMaps.get(1L).add(FunctionalityTested.builder()
                .id(2L)
                .name("")
                .description("")
                .build());
        functionalityTestedMaps.get(1L).add(FunctionalityTested.builder()
                .id(4L)
                .name("another test func")
                .description("another test func")
                .build());
        return functionalityTestedMaps;
    }

    private List<FunctionalityTestedCriteriaMap> createDefaultFunctionalityTestedCriteriaMaps() {
        List<FunctionalityTestedCriteriaMap> maps = new ArrayList<FunctionalityTestedCriteriaMap>();
        maps.add(FunctionalityTestedCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                        .certificationEdition("2015")
                        .certificationEditionId(1L)
                        .number("170.315 (a)(2)")
                        .id(2L)
                        .build())
                .id(1L)
                .functionalityTested(FunctionalityTested.builder()
                        .id(3L)
                        .name("mismatch")
                        .description("mismatch")
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
