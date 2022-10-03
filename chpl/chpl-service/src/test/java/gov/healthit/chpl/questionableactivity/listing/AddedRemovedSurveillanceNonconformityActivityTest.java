package gov.healthit.chpl.questionableactivity.listing;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.compress.utils.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.concept.RequirementTypeEnum;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.service.CertificationCriterionService;

public class AddedRemovedSurveillanceNonconformityActivityTest {

    private CertificationCriterionService certificationCriterionService;

    private AddedRemovedSurveillanceNonconformityActivity activity;

    @Before
    public void setup() {
        certificationCriterionService = Mockito.mock(CertificationCriterionService.class);

        activity = new AddedRemovedSurveillanceNonconformityActivity(certificationCriterionService);
    }

    @Test
    public void check_NoNewNonconformitiesAdded_EmptyList() {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .nonconformities(Arrays.asList(SurveillanceNonconformity.builder()
                                        .id(1L)
                                        .type(NonconformityType.builder()
                                                .id(1L)
                                                .removed(false)
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build();
        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .nonconformities(Arrays.asList(SurveillanceNonconformity.builder()
                                        .id(1L)
                                        .type(NonconformityType.builder()
                                                .id(1L)
                                                .removed(false)
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build();

        List<QuestionableActivityListingDTO> dtos = activity.check(origListing, newListing);

        assertEquals(0, dtos.size());
    }

    @Test
    public void check_NewNonconformitiesNotRemovedAdded_EmptyList() {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .requirement("170.315(a)(1)")
                                .nonconformities(Arrays.asList(SurveillanceNonconformity.builder()
                                        .id(1L)
                                        .type(NonconformityType.builder()
                                                .id(1L)
                                                .removed(false)
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build();
        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .requirement("170.315(a)(1)")
                                .nonconformities(Arrays.asList(SurveillanceNonconformity.builder()
                                        .id(1L)
                                        .type(NonconformityType.builder()
                                                .id(1L)
                                                .removed(false)
                                                .build())
                                        .build()))
                                .build(),
                                SurveillanceRequirement.builder()
                                .requirement("170.315(a)(2)")
                                .nonconformities(Arrays.asList(SurveillanceNonconformity.builder()
                                        .id(2L)
                                        .type(NonconformityType.builder()
                                                .id(2L)
                                                .removed(false)
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build();

        List<QuestionableActivityListingDTO> dtos = activity.check(origListing, newListing);

        assertEquals(0, dtos.size());
    }

    @Test
    public void check_NewNonconformitiesAsRemovedCriteriaAdded_ListPopulated() {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .requirement("170.315(a)(1)")
                                .nonconformities(Arrays.asList(SurveillanceNonconformity.builder()
                                        .id(1L)
                                        .type(NonconformityType.builder()
                                                .id(1L)
                                                .removed(false)
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build();
        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .requirement("170.315(a)(1)")
                                .nonconformities(Arrays.asList(SurveillanceNonconformity.builder()
                                        .id(1L)
                                        .type(NonconformityType.builder()
                                                .id(1L)
                                                .removed(false)
                                                .build())
                                        .build()))
                                .build(),
                                SurveillanceRequirement.builder()
                                .requirement("170.315(a)(6)")
                                .nonconformities(Arrays.asList(SurveillanceNonconformity.builder()
                                        .id(2L)
                                        .type(NonconformityType.builder()
                                                .id(2L)
                                                .removed(true)
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build();

        List<QuestionableActivityListingDTO> dtos = activity.check(origListing, newListing);

        assertEquals(1, dtos.size());
    }

    @Test
    public void check_NonconformityChangedToRemovedNonconformity_ListPopulated() {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .requirement("170.315(a)(1)")
                                .nonconformities(Arrays.asList(SurveillanceNonconformity.builder()
                                        .id(1L)
                                        .type(NonconformityType.builder()
                                                .id(1L)
                                                .removed(false)
                                                .build())
                                        .build()))
                                .build(),
                                SurveillanceRequirement.builder()
                                .requirement("170.315(a)(6)")
                                .nonconformities(Arrays.asList(SurveillanceNonconformity.builder()
                                        .id(2L)
                                        .type(NonconformityType.builder()
                                                .id(2L)
                                                .removed(false)
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build();
        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .requirement("170.315(a)(1)")
                                .nonconformities(Arrays.asList(SurveillanceNonconformity.builder()
                                        .id(1L)
                                        .type(NonconformityType.builder()
                                                .id(1L)
                                                .removed(false)
                                                .build())
                                        .build()))
                                .build(),
                                SurveillanceRequirement.builder()
                                .requirement(RequirementTypeEnum.K2.getName())
                                .nonconformities(Arrays.asList(SurveillanceNonconformity.builder()
                                        .id(2L)
                                        .type(NonconformityType.builder()
                                                .id(3L)
                                                .removed(true)
                                                .build())
                                        .build()))
                                .build()
                                ))
                        .build()))
                .build();

        List<QuestionableActivityListingDTO> dtos = activity.check(origListing, newListing);

        assertEquals(1, dtos.size());
    }
}
