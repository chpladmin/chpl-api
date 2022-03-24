package gov.healthit.chpl.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.classmate.Filter;

import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
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
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.domain.PendingCertifiedProductMetadata;
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
import gov.healthit.chpl.web.controller.results.PendingCertifiedProductResults;
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

    private DeprecatedFieldExplorer deprecatedFieldExplorer;

    @Before
    public void setup() {
        deprecatedFieldExplorer = new DeprecatedFieldExplorer();
    }

    @Test
    public void findDeprecatedFields_CertifiedProductSearchDetails() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertifiedProductSearchDetails.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(17, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("transparencyAttestationUrl"));
        assertTrue(deprecatedFieldNames.contains("meaningfulUseUserHistory"));
        assertTrue(deprecatedFieldNames.contains("currentMeaningfulUseUsers"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "authority"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "startDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "endDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "status"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "dateOfDetermination"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capApprovalDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capStartDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capEndDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capMustCompleteDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformityCloseDate"));
    }

    @Test
    public void findDeprecatedFields_CertifiedProductSearchBasicDetails() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertifiedProductSearchBasicDetails.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(17, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("transparencyAttestationUrl"));
        assertTrue(deprecatedFieldNames.contains("meaningfulUseUserHistory"));
        assertTrue(deprecatedFieldNames.contains("currentMeaningfulUseUsers"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "authority"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "startDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "endDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "status"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "dateOfDetermination"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capApprovalDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capStartDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capEndDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capMustCompleteDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformityCloseDate"));
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
        assertEquals(17, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("transparencyAttestationUrl"));
        assertTrue(deprecatedFieldNames.contains("meaningfulUseUserHistory"));
        assertTrue(deprecatedFieldNames.contains("currentMeaningfulUseUsers"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "authority"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "startDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "endDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "status"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "dateOfDetermination"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capApprovalDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capStartDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capEndDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capMustCompleteDate"));
        assertTrue(deprecatedFieldNames.contains("surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformityCloseDate"));
    }

    @Test
    public void findDeprecatedFields_PendingCertifiedProductResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(PendingCertifiedProductResults.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(17, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "transparencyAttestationUrl"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "meaningfulUseUserHistory"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "currentMeaningfulUseUsers"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "authority"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "startDate"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "endDate"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "status"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "dateOfDetermination"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capApprovalDate"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capStartDate"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capEndDate"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capMustCompleteDate"));
        assertTrue(deprecatedFieldNames.contains("pendingCertifiedProducts" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformityCloseDate"));
    }

    @Test
    public void findDeprecatedFields_SurveillanceResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(SurveillanceResults.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(10, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("pendingSurveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "authority"));
        assertTrue(deprecatedFieldNames.contains("pendingSurveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "startDate"));
        assertTrue(deprecatedFieldNames.contains("pendingSurveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "endDate"));
        assertTrue(deprecatedFieldNames.contains("pendingSurveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "status"));
        assertTrue(deprecatedFieldNames.contains("pendingSurveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "dateOfDetermination"));
        assertTrue(deprecatedFieldNames.contains("pendingSurveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capApprovalDate"));
        assertTrue(deprecatedFieldNames.contains("pendingSurveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capStartDate"));
        assertTrue(deprecatedFieldNames.contains("pendingSurveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capEndDate"));
        assertTrue(deprecatedFieldNames.contains("pendingSurveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capMustCompleteDate"));
        assertTrue(deprecatedFieldNames.contains("pendingSurveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformityCloseDate"));
    }

    @Test
    public void findDeprecatedFields_Surveillance() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(Surveillance.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(10, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("authority"));
        assertTrue(deprecatedFieldNames.contains("startDate"));
        assertTrue(deprecatedFieldNames.contains("endDate"));
        assertTrue(deprecatedFieldNames.contains("requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "status"));
        assertTrue(deprecatedFieldNames.contains("requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "dateOfDetermination"));
        assertTrue(deprecatedFieldNames.contains("requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capApprovalDate"));
        assertTrue(deprecatedFieldNames.contains("requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capStartDate"));
        assertTrue(deprecatedFieldNames.contains("requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capEndDate"));
        assertTrue(deprecatedFieldNames.contains("requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "capMustCompleteDate"));
        assertTrue(deprecatedFieldNames.contains("requirements" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformities" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "nonconformityCloseDate"));
    }

    @Test
    public void findDeprecatedFields_ApiKey() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ApiKey.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(1, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("apiKey"));
    }

    @Test
    public void findDeprecatedFields_CertificationIdResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertificationIdResults.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(1, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("isValid"));
    }

    @Test
    public void findDeprecatedFields_CertifiedProductFlatSearchResult() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(CertifiedProductFlatSearchResult.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(3, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("numMeaningfulUse"));
        assertTrue(deprecatedFieldNames.contains("numMeaningfulUseDate"));
        assertTrue(deprecatedFieldNames.contains("transparencyAttestationUrl"));
    }

    @Test
    public void findDeprecatedFields_SearchResponse() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(SearchResponse.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(3, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("results" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "numMeaningfulUse"));
        assertTrue(deprecatedFieldNames.contains("results" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "numMeaningfulUseDate"));
        assertTrue(deprecatedFieldNames.contains("results" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "transparencyAttestationUrl"));
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
        assertEquals(3, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("results" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillances" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "userPermissionId"));
        assertTrue(deprecatedFieldNames.contains("results" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillances" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "startDate"));
        assertTrue(deprecatedFieldNames.contains("results" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillances" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "endDate"));
    }

    @Test
    public void findDeprecatedFields_Complaint() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(Complaint.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(3, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("surveillances" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "userPermissionId"));
        assertTrue(deprecatedFieldNames.contains("surveillances" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "startDate"));
        assertTrue(deprecatedFieldNames.contains("surveillances" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "surveillance" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "endDate"));
    }


    @Test
    public void findDeprecatedFields_QuarterlyReport() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(QuarterlyReport.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(2, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("reactiveSummary"));
        assertTrue(deprecatedFieldNames.contains("transparencyDisclosureSummary"));
    }

    @Test
    public void findDeprecatedFields_RelevantListing() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(RelevantListing.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(3, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("surveillances" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "userPermissionId"));
        assertTrue(deprecatedFieldNames.contains("surveillances" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "startDate"));
        assertTrue(deprecatedFieldNames.contains("surveillances" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "endDate"));
    }

    @Test
    public void findDeprecatedFields_PrivilegedSurveillance() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(PrivilegedSurveillance.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(3, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("userPermissionId"));
        assertTrue(deprecatedFieldNames.contains("startDate"));
        assertTrue(deprecatedFieldNames.contains("endDate"));
    }

    @Test
    public void findDeprecatedFields_ChplOneTimeTrigger() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ChplOneTimeTrigger.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(1, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("job" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "frequency"));
    }

    @Test
    public void findDeprecatedFields_ScheduleTriggersResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ScheduleTriggersResults.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(1, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("results" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "job" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "frequency"));
    }

    @Test
    public void findDeprecatedFields_ScheduleOneTimeTriggersResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ScheduleOneTimeTriggersResults.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(1, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("results" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "job" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "frequency"));
    }

    @Test
    public void findDeprecatedFields_ChplJobsResults() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(ChplJobsResults.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(1, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("results" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "frequency"));
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
        assertEquals(4, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
    }

    @Test
    public void findDeprecatedFields_PendingCertifiedProductMetadata() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(PendingCertifiedProductMetadata.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(4, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("product" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
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
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_Announcement() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(Announcement.class);

        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
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
        assertEquals(4, deprecatedFieldNames.size());
        assertTrue(deprecatedFieldNames.contains("developerId"));
        assertTrue(deprecatedFieldNames.contains("products" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "productId"));
        assertTrue(deprecatedFieldNames.contains("products" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "owner" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
        assertTrue(deprecatedFieldNames.contains("products" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "ownerHistory" + DeprecatedFieldExplorer.FIELD_SEPARATOR  + "developer" + DeprecatedFieldExplorer.FIELD_SEPARATOR + "developerId"));
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
        assertEquals(0, deprecatedFieldNames.size());
    }

    @Test
    public void findDeprecatedFields_SplitVersionResponse() {
        Set<String> deprecatedFieldNames = deprecatedFieldExplorer.getDeprecatedFieldsForClass(SplitVersionResponse.class);
        assertNotNull(deprecatedFieldNames);
        assertEquals(0, deprecatedFieldNames.size());
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
}
