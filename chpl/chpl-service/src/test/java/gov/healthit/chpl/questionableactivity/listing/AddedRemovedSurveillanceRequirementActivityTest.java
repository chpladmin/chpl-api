package gov.healthit.chpl.questionableactivity.listing;

import gov.healthit.chpl.manager.DimensionalDataManager;

public class AddedRemovedSurveillanceRequirementActivityTest {
    private DimensionalDataManager dimensionalDataManager;

    private AddedRemovedSurveillanceRequirementActivity activity;

    //TODO  OCD-4029
    /*
    @Before
    public void setup() {
        dimensionalDataManager = Mockito.mock(DimensionalDataManager.class);
        Mockito.when(dimensionalDataManager.getSurveillanceRequirementOptions()).thenReturn(getSurveillanceRequirementOptions());

        activity = new AddedRemovedSurveillanceRequirementActivity(dimensionalDataManager);
    }


    @Test
    public void check_NoNewNonconformitiesAdded_EmptyList() {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .id(1L)
                                .requirement(getSurveillanceRequirementOptions().getCriteriaOptions2015().get(0).getNumber())
                                .build()))
                        .build()))
                .build();
        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .id(1L)
                                .requirement(getSurveillanceRequirementOptions().getCriteriaOptions2015().get(0).getNumber())
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
                                .id(1L)
                                .requirement(getSurveillanceRequirementOptions().getCriteriaOptions2015().get(0).getNumber())
                                .build()))
                        .build()))
                .build();
        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .id(1L)
                                .requirement(getSurveillanceRequirementOptions().getCriteriaOptions2015().get(0).getNumber())
                                .build(),
                                SurveillanceRequirement.builder()
                                .id(2L)
                                .requirement(getSurveillanceRequirementOptions().getCriteriaOptions2015().get(1).getNumber())
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
                                .id(1L)
                                .requirement(getSurveillanceRequirementOptions().getCriteriaOptions2015().get(0).getNumber())
                                .build()))
                        .build()))
                .build();
        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .id(1L)
                                .requirement(getSurveillanceRequirementOptions().getCriteriaOptions2015().get(0).getNumber())
                                .build(),
                                SurveillanceRequirement.builder()
                                .id(2L)
                                .requirement(getSurveillanceRequirementOptions().getCriteriaOptions2015().get(3).getNumber())
                                .build()))
                        .build()))
                .build();

        List<QuestionableActivityListingDTO> dtos = activity.check(origListing, newListing);

        assertEquals(1, dtos.size());
    }

    @Test
    public void check_NewNonconformitiesAsRemovedAdded_ListPopulated() {
        CertifiedProductSearchDetails origListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .id(1L)
                                .requirement(getSurveillanceRequirementOptions().getCriteriaOptions2015().get(0).getNumber())
                                .build()))
                        .build()))
                .build();
        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .id(1L)
                                .requirement(getSurveillanceRequirementOptions().getCriteriaOptions2015().get(0).getNumber())
                                .build(),
                                SurveillanceRequirement.builder()
                                .id(2L)
                                .requirement(RequirementTypeEnum.K2.getName())
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
                                .requirement(getSurveillanceRequirementOptions().getCriteriaOptions2015().get(0).getNumber())
                                .build(),
                                SurveillanceRequirement.builder()
                                .id(2L)
                                .requirement(getSurveillanceRequirementOptions().getCriteriaOptions2015().get(1).getNumber())
                                .build()))
                        .build()))
                .build();
        CertifiedProductSearchDetails newListing = CertifiedProductSearchDetails.builder()
                .surveillance(Arrays.asList(Surveillance.builder()
                        .requirements(Sets.newHashSet(SurveillanceRequirement.builder()
                                .id(1L)
                                .requirement(getSurveillanceRequirementOptions().getCriteriaOptions2015().get(0).getNumber())
                                .build(),
                                SurveillanceRequirement.builder()
                                .id(2L)
                                .requirement(RequirementTypeEnum.K2.getName())
                                .build()))
                        .build()))
                .build();

        List<QuestionableActivityListingDTO> dtos = activity.check(origListing, newListing);

        assertEquals(1, dtos.size());
    }


    private SurveillanceRequirementOptions getSurveillanceRequirementOptions() {
        return SurveillanceRequirementOptions.builder()
                .criteriaOption2015(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .title("Computerized Provider Order Entry (CPOE) - Medications")
                        .removed(false)
                        .build())
                .criteriaOption2015(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(2)")
                        .title("CPOE - Laboratory")
                        .removed(false)
                        .build())
                .criteriaOption2015(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(3)")
                        .title("CPOE - Diagnostic Imaging")
                        .removed(false)
                        .build())
                .criteriaOption2015(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(6)")
                        .title("Problem List")
                        .removed(true)
                        .build())
                .transparencyOption(Removable.<String>builder()
                        .item(RequirementTypeEnum.K1.getName())
                        .removed(RequirementTypeEnum.K1.getRemoved())
                        .build())
                .transparencyOption(Removable.<String>builder()
                        .item(RequirementTypeEnum.K2.getName())
                        .removed(RequirementTypeEnum.K2.getRemoved())
                        .build())
                .realWorldTestingOption(Removable.<String>builder()
                        .item(RequirementTypeEnum.ANNUAL_RWT_PLAN.getName())
                        .removed(RequirementTypeEnum.ANNUAL_RWT_PLAN.getRemoved())
                        .build())
                .realWorldTestingOption(Removable.<String>builder()
                        .item(RequirementTypeEnum.ANNUAL_RWT_RESULTS.getName())
                        .removed(RequirementTypeEnum.ANNUAL_RWT_RESULTS.getRemoved())
                        .build())
                .build();
    }
    */
}
