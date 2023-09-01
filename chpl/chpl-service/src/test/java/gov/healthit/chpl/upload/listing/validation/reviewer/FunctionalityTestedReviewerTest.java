package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.functionalitytested.FunctionalityTested;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedDAO;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class FunctionalityTestedReviewerTest {
    private static final String FUNCTIONALITIES_TESTED_NOT_APPLICABLE = "Functionality tested is not applicable for the criterion %s. It has been removed.";
    private static final String FUNCTIONALITIES_TESTED_NOT_FOUND_REMOVED = "Criteria %s contains an invalid Functionality Tested '%s'. It has been removed from the pending listing.";
    private static final String MISSING_FUNCTIONALITY_TESTED_NAME = "There was no Functionality Tested name found for certification criteria %s.";
    private static final String FUNCTIONALITY_TESTED_CRITERION_MISMATCH = "In Criteria %s, Functionality Tested %s is for Criteria %s and is not valid for Criteria %s. The invalid Functionality Tested has been removed.";

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

        Mockito.when(functionalityTestedDao.getFunctionalitiesTestedCriteriaMaps())
            .thenReturn(createDefaultFunctionalityTestedCriteriaMaps());

        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.functionalityTestedNotApplicable"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(FUNCTIONALITIES_TESTED_NOT_APPLICABLE, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.functionalityTestedNotFoundAndRemoved"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(FUNCTIONALITIES_TESTED_NOT_FOUND_REMOVED, i.getArgument(1), i.getArgument(2)));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.missingFunctionalityTestedName"),
                ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(MISSING_FUNCTIONALITY_TESTED_NAME, i.getArgument(1), ""));
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.criteria.functionalityTestedCriterionMismatch"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(FUNCTIONALITY_TESTED_CRITERION_MISMATCH, i.getArgument(1), i.getArgument(2), i.getArgument(3), i.getArgument(4)));
        reviewer = new FunctionalityTestedReviewer(certResultRules,
                new ValidationUtils(Mockito.mock(CertificationCriterionService.class)),
                functionalityTestedDao, msgUtil);
    }

    @Test
    public void review_nullFunctionalitiesTested_noError() {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(false);
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(1L)
                        .regulatoryTextCitation("func tested")
                        .build())
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(false);
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(1L)
                        .regulatoryTextCitation("func tested")
                        .build())
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(1L)
                        .regulatoryTextCitation("func tested")
                        .build())
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .regulatoryTextCitation("bad name")
                        .build())
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(1L)
                        .regulatoryTextCitation("func tested")
                        .build())
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .regulatoryTextCitation("bad name")
                        .build())
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(1L)
                        .regulatoryTextCitation("func tested")
                        .build())
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(null)
                        .regulatoryTextCitation("")
                        .build())
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(1L)
                        .regulatoryTextCitation("func tested")
                        .build())
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(null)
                        .regulatoryTextCitation("")
                        .build())
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(1L)
                        .regulatoryTextCitation("func tested")
                        .build())
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(2L)
                        .regulatoryTextCitation("")
                        .build())
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(1L)
                        .regulatoryTextCitation("func tested")
                        .build())
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(2L)
                        .regulatoryTextCitation("")
                        .build())
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
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
    public void review_functionalityTestedWithIdCriteriaMismatch_hasError() throws EntityRetrievalException {
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);
        Mockito.when(functionalityTestedDao.getById(ArgumentMatchers.anyLong()))
            .thenReturn(FunctionalityTested.builder()
                    .id(3L)
                    .regulatoryTextCitation("mismatch")
                    .value("mismatch")
                    .criteria(Stream.of(CertificationCriterion.builder()
                            .id(2L)
                            .number("170.315 (a)(2)")
                            .title("a2")
                            .removed(false)
                            .build()).collect(Collectors.toList()))
                    .build());
        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(1L)
                        .regulatoryTextCitation("valid func")
                        .build())
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(3L)
                        .regulatoryTextCitation("mismatch")
                        .build())
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(1L)
                        .regulatoryTextCitation("func tested")
                        .build())
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(3L)
                        .regulatoryTextCitation("mismatch")
                        .build())
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
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
        Mockito.when(certResultRules.hasCertOption(ArgumentMatchers.anyLong(), ArgumentMatchers.eq(CertificationResultRules.FUNCTIONALITY_TESTED)))
            .thenReturn(true);

        List<CertificationResultFunctionalityTested> functionalitiesTested = new ArrayList<CertificationResultFunctionalityTested>();
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(1L)
                        .regulatoryTextCitation("valid func")
                        .build())
                .build());
        functionalitiesTested.add(CertificationResultFunctionalityTested.builder()
                .functionalityTested(FunctionalityTested.builder()
                        .id(4L)
                        .regulatoryTextCitation("another func tested")
                        .build())
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
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

    private Map<Long, List<FunctionalityTested>> createDefaultFunctionalityTestedCriteriaMaps() {
        Map<Long, List<FunctionalityTested>> maps = new HashMap<Long, List<FunctionalityTested>>();
        List<FunctionalityTested> a1FunctionalityTested = new ArrayList<FunctionalityTested>();
        List<FunctionalityTested> a2FunctionalityTested = new ArrayList<FunctionalityTested>();
        a1FunctionalityTested.add(FunctionalityTested.builder()
                .id(1L)
                .regulatoryTextCitation("func tested")
                .value("func tested")
                .criteria(Stream.of(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .title("a1")
                        .removed(false)
                        .build()).collect(Collectors.toList()))
                .build());
        a1FunctionalityTested.add(FunctionalityTested.builder()
                .id(2L)
                .regulatoryTextCitation("")
                .value("")
                .criteria(Stream.of(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .title("a1")
                        .removed(false)
                        .build()).collect(Collectors.toList()))
                .build());
        a1FunctionalityTested.add(FunctionalityTested.builder()
                .id(4L)
                .regulatoryTextCitation("another func tested")
                .value("another func tested")
                .criteria(Stream.of(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .title("a1")
                        .removed(false)
                        .build()).collect(Collectors.toList()))
                .build());
        a2FunctionalityTested.add(FunctionalityTested.builder()
                .id(3L)
                .regulatoryTextCitation("mismatch")
                .value("mismatch")
                .criteria(Stream.of(CertificationCriterion.builder()
                        .id(2L)
                        .number("170.315 (a)(2)")
                        .title("a2")
                        .removed(false)
                        .build()).collect(Collectors.toList()))
                .build());

        maps.put(1L, a1FunctionalityTested);
        maps.put(2L, a2FunctionalityTested);
        return maps;
    }
}
