package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;

import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.concept.RequirementTypeEnum;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceNonconformityReviewer;

public class SurveillanceNonconformityReviewerTest {
    private static final String NONCONFORMITY_TYPE_REMOVED_MSG_KEY = "surveillance.nonconformityTypeRemoved";
    private static final String NONCONFORMITY_TYPE_REMOVED_MSG = "Test Error Message - Non-conformity Removed";

    private CertificationResultDetailsDAO certResultDetailsDao;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationCriterionService criterionService;

    private SurveillanceNonconformityReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(NONCONFORMITY_TYPE_REMOVED_MSG_KEY), ArgumentMatchers.any()))
                .thenReturn(NONCONFORMITY_TYPE_REMOVED_MSG);

        certResultDetailsDao = Mockito.mock(CertificationResultDetailsDAO.class);
        Mockito.when(certResultDetailsDao.getCertificationResultsForSurveillanceListing(ArgumentMatchers.any()))
                .thenReturn(new ArrayList<CertificationResultDetailsDTO>());

        criterionService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criterionService.coerceToCriterionNumberFormat(ArgumentMatchers.anyString()))
                .thenCallRealMethod();

        reviewer = new SurveillanceNonconformityReviewer(certResultDetailsDao, errorMessageUtil, criterionService);
    }

    @Test
    public void review_NonconformityTypeNotRemoved_NoErrors() {
        Surveillance surv = Surveillance.builder()
                .requirements(Sets.newSet(SurveillanceRequirement.builder()
                        .requirement(RequirementTypeEnum.K1.getName())
                        .type(SurveillanceRequirementType.builder()
                                .name("Transparency or Disclosure Requirement")
                                .id(2L)
                                .build())
                        .result(SurveillanceResultType.builder()
                                .id(1L)
                                .name(SurveillanceResultType.NON_CONFORMITY)
                                .build())
                        .nonconformities(Arrays.asList(SurveillanceNonconformity.builder()
                                .dateOfDeterminationDay(LocalDate.now())
                                .summary("Summary")
                                .findings("Findings")
                                .nonconformityType(NonconformityType.K1.getName())
                                .build()))
                        .build()))
                .build();
        surv.setErrorMessages(new HashSet<String>());

        reviewer.review(surv);

        assertEquals(0, surv.getErrorMessages().size());
    }

    @Test
    public void review_NonconformityTypeRemoved_ErrorMessageExists() {
        Surveillance surv = Surveillance.builder()
                .requirements(Sets.newSet(SurveillanceRequirement.builder()
                        .requirement(RequirementTypeEnum.K1.getName())
                        .type(SurveillanceRequirementType.builder()
                                .name("Transparency or Disclosure Requirement")
                                .id(2L)
                                .build())
                        .result(SurveillanceResultType.builder()
                                .id(1L)
                                .name(SurveillanceResultType.NON_CONFORMITY)
                                .build())
                        .nonconformities(Arrays.asList(SurveillanceNonconformity.builder()
                                .dateOfDeterminationDay(LocalDate.now())
                                .summary("Summary")
                                .findings("Findings")
                                .nonconformityType(NonconformityType.K2.getName())
                                .build()))
                        .build()))
                .build();
        surv.setErrorMessages(new HashSet<String>());

        reviewer.review(surv);

        assertEquals(1, surv.getErrorMessages().size());
        assertTrue(surv.getErrorMessages().contains(NONCONFORMITY_TYPE_REMOVED_MSG));
    }

}
