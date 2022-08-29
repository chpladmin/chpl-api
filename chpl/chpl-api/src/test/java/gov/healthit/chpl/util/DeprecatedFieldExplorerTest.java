package gov.healthit.chpl.util;

public class DeprecatedFieldExplorerTest {

    //TODO - TMY - Will we still have this test? - YES (OCD-4029)
    /*
    private DeprecatedFieldExplorer deprecatedFieldExplorer;

    private DeprecatedResponseFieldExplorer deprecatedFieldExplorer;

    @Before
    public void setup() {
        deprecatedFieldExplorer = new DeprecatedResponseFieldExplorer();
    }

    @Test
    public void findDeprecatedFields_CertifiedProductSearchDetails() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CertifiedProductSearchDetails.class);
        assertNotNull(deprecatedItems);
        assertEquals(8, deprecatedItems.keySet().size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("product" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedItemNames.contains("product" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("product" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("version" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "versionId"));
        assertTrue(deprecatedItemNames.contains("surveillance" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "documents"));
        assertTrue(deprecatedItemNames.contains("surveillance" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "requirementName"));
        assertTrue(deprecatedItemNames.contains("surveillance" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "nonconformityTypeName"));
    }

    @Test
    public void findDeprecatedFields_CertifiedProductSearchBasicDetails() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CertifiedProductSearchBasicDetails.class);
        assertNotNull(deprecatedItems);
        assertEquals(8, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("product" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedItemNames.contains("product" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("product" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("version" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "versionId"));
        assertTrue(deprecatedItemNames.contains("surveillance" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "requirementName"));
        assertTrue(deprecatedItemNames.contains("surveillance" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "nonconformityTypeName"));
        assertTrue(deprecatedItemNames.contains("surveillance" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "documents"));
    }

    @Test
    public void findDeprecatedFields_CertificationResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CertificationResults.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_SurveillanceResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(SurveillanceResults.class);

        assertNotNull(deprecatedItems);
        assertEquals(3, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("pendingSurveillance" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "requirementName"));
        assertTrue(deprecatedItemNames.contains("pendingSurveillance" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "nonconformityTypeName"));
        assertTrue(deprecatedItemNames.contains("pendingSurveillance" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "documents"));
    }

    @Test
    public void findDeprecatedFields_Surveillance() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(Surveillance.class);

        assertNotNull(deprecatedItems);
        assertEquals(3, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("requirements" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "requirementName"));
        assertTrue(deprecatedItemNames.contains("requirements" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "nonconformityTypeName"));
        assertTrue(deprecatedItemNames.contains("requirements" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "documents"));
    }

    @Test
    public void findDeprecatedFields_ApiKey() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ApiKey.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_CertificationIdResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CertificationIdResults.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_CertifiedProductFlatSearchResult() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CertifiedProductFlatSearchResult.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_SearchResponse() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(SearchResponse.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ListingSearchResponse() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ListingSearchResponse.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ComplaintResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ComplaintResults.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_Complaint() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(Complaint.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }


    @Test
    public void findDeprecatedFields_QuarterlyReport() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(QuarterlyReport.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_RelevantListing() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(RelevantListing.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_PrivilegedSurveillance() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(PrivilegedSurveillance.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ChplOneTimeTrigger() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ChplOneTimeTrigger.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ScheduleTriggersResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ScheduleTriggersResults.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ScheduleOneTimeTriggersResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ScheduleOneTimeTriggersResults.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ChplJobsResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ChplJobsResults.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_SystemTriggerResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(SystemTriggerResults.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_CertifiedProduct() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CertifiedProduct.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_CqmResultDetails() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CQMResultDetailResults.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_MeasureResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(MeasureResults.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_IcsFamilyTreeNode() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(IcsFamilyTreeNode.class);
        assertNotNull(deprecatedItems);
        assertEquals(5, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("product" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedItemNames.contains("product" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("product" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("version" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "versionId"));
    }

    @Test
    public void findDeprecatedFields_ActivityDetails() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ActivityDetails.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ActivityMetadataPage() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ActivityMetadataPage.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ActivityMetadata() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ActivityMetadata.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_AnnouncementResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(AnnouncementResults.class);

        assertNotNull(deprecatedItems);
        assertEquals(2, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("announcements" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "startDate"));
        assertTrue(deprecatedItemNames.contains("announcements" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "endDate"));
    }

    @Test
    public void findDeprecatedFields_Announcement() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(Announcement.class);

        assertNotNull(deprecatedItems);
        assertEquals(2, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("startDate"));
        assertTrue(deprecatedItemNames.contains("endDate"));
    }

    @Test
    public void findDeprecatedFields_BooleanResult() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(BooleanResult.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_UpdatePasswordResponse() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(UpdatePasswordResponse.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_CertificationBodyResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CertificationBodyResults.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_CertificationBody() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CertificationBody.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_UsersResponse() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(UsersResponse.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_SimpleCertificationId() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(SimpleCertificationId.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_CertificationIdLookupResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CertificationIdLookupResults.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_CertificationIdVerifyResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CertificationIdVerifyResults.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ChangeRequest() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ChangeRequest.class);
        assertNotNull(deprecatedItems);
        assertEquals(1, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
    }

    @Test
    public void findDeprecatedFields_ChangeRequestResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ChangeRequestResults.class);

        assertNotNull(deprecatedItems);
        assertEquals(1, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("results" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
    }

    @Test
    public void findDeprecatedFields_ChplFileDTO() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CHPLFileDTO.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_DecertifiedDeveloper() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(DecertifiedDeveloper.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_DeveloperResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(DeveloperResults.class);
        assertNotNull(deprecatedItems);
        assertEquals(1, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("developers" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
    }

    @Test
    public void findDeprecatedFields_Developer() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(Developer.class);
        assertNotNull(deprecatedItems);
        assertEquals(1, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("developerId"));
    }

    @Test
    public void findDeprecatedFields_DeveloperTree() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(DeveloperTree.class);
        assertNotNull(deprecatedItems);
        assertEquals(5, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("developerId"));
        assertTrue(deprecatedItemNames.contains("products" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedItemNames.contains("products" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("products" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("products" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "versions" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "versionId"));
    }

    @Test
    public void findDeprecatedFields_DirectReview() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(DirectReview.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_PermissionDeletedResponse() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(PermissionDeletedResponse.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_FuzzyChoices() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(FuzzyChoices.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_KeyValueModel() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(KeyValueModel.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_KeyValueModelStatuses() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(KeyValueModelStatuses.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_SearchOption() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(SearchOption.class);
        //this isn't a great check because the SearchOption class has a generic list with
        //anything extending Object so actual classes that are returned here may have deprecated
        //fields that this would not catch
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_OptionalStandard() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(OptionalStandard.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_TestFunctionality() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(TestFunctionality.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_CriteriaSpecificDescriptiveModel() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CriteriaSpecificDescriptiveModel.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_TestStandard() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(TestStandard.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_Measure() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(Measure.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_MeasureType() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(MeasureType.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_SurveillanceRequirementOptions() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(SurveillanceRequirementOptions.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_DimensionalData() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(DimensionalData.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_CertificationCriterionResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CertificationCriterionResults.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_SvapResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(SvapResults.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_Svap() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(Svap.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_FilterResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(FilterResults.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_Filter() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(Filter.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ListingUpload() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ListingUpload.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ListingUploadResponse() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ListingUploadResponse.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ProductResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ProductResults.class);
        assertNotNull(deprecatedItems);
        assertEquals(3, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("products" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedItemNames.contains("products" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("products" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
    }

    @Test
    public void findDeprecatedFields_Product() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(Product.class);
        assertNotNull(deprecatedItems);
        assertEquals(3, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("productId"));
        assertTrue(deprecatedItemNames.contains("owner" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("ownerHistory" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
    }

    @Test
    public void findDeprecatedFields_SplitProductResponse() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(SplitProductResponse.class);
        assertNotNull(deprecatedItems);
        assertEquals(6, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("oldProduct" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedItemNames.contains("oldProduct" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("oldProduct" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("newProduct" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedItemNames.contains("newProduct" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("newProduct" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
    }

    @Test
    public void findDeprecatedFields_ProductVersion() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ProductVersion.class);
        assertNotNull(deprecatedItems);
        assertEquals(1, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("versionId"));
    }

    @Test
    public void findDeprecatedFields_SplitVersionResponse() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(SplitVersionResponse.class);
        assertNotNull(deprecatedItems);
        assertEquals(2, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("oldVersion" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "versionId"));
        assertTrue(deprecatedItemNames.contains("newVersion" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "versionId"));
    }

    @Test
    public void findDeprecatedFields_RealWorldTestingUploadResponse() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(RealWorldTestingUploadResponse.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_NonconformityTypeStatisticsResult() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(NonconformityTypeStatisticsResult.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ListingCountStatisticsResult() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ListingCountStatisticsResult.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_CriterionProductStatisticsResult() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CriterionProductStatisticsResult.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_IncumbentDevelopersStatisticsResult() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(IncumbentDevelopersStatisticsResult.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_SedParticipantStatisticsCountResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(SedParticipantStatisticsCountResults.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ParticipantGenderStatistics() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ParticipantGenderStatistics.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ParticipantAgeStatisticsResult() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ParticipantAgeStatisticsResult.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ParticipantEducationStatisticsResult() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ParticipantEducationStatisticsResult.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_ParticipantExperienceStatisticsResult() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ParticipantExperienceStatisticsResult.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_SystemStatus() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(SystemStatus.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_AnnualReport() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(AnnualReport.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_CertificationCriterion() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(CertificationCriterion.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_TestingLabResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(TestingLabResults.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_TestingLab() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(TestingLab.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_User() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(User.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    @Test
    public void findDeprecatedFields_UserInvitation() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(UserInvitation.class);
        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
    }

    //This test does not work due to the recursive nature of formItem -> childFormItems
    //This test ends up in an endless loop and never finishes
    @Ignore
    @Test
    public void findDeprecatedFields_DeveloperAttestationSubmissionResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(DeveloperAttestationSubmissionResults.class);
        assertNotNull(deprecatedItems);
        assertEquals(2, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("canSubmitAttestationChangeRequest"));
        assertTrue(deprecatedItemNames.contains("developerAttestations" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
    }
    */
}
