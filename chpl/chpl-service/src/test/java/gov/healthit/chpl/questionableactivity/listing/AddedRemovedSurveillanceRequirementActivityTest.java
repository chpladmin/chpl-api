package gov.healthit.chpl.questionableactivity.listing;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.compress.utils.Sets;
import org.junit.Before;
import org.junit.Test;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.RequirementDetailType;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;

public class AddedRemovedSurveillanceRequirementActivityTest {
    private AddedRemovedSurveillanceRequirementActivity activity;

    @Before
    public void setup() {
        activity = new AddedRemovedSurveillanceRequirementActivity();
    }


    @Test
    public void check_NoNewRequirementsAdded_EmptyList() {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .id(1L)
                                .requirementDetailType(RequirementDetailType.builder()
                                        .id(1L)
                                        .removed(false)
                                        .build())
                                .build()))
                        .build()))
                .build();
        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .id(1L)
                                .requirementDetailType(RequirementDetailType.builder()
                                        .id(1L)
                                        .removed(false)
                                        .build())
                                .build()))
                        .build()))
                .build();

        List<QuestionableActivityListingDTO> dtos = activity.check(origListing, newListing);

        assertEquals(0, dtos.size());
    }

    @Test
    public void check_NewRequirementsNotRemovedAdded_EmptyList() {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .id(1L)
                                .requirementDetailType(RequirementDetailType.builder()
                                        .id(1L)
                                        .removed(false)
                                        .build())
                                .build()))
                        .build()))
                .build();
        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .id(1L)
                                .requirementDetailType(RequirementDetailType.builder()
                                        .id(1L)
                                        .removed(false)
                                        .build())
                                .build(),
                                SurveillanceRequirement.builder()
                                .id(2L)
                                .requirementDetailType(RequirementDetailType.builder()
                                        .id(2L)
                                        .removed(false)
                                        .build())
                                .build()))
                        .build()))
                .build();

        List<QuestionableActivityListingDTO> dtos = activity.check(origListing, newListing);

        assertEquals(0, dtos.size());
    }

    @Test
    public void check_NewRequirementsAsRemovedRequirementDetailsTypeAdded_ListPopulated() {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .id(1L)
                                .requirementDetailType(RequirementDetailType.builder()
                                        .id(1L)
                                        .removed(false)
                                        .build())

                                .build()))
                        .build()))
                .build();
        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .id(1L)
                                .requirementDetailType(RequirementDetailType.builder()
                                        .id(1L)
                                        .removed(false)
                                        .build())

                                .build(),
                                SurveillanceRequirement.builder()
                                .id(2L)
                                .requirementDetailType(RequirementDetailType.builder()
                                        .id(2L)
                                        .removed(true)
                                        .build())

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
                                .id(1L)
                                .requirementDetailType(RequirementDetailType.builder()
                                        .id(1L)
                                        .removed(false)
                                        .build())

                                .build(),
                                SurveillanceRequirement.builder()
                                .id(2L)
                                .requirementDetailType(RequirementDetailType.builder()
                                        .id(2L)
                                        .removed(false)
                                        .build())

                                .build()))
                        .build()))
                .build();
        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .id(1L)
                                .requirementDetailType(RequirementDetailType.builder()
                                        .id(1L)
                                        .removed(false)
                                        .build())

                                .build(),
                                SurveillanceRequirement.builder()
                                .id(2L)
                                .requirementDetailType(RequirementDetailType.builder()
                                        .id(3L)
                                        .removed(true)
                                        .build())

                                .build()))
                        .build()))
                .build();

        List<QuestionableActivityListingDTO> dtos = activity.check(origListing, newListing);

        assertEquals(1, dtos.size());
    }
}