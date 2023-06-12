package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class OptionalStandardReviewerTest {
    private static final String INVALID_EDITION_ERROR_KEY = "listing.criteria.optionalStandard.invalidEdition";
    private static final String INVALID_OPTIONAL_STANDARD_CRITERIA_ERROR_KEY = "listing.criteria.optionalStandard.invalidCriteria";

    private OptionalStandardDAO optionalStandardDAO;
    private ErrorMessageUtil errorMessageUtil;
    private OptionalStandardReviewer optionalStandardReviewer;
    private ResourcePermissions resourcePermissions;

    @Before
    public void before() throws EntityRetrievalException {
        optionalStandardDAO = Mockito.mock(OptionalStandardDAO.class);
        Mockito.when(optionalStandardDAO.getAllOptionalStandardCriteriaMap())
                .thenReturn(getOptionalStandardCriteriaMaps());

        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(INVALID_EDITION_ERROR_KEY), ArgumentMatchers.any()))
                .thenReturn("Test Error Message 1");
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(INVALID_OPTIONAL_STANDARD_CRITERIA_ERROR_KEY), ArgumentMatchers.any()))
                .thenReturn("Test Error Message 2");

        resourcePermissions = Mockito.mock(ResourcePermissions.class);

        optionalStandardReviewer = new OptionalStandardReviewer(optionalStandardDAO, errorMessageUtil, resourcePermissions);
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
                        .optionalStandards(Stream.of(CertificationResultOptionalStandard.builder()
                                .optionalStandardId(1L)
                                .citation("std1")
                                .build()).collect(Collectors.toList()))
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
                        .optionalStandards(Stream.of(CertificationResultOptionalStandard.builder()
                                .optionalStandardId(2L)
                                .citation("bad std1")
                                .build()).collect(Collectors.toList()))
                        .build())
                .certificationEdition(certEdition)
                .build();

        optionalStandardReviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
    }

    @Test
    public void review_invalidOptionalStandardCriterionCombinationWhenCriteriaRemovedAndRoleAcb_NoMessageExists() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        Map<String, Object> certEdition = new HashMap<String, Object>();
        certEdition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        certEdition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(13)")
                                .id(1L)
                                .removed(true)
                                .build())
                        .optionalStandards(Stream.of(CertificationResultOptionalStandard.builder()
                                .optionalStandardId(2L)
                                .citation("bad std1")
                                .build()).collect(Collectors.toList()))
                        .build())
                .certificationEdition(certEdition)
                .build();

        optionalStandardReviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_invalidOptionalStandardCriterionCombinationWhenCriteriaRemovedAndRoleAdmin_noWarnings() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);

        Map<String, Object> certEdition = new HashMap<String, Object>();
        certEdition.put(CertifiedProductSearchDetails.EDITION_ID_KEY, 3L);
        certEdition.put(CertifiedProductSearchDetails.EDITION_NAME_KEY, "2015");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .id(1L)
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .number("170.315 (a)(13)")
                                .id(1L)
                                .removed(true)
                                .build())
                        .optionalStandards(Stream.of(CertificationResultOptionalStandard.builder()
                                .optionalStandardId(2L)
                                .citation("bad std1")
                                .build()).collect(Collectors.toList()))
                        .build())
                .certificationEdition(certEdition)
                .build();

        optionalStandardReviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
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
