package gov.healthit.chpl.util;

public class DeprecatedFieldExplorerTest {
    //TODO - TMY - Will we still have this test?  (OCD-4029)
    /*
    private DeprecatedFieldExplorer deprecatedFieldExplorer;

    @Before
    public void setup() {
        deprecatedFieldExplorer = new DeprecatedFieldExplorer();
    }

    @Test
    public void findDeprecatedFields_CertifiedProductSearchDetails() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertifiedProductSearchDetails.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(8, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("version" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "versionId"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirementName"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformityTypeName"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "documents"));
    }

    @Test
    public void findDeprecatedFields_CertifiedProductSearchBasicDetails() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertifiedProductSearchBasicDetails.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(8, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("version" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "versionId"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirementName"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformityTypeName"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "documents"));
    }

    @Test
    public void findDeprecatedFields_CertificationResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertificationResults.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_PendingCertifiedProductDetails() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(PendingCertifiedProductDetails.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(8, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("version" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "versionId"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirementName"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformityTypeName"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "documents"));
    }

    @Test
    public void findDeprecatedFields_PendingCertifiedProductResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(PendingCertifiedProductResults.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(8, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "version" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "versionId"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirementName"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformityTypeName"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "documents"));
    }

    @Test
    public void findDeprecatedFields_SurveillanceResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(SurveillanceResults.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(3, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("pendingSurveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirementName"));
        assertTrue(deprecatedFieldNames.contains("pendingSurveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformityTypeName"));
        assertTrue(deprecatedFieldNames.contains("pendingSurveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "documents"));
    }

    @Test
    public void findDeprecatedFields_Surveillance() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(Surveillance.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(3, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirementName"));
        assertTrue(deprecatedFieldNames.contains("requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformityTypeName"));
        assertTrue(deprecatedFieldNames.contains("requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "documents"));
    }

    @Test
    public void findDeprecatedFields_ApiKey() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ApiKey.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_CertificationIdResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertificationIdResults.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_CertifiedProductFlatSearchResult() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertifiedProductFlatSearchResult.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_SearchResponse() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(SearchResponse.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ListingSearchResponse() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ListingSearchResponse.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ComplaintResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ComplaintResults.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_Complaint() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(Complaint.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }


    @Test
    public void findDeprecatedFields_QuarterlyReport() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(QuarterlyReport.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_RelevantListing() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(RelevantListing.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_PrivilegedSurveillance() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(PrivilegedSurveillance.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ChplOneTimeTrigger() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ChplOneTimeTrigger.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ScheduleTriggersResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ScheduleTriggersResults.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ScheduleOneTimeTriggersResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ScheduleOneTimeTriggersResults.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ChplJobsResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ChplJobsResults.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_SystemTriggerResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(SystemTriggerResults.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_CertifiedProduct() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertifiedProduct.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_CqmResultDetails() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CQMResultDetailResults.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_MeasureResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(MeasureResults.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_IcsFamilyTreeNode() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(IcsFamilyTreeNode.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(5, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("version" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "versionId"));
    }

    @Test
    public void findDeprecatedFields_PendingCertifiedProductMetadata() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(PendingCertifiedProductMetadata.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(5, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("version" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "versionId"));
    }

    @Test
    public void findDeprecatedFields_ActivityDetails() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ActivityDetails.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ActivityMetadataPage() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ActivityMetadataPage.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ActivityMetadata() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ActivityMetadata.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_AnnouncementResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(AnnouncementResults.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(2, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("announcements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "startDate"));
        assertTrue(deprecatedFieldNames.contains("announcements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "endDate"));
    }

    @Test
    public void findDeprecatedFields_Announcement() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(Announcement.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(2, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("startDate"));
        assertTrue(deprecatedFieldNames.contains("endDate"));
    }

    @Test
    public void findDeprecatedFields_BooleanResult() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(BooleanResult.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_UpdatePasswordResponse() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(UpdatePasswordResponse.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_CertificationBodyResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertificationBodyResults.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_CertificationBody() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertificationBody.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_UsersResponse() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(UsersResponse.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_SimpleCertificationId() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(SimpleCertificationId.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_CertificationIdLookupResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertificationIdLookupResults.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_CertificationIdVerifyResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertificationIdVerifyResults.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ChangeRequest() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ChangeRequest.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(1, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
    }

    @Test
    public void findDeprecatedFields_ChangeRequestResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ChangeRequestResults.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(1, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("results" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
    }

    @Test
    public void findDeprecatedFields_ChplFileDTO() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CHPLFileDTO.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_DecertifiedDeveloper() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(DecertifiedDeveloper.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_DeveloperResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(DeveloperResults.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(1, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("developers" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
    }

    @Test
    public void findDeprecatedFields_Developer() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(Developer.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(1, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("developerId"));
    }

    @Test
    public void findDeprecatedFields_DeveloperTree() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(DeveloperTree.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(5, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("developerId"));
        assertTrue(deprecatedFieldNames.contains("products" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("products" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("products" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("products" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "versions" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "versionId"));
    }

    @Test
    public void findDeprecatedFields_DirectReview() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(DirectReview.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_PermissionDeletedResponse() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(PermissionDeletedResponse.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_FuzzyChoices() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(FuzzyChoices.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_KeyValueModel() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(KeyValueModel.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_KeyValueModelStatuses() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(KeyValueModelStatuses.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_SearchOption() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(SearchOption.class);
        //this isn't a great check because the SearchOption class has a generic list with
        //anything extending Object so actual classes that are returned here may have deprecated
        //fields that this would not catch
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_OptionalStandard() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(OptionalStandard.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_TestFunctionality() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(TestFunctionality.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_CriteriaSpecificDescriptiveModel() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CriteriaSpecificDescriptiveModel.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_TestStandard() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(TestStandard.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_Measure() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(Measure.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_MeasureType() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(MeasureType.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_SurveillanceRequirementOptions() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(SurveillanceRequirementOptions.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_DimensionalData() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(DimensionalData.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_CertificationCriterionResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertificationCriterionResults.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_SvapResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(SvapResults.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_Svap() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(Svap.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_FilterResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(FilterResults.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_Filter() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(Filter.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ListingUpload() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ListingUpload.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ListingUploadResponse() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ListingUploadResponse.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ProductResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ProductResults.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(3, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("products" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("products" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("products" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
    }

    @Test
    public void findDeprecatedFields_Product() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(Product.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(3, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("productId"));
        assertTrue(deprecatedFieldNames.contains("owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
    }

    @Test
    public void findDeprecatedFields_SplitProductResponse() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(SplitProductResponse.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(6, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("oldProduct" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("oldProduct" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("oldProduct" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("newProduct" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("newProduct" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("newProduct" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
    }

    @Test
    public void findDeprecatedFields_ProductVersion() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ProductVersion.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(1, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("versionId"));
    }

    @Test
    public void findDeprecatedFields_SplitVersionResponse() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(SplitVersionResponse.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(2, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("oldVersion" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "versionId"));
        assertTrue(deprecatedFieldNames.contains("newVersion" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "versionId"));
    }

    @Test
    public void findDeprecatedFields_RealWorldTestingUploadResponse() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(RealWorldTestingUploadResponse.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_NonconformityTypeStatisticsResult() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(NonconformityTypeStatisticsResult.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ListingCountStatisticsResult() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ListingCountStatisticsResult.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_CriterionProductStatisticsResult() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CriterionProductStatisticsResult.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_IncumbentDevelopersStatisticsResult() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(IncumbentDevelopersStatisticsResult.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_SedParticipantStatisticsCountResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(SedParticipantStatisticsCountResults.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ParticipantGenderStatistics() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ParticipantGenderStatistics.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ParticipantAgeStatisticsResult() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ParticipantAgeStatisticsResult.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ParticipantEducationStatisticsResult() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ParticipantEducationStatisticsResult.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_ParticipantExperienceStatisticsResult() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ParticipantExperienceStatisticsResult.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_SystemStatus() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(SystemStatus.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_AnnualReport() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(AnnualReport.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_CertificationCriterion() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertificationCriterion.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_TestingLabResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(TestingLabResults.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_TestingLab() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(TestingLab.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_User() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(User.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_UserInvitation() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(UserInvitation.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
    }

    //This test does not work due to the recursive nature of formItem -> childFormItems
    //This test ends up in an endless loop and never finishes
    @Ignore
    @Test
    public void findDeprecatedFields_DeveloperAttestationSubmissionResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(DeveloperAttestationSubmissionResults.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(2, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("canSubmitAttestationChangeRequest"));
        assertTrue(deprecatedFieldNames.contains("developerAttestations" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
    }
    */
}
