package gov.healthit.chpl.validation.surveillance.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.compliance.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.surveillance.RequirementGroupType;
import gov.healthit.chpl.domain.surveillance.RequirementType;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SurveillanceRequirementReviewerTest {
    private SurveillanceDAO surveillanceDAO;
    private ErrorMessageUtil errorMessageUtil;
    private DimensionalDataManager dimensionalDataManager;

    private SurveillanceRequirementReviewer reviewer;

    @Before
    public void before() {
        surveillanceDAO = Mockito.mock(SurveillanceDAO.class);
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        dimensionalDataManager = Mockito.mock(DimensionalDataManager.class);

        Mockito.when(surveillanceDAO.findSurveillanceResultType(ArgumentMatchers.anyLong()))
            .thenReturn(getNoNonConformityResultType());

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
            .thenReturn("This is an error message.");
    }

    @Test
    public void review_requirementNotCriteriaRelatedIsValid_noErrorMessages() {
        RequirementType requirementType = RequirementType.builder()
                .id(200L)
                .startDay(null)
                .endDay(null)
                .requirementGroupType(RequirementGroupType.builder()
                        .id(4L)
                        .name("Real World Testing Submission")
                        .build())
                .title("Annual Real World Testing Results Reports")
                .build();

        Surveillance surveillance = Surveillance.builder()
                .id(null)
                .startDay(LocalDate.now())
                .requirements(new LinkedHashSet<SurveillanceRequirement>(Collections.singleton(SurveillanceRequirement.builder()
                        .id(1L)
                        .result(getNoNonConformityResultType())
                        .requirementType(requirementType)
                        .build())))
                .build();
        surveillance.setErrorMessages(new HashSet<String>());

        Mockito.when(dimensionalDataManager.getRequirementTypes())
                .thenReturn(Set.of(requirementType));

        reviewer = new SurveillanceRequirementReviewer(surveillanceDAO, errorMessageUtil, dimensionalDataManager);
        reviewer.review(surveillance);

        assertEquals(0, surveillance.getErrorMessages().size());
    }

    @Test
    public void review_requirementCriteriaRelatedWithSurvStartDateEqualCriteriaStartDateIsValid_noErrorMessages() {
        Surveillance surveillance = Surveillance.builder()
                .id(null)
                .startDay(LocalDate.of(2023, 10, 18))
                .requirements(new LinkedHashSet<SurveillanceRequirement>(Collections.singleton(SurveillanceRequirement.builder()
                        .id(1L)
                        .result(getNoNonConformityResultType())
                        .requirementType(getRequirementType())
                        .build())))
                .build();
        surveillance.setErrorMessages(new HashSet<String>());

        Mockito.when(dimensionalDataManager.getRequirementTypes())
                .thenReturn(Set.of(getRequirementType()));


        reviewer = new SurveillanceRequirementReviewer(surveillanceDAO, errorMessageUtil, dimensionalDataManager);
        reviewer.review(surveillance);

        assertEquals(0, surveillance.getErrorMessages().size());
    }

    @Test
    public void review_requirementCriteriaRelatedWithSurvStartDateEqualCriteriaEndDateIsValid_noErrorMessages() {
        Surveillance surveillance = Surveillance.builder()
                .id(null)
                .startDay(LocalDate.of(2023, 10, 18))
                .requirements(new LinkedHashSet<SurveillanceRequirement>(Collections.singleton(SurveillanceRequirement.builder()
                        .id(1L)
                        .result(getNoNonConformityResultType())
                        .requirementType(getRequirementType())
                        .build())))
                .build();
        surveillance.setErrorMessages(new HashSet<String>());

        Mockito.when(dimensionalDataManager.getRequirementTypes())
                .thenReturn(Set.of(getRequirementType()));


        reviewer = new SurveillanceRequirementReviewer(surveillanceDAO, errorMessageUtil, dimensionalDataManager);
        reviewer.review(surveillance);

        assertEquals(0, surveillance.getErrorMessages().size());
    }

    @Test
    public void review_requirementCriteriaRelatedWithSurvStartDateBetweenCriteriaStartAndEndDateIsValid_noErrorMessages() {
        Surveillance surveillance = Surveillance.builder()
                .id(null)
                .startDay(LocalDate.of(2022, 10, 18))
                .requirements(new LinkedHashSet<SurveillanceRequirement>(Collections.singleton(SurveillanceRequirement.builder()
                        .id(1L)
                        .result(getNoNonConformityResultType())
                        .requirementType(getRequirementType())
                        .build())))
                .build();
        surveillance.setErrorMessages(new HashSet<String>());

        Mockito.when(dimensionalDataManager.getRequirementTypes())
                .thenReturn(Set.of(getRequirementType()));


        reviewer = new SurveillanceRequirementReviewer(surveillanceDAO, errorMessageUtil, dimensionalDataManager);
        reviewer.review(surveillance);

        assertEquals(0, surveillance.getErrorMessages().size());
    }

    @Test
    public void review_requirementCriteriaRelatedWithSurvStartDateBeforeCriteriaStartDateIsNotValid_errorMessagesExist() {
        Surveillance surveillance = Surveillance.builder()
                .id(null)
                .startDay(LocalDate.of(1999, 10, 18))
                .requirements(new LinkedHashSet<SurveillanceRequirement>(Collections.singleton(SurveillanceRequirement.builder()
                        .id(1L)
                        .result(getNoNonConformityResultType())
                        .requirementType(getRequirementType())
                        .build())))
                .build();
        surveillance.setErrorMessages(new HashSet<String>());

        Mockito.when(dimensionalDataManager.getRequirementTypes())
                .thenReturn(Set.of(getRequirementType()));


        reviewer = new SurveillanceRequirementReviewer(surveillanceDAO, errorMessageUtil, dimensionalDataManager);
        reviewer.review(surveillance);

        assertEquals(1, surveillance.getErrorMessages().size());
    }

    @Test
    public void review_requirementCriteriaRelatedWithSurvStartDateAfterCriteriaEndDateIsNotValid_errorMessagesExist() {
        Surveillance surveillance = Surveillance.builder()
                .id(null)
                .startDay(LocalDate.of(2024, 10, 18))
                .requirements(new LinkedHashSet<SurveillanceRequirement>(Collections.singleton(SurveillanceRequirement.builder()
                        .id(1L)
                        .result(getNoNonConformityResultType())
                        .requirementType(getRequirementType())
                        .build())))
                .build();
        surveillance.setErrorMessages(new HashSet<String>());

        Mockito.when(dimensionalDataManager.getRequirementTypes())
                .thenReturn(Set.of(getRequirementType()));


        reviewer = new SurveillanceRequirementReviewer(surveillanceDAO, errorMessageUtil, dimensionalDataManager);
        reviewer.review(surveillance);

        assertEquals(1, surveillance.getErrorMessages().size());
    }

    private RequirementType getRequirementType() {
        return RequirementType.builder()
                .id(18L)
                .startDay(LocalDate.of(2020, 10, 18))
                .endDay(LocalDate.of(2023, 10, 18))
                .requirementGroupType(RequirementGroupType.builder()
                        .id(1L)
                        .name("Certified Capability")
                        .build())
                .title("Implantable Device List")
                .number("170.315 (a)(14)")
                .build();

    }

    private SurveillanceResultType getNoNonConformityResultType() {
        return SurveillanceResultType.builder()
                .id(2L)
                .name("No Non-conformity")
                .build();
    }
}
