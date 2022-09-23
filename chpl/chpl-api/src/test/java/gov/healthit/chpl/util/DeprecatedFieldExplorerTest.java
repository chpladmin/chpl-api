package gov.healthit.chpl.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.classmate.Filter;

import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResponse;
import gov.healthit.chpl.domain.Announcement;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchBasicDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CriteriaSpecificDescriptiveModel;
import gov.healthit.chpl.domain.DecertifiedDeveloper;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DimensionalData;
import gov.healthit.chpl.domain.FuzzyChoices;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.domain.ParticipantGenderStatistics;
import gov.healthit.chpl.domain.PermissionDeletedResponse;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.SearchOption;
import gov.healthit.chpl.domain.SimpleCertificationId;
import gov.healthit.chpl.domain.TestFunctionality;
import gov.healthit.chpl.domain.TestStandard;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.domain.activity.ActivityDetails;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.domain.activity.ActivityMetadataPage;
import gov.healthit.chpl.domain.auth.UpdatePasswordResponse;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.auth.UserInvitation;
import gov.healthit.chpl.domain.auth.UsersResponse;
import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.developer.hierarchy.DeveloperTree;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.domain.status.SystemStatus;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementOptions;
import gov.healthit.chpl.dto.CHPLFileDTO;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUploadResponse;
import gov.healthit.chpl.search.domain.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResponse;
import gov.healthit.chpl.search.domain.SearchResponse;
import gov.healthit.chpl.surveillance.report.domain.AnnualReport;
import gov.healthit.chpl.surveillance.report.domain.PrivilegedSurveillance;
import gov.healthit.chpl.surveillance.report.domain.QuarterlyReport;
import gov.healthit.chpl.surveillance.report.domain.RelevantListing;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.web.controller.results.AnnouncementResults;
import gov.healthit.chpl.web.controller.results.BooleanResult;
import gov.healthit.chpl.web.controller.results.CQMResultDetailResults;
import gov.healthit.chpl.web.controller.results.CertificationBodyResults;
import gov.healthit.chpl.web.controller.results.CertificationCriterionResults;
import gov.healthit.chpl.web.controller.results.CertificationIdLookupResults;
import gov.healthit.chpl.web.controller.results.CertificationIdResults;
import gov.healthit.chpl.web.controller.results.CertificationIdVerifyResults;
import gov.healthit.chpl.web.controller.results.CertificationResults;
import gov.healthit.chpl.web.controller.results.ChangeRequestResults;
import gov.healthit.chpl.web.controller.results.ChplJobsResults;
import gov.healthit.chpl.web.controller.results.ComplaintResults;
import gov.healthit.chpl.web.controller.results.CriterionProductStatisticsResult;
import gov.healthit.chpl.web.controller.results.DeveloperAttestationSubmissionResults;
import gov.healthit.chpl.web.controller.results.DeveloperResults;
import gov.healthit.chpl.web.controller.results.FilterResults;
import gov.healthit.chpl.web.controller.results.IncumbentDevelopersStatisticsResult;
import gov.healthit.chpl.web.controller.results.ListingCountStatisticsResult;
import gov.healthit.chpl.web.controller.results.ListingUploadResponse;
import gov.healthit.chpl.web.controller.results.MeasureResults;
import gov.healthit.chpl.web.controller.results.NonconformityTypeStatisticsResult;
import gov.healthit.chpl.web.controller.results.ParticipantAgeStatisticsResult;
import gov.healthit.chpl.web.controller.results.ParticipantEducationStatisticsResult;
import gov.healthit.chpl.web.controller.results.ParticipantExperienceStatisticsResult;
import gov.healthit.chpl.web.controller.results.ProductResults;
import gov.healthit.chpl.web.controller.results.ScheduleOneTimeTriggersResults;
import gov.healthit.chpl.web.controller.results.ScheduleTriggersResults;
import gov.healthit.chpl.web.controller.results.SedParticipantStatisticsCountResults;
import gov.healthit.chpl.web.controller.results.SplitProductResponse;
import gov.healthit.chpl.web.controller.results.SplitVersionResponse;
import gov.healthit.chpl.web.controller.results.SurveillanceResults;
import gov.healthit.chpl.web.controller.results.SvapResults;
import gov.healthit.chpl.web.controller.results.SystemTriggerResults;
import gov.healthit.chpl.web.controller.results.TestingLabResults;


public class DeprecatedFieldExplorerTest {

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
        assertEquals(4, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("submittedDate"));
        assertTrue(deprecatedItemNames.contains("currentStatus" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "statusChangeDate"));
        assertTrue(deprecatedItemNames.contains("statuses" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "statusChangeDate"));
    }

    @Test
    public void findDeprecatedFields_ChangeRequestResults() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ChangeRequestResults.class);

        assertNotNull(deprecatedItems);
        assertEquals(4, deprecatedItems.size());
        Set<String> deprecatedItemNames = deprecatedItems.keySet();
        assertTrue(deprecatedItemNames.contains("results" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developer" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedItemNames.contains("results" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "submittedDate"));
        assertTrue(deprecatedItemNames.contains("results" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "currentStatus" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "statusChangeDate"));
        assertTrue(deprecatedItemNames.contains("results" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "statuses" + DeprecatedResponseFieldExplorer.FIELD_SEPARATOR + "statusChangeDate"));
    }

    @Test
    public void findDeprecatedFields_ChangeRequestSearchResponse() {
        Map<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(ChangeRequestSearchResponse.class);

        assertNotNull(deprecatedItems);
        assertEquals(0, deprecatedItems.size());
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
}
