package gov.healthit.chpl.validation.surveillance.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.surveillance.RequirementGroupType;
import gov.healthit.chpl.domain.surveillance.RequirementType;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class SurveillanceNonconformityReviewerTest {
    private ErrorMessageUtil errorMessageUtil;


    @Before
    public void before() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
            .thenReturn("This is an error message.");
    }


    @Test
    public void review_nonConformityNotCriteriaRelatedIsValid_noErrorMessages() {

    }

    @Test
    public void review_nonConformityCriteriaRelatedWithSurvStartDateEqualCriteriaStartDateIsValid_noErrorMessages() {
        Surveillance surveillance = Surveillance.builder()
                .id(1L)
                .startDay(LocalDate.of(2020, 10, 18))
                .requirements(new LinkedHashSet<SurveillanceRequirement>(Collections.singleton(SurveillanceRequirement.builder()
                        .id(1L)
                        .result(getNonConformityResultType())
                        .requirementType(getRequirementType())
                        .nonconformities(List.of(SurveillanceNonconformity.builder()
                                .id(1L)
                                .capApprovalDay(LocalDate.now())
                                .capMustCompleteDay(LocalDate.now())
                                .dateOfDeterminationDay(LocalDate.now())
                                .summary("Summary")
                                .findings("Findings")
                                .type(NonconformityType.builder()
                                        .id(1L)
                                        .startDay(LocalDate.of(2020, 10, 18))
                                        .endDay(LocalDate.of(2023, 10, 18))
                                        .title(null)
                                        .number(null)
                                        .build())
                                .build()))
                        .build())))
                .build();

        SurveillanceNonconformityReviewer reviewer = new SurveillanceNonconformityReviewer(errorMessageUtil);

        reviewer.review(surveillance);

        assertEquals(0, surveillance.getErrorMessages().size());
    }

    @Test
    public void review_nonConformityCriteriaRelatedWithSurvStartDateEqualCriteriaEndDateIsValid_noErrorMessages() {
        Surveillance surveillance = Surveillance.builder()
                .id(1L)
                .startDay(LocalDate.of(2023, 10, 18))
                .requirements(new LinkedHashSet<SurveillanceRequirement>(Collections.singleton(SurveillanceRequirement.builder()
                        .id(1L)
                        .result(getNonConformityResultType())
                        .requirementType(getRequirementType())
                        .nonconformities(List.of(SurveillanceNonconformity.builder()
                                .id(1L)
                                .capApprovalDay(LocalDate.now())
                                .capMustCompleteDay(LocalDate.now())
                                .dateOfDeterminationDay(LocalDate.now())
                                .summary("Summary")
                                .findings("Findings")
                                .type(NonconformityType.builder()
                                        .id(1L)
                                        .startDay(LocalDate.of(2020, 10, 18))
                                        .endDay(LocalDate.of(2023, 10, 18))
                                        .title(null)
                                        .number(null)
                                        .build())
                                .build()))
                        .build())))
                .build();

        SurveillanceNonconformityReviewer reviewer = new SurveillanceNonconformityReviewer(errorMessageUtil);

        reviewer.review(surveillance);

        assertEquals(0, surveillance.getErrorMessages().size());
    }

    @Test
    public void review_nonConformityCriteriaRelatedWithSurvStartDateBetweenCriteriaStartAndEndDateIsValid_noErrorMessages() {
        Surveillance surveillance = Surveillance.builder()
                .id(1L)
                .startDay(LocalDate.of(2022, 10, 18))
                .requirements(new LinkedHashSet<SurveillanceRequirement>(Collections.singleton(SurveillanceRequirement.builder()
                        .id(1L)
                        .result(getNonConformityResultType())
                        .requirementType(getRequirementType())
                        .nonconformities(List.of(SurveillanceNonconformity.builder()
                                .id(1L)
                                .capApprovalDay(LocalDate.now())
                                .capMustCompleteDay(LocalDate.now())
                                .dateOfDeterminationDay(LocalDate.now())
                                .summary("Summary")
                                .findings("Findings")
                                .type(NonconformityType.builder()
                                        .id(1L)
                                        .startDay(LocalDate.of(2020, 10, 18))
                                        .endDay(LocalDate.of(2023, 10, 18))
                                        .title(null)
                                        .number(null)
                                        .build())
                                .build()))
                        .build())))
                .build();

        SurveillanceNonconformityReviewer reviewer = new SurveillanceNonconformityReviewer(errorMessageUtil);

        reviewer.review(surveillance);

        assertEquals(0, surveillance.getErrorMessages().size());

    }

    @Test
    public void review_nonConformityCriteriaRelatedWithSurvStartDateBeforeCriteriaStartDateIsNotValid_errorMessagesExist() {
        Surveillance surveillance = Surveillance.builder()
                .id(1L)
                .startDay(LocalDate.of(2019, 10, 18))
                .requirements(new LinkedHashSet<SurveillanceRequirement>(Collections.singleton(SurveillanceRequirement.builder()
                        .id(1L)
                        .result(getNonConformityResultType())
                        .requirementType(getRequirementType())
                        .nonconformities(List.of(SurveillanceNonconformity.builder()
                                .id(1L)
                                .capApprovalDay(LocalDate.now())
                                .capMustCompleteDay(LocalDate.now())
                                .dateOfDeterminationDay(LocalDate.now())
                                .summary("Summary")
                                .findings("Findings")
                                .type(NonconformityType.builder()
                                        .id(1L)
                                        .startDay(LocalDate.of(2020, 10, 18))
                                        .endDay(LocalDate.of(2023, 10, 18))
                                        .title(null)
                                        .number(null)
                                        .build())
                                .build()))
                        .build())))
                .build();

        SurveillanceNonconformityReviewer reviewer = new SurveillanceNonconformityReviewer(errorMessageUtil);

        reviewer.review(surveillance);

        assertEquals(1, surveillance.getErrorMessages().size());
    }

    @Test
    public void review_nonConformityCriteriaRelatedWithSurvStartDateAfterCriteriaEndDateIsNotValid_errorMessagesExist() {
        Surveillance surveillance = Surveillance.builder()
                .id(1L)
                .startDay(LocalDate.of(2024, 10, 18))
                .requirements(new LinkedHashSet<SurveillanceRequirement>(Collections.singleton(SurveillanceRequirement.builder()
                        .id(1L)
                        .result(getNonConformityResultType())
                        .requirementType(getRequirementType())
                        .nonconformities(List.of(SurveillanceNonconformity.builder()
                                .id(1L)
                                .capApprovalDay(LocalDate.now())
                                .capMustCompleteDay(LocalDate.now())
                                .dateOfDeterminationDay(LocalDate.now())
                                .summary("Summary")
                                .findings("Findings")
                                .type(NonconformityType.builder()
                                        .id(1L)
                                        .startDay(LocalDate.of(2020, 10, 18))
                                        .endDay(LocalDate.of(2023, 10, 18))
                                        .title(null)
                                        .number(null)
                                        .build())
                                .build()))
                        .build())))
                .build();

        SurveillanceNonconformityReviewer reviewer = new SurveillanceNonconformityReviewer(errorMessageUtil);

        reviewer.review(surveillance);

        assertEquals(1, surveillance.getErrorMessages().size());
    }

    private SurveillanceResultType getNonConformityResultType() {
        return SurveillanceResultType.builder()
                .id(1L)
                .name("Non-conformity")
                .build();
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

}
