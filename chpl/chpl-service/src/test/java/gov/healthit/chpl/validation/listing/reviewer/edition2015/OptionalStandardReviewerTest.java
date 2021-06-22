package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.OptionalStandardReviewer;

public class OptionalStandardReviewerTest {
    private static final String INVALID_EDITION_ERROR_KEY = "listing.criteria.optionalStandard.invalidEdition";
    private static final String INVALID_OPTIONAL_STANDARD_CRITERIA_ERROR_KEY = "listing.criteria.optionalStandard.invalidCriteria";

    private OptionalStandardDAO optionalStandardDAO;
    private ErrorMessageUtil errorMessageUtil;
    private OptionalStandardReviewer optionalStandardReviewer;
    private FF4j ff4j;

    @Before
    public void before() throws EntityRetrievalException {
        optionalStandardDAO = Mockito.mock(OptionalStandardDAO.class);
        Mockito.when(optionalStandardDAO.getAllOptionalStandardCriteriaMap())
        .thenReturn(getOptionalStandardCriteriaMaps());

        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(INVALID_EDITION_ERROR_KEY))
        .thenReturn("Test Error Message 1");
        Mockito.when(errorMessageUtil.getMessage(INVALID_OPTIONAL_STANDARD_CRITERIA_ERROR_KEY))
        .thenReturn("Test Error Message 2");

        ff4j = Mockito.mock(FF4j.class);
        Mockito.when(ff4j.check(FeatureList.OPTIONAL_STANDARDS))
        .thenReturn(true);

        optionalStandardReviewer = new OptionalStandardReviewer(optionalStandardDAO, errorMessageUtil, ff4j);
    }

    @Test
    public void review_validOptionalStandardAndValidCriterion_NoErrors() {
        Map<String, Object> certEdition = new HashMap<String, Object>();
        certEdition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        certEdition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(6)")
                                .id(1L)
                                .build())
                        .optionalStandard(CertificationResultOptionalStandard.builder()
                                .optionalStandard(OptionalStandard.builder()
                                        .id(1L)
                                        .citation("std1")
                                        .build())
                                .build())
                        .build())
                .certificationEdition(certEdition)
                .build();

        optionalStandardReviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_invalidOptionalStandardCriterionCombination_ErrorMessageExists() {
        Map<String, Object> certEdition = new HashMap<String, Object>();
        certEdition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        certEdition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(6)")
                                .id(1L)
                                .build())
                        .optionalStandard(CertificationResultOptionalStandard.builder()
                                .optionalStandard(OptionalStandard.builder()
                                        .id(2L)
                                        .citation("bad std1")
                                        .build())
                                .build())
                        .build())
                .certificationEdition(certEdition)
                .build();


        optionalStandardReviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
    }

    private List<OptionalStandardCriteriaMap> getOptionalStandardCriteriaMaps() {
        List<OptionalStandardCriteriaMap> map = new ArrayList<OptionalStandardCriteriaMap>();

        map.add(OptionalStandardCriteriaMap.builder()
                .id(1L)
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .certificationEdition("2015")
                        .certificationEditionId(3L)
                        .number("170.315 (a)(6)")
                        .removed(false)
                        .build())
                .optionalStandard(OptionalStandard.builder()
                        .id(1L)
                        .citation("std1")
                        .build())
                .build());

        return map;
    }
}
