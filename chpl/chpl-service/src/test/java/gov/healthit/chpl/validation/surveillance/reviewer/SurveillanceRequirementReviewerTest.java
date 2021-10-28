package gov.healthit.chpl.validation.surveillance.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;

import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.concept.RequirementTypeEnum;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SurveillanceRequirementReviewerTest {
    private static final String REQUIREMENT_REMOVED_MSG_KEY = "surveillance.requirementRemoved";
    private static final String REQUIREMENT_REMOVED_MSG = "Test Error Message - Requirement Removed";

    private SurveillanceRequirementReviewer reviewer;

    private CertificationResultDetailsDAO certResultDetailsDao;
    private SurveillanceDAO survDao;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationCriterionService criterionService;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq(REQUIREMENT_REMOVED_MSG_KEY), ArgumentMatchers.any()))
                .thenReturn(REQUIREMENT_REMOVED_MSG);

        certResultDetailsDao = Mockito.mock(CertificationResultDetailsDAO.class);
        Mockito.when(certResultDetailsDao.getCertificationResultsForSurveillanceListing(ArgumentMatchers.any()))
                .thenReturn(new ArrayList<CertificationResultDetailsDTO>());

        survDao = Mockito.mock(SurveillanceDAO.class);
        Mockito.when(survDao.findSurveillanceRequirementType(ArgumentMatchers.anyLong()))
                .thenReturn(SurveillanceRequirementType.builder()
                        .id(2L)
                        .name("Transparency or Disclosure Requirement")
                        .build());

        criterionService = Mockito.mock(CertificationCriterionService.class);
        Mockito.when(criterionService.coerceToCriterionNumberFormat(ArgumentMatchers.anyString()))
                .thenCallRealMethod();

        reviewer = new SurveillanceRequirementReviewer(survDao, certResultDetailsDao, errorMessageUtil, criterionService);
    }

    @Test
    public void review_RequirementNotRemoved_NoErrors() {
        Surveillance surv = Surveillance.builder()
                .requirements(Sets.newSet(SurveillanceRequirement.builder()
                        .requirement(RequirementTypeEnum.K1.getName())
                        .type(SurveillanceRequirementType.builder()
                                .name("Transparency or Disclosure Requirement")
                                .id(2L)
                                .build())
                        .build()))
                .build();
        surv.setErrorMessages(new HashSet<String>());

        reviewer.review(surv);

        assertEquals(0, surv.getErrorMessages().size());
    }

    @Test
    public void review_RequirementRemoved_ErrorMessageExists() {
        Surveillance surv = Surveillance.builder()
                .requirements(Sets.newSet(SurveillanceRequirement.builder()
                        .requirement(RequirementTypeEnum.K2.getName())
                        .type(SurveillanceRequirementType.builder()
                                .name("Transparency or Disclosure Requirement")
                                .id(2L)
                                .build())
                        .build()))
                .build();
        surv.setErrorMessages(new HashSet<String>());

        reviewer.review(surv);

        assertEquals(1, surv.getErrorMessages().size());
        assertTrue(surv.getErrorMessages().contains(REQUIREMENT_REMOVED_MSG));
    }

}
