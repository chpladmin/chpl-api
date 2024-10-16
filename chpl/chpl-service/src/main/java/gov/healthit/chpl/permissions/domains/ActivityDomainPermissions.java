package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.activity.GetAcbActivityMetadataActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetActivityDetailsActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetActivityMetadataByAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetActivityMetadataByAtlActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetActivityMetadataByConceptActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetAnnualReportActivityMetadataActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetApiKeyManagementActivityMetadataActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetAtlActivityMetadataActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetChangeRequestActivityMetadataActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetComplaintActivityMetadataActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetFunctionalityTestedMetadataActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetPendingSurveillanceMetadataActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetQuarterlyReportActivityMetadataActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetStandardMetadataActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetSvapMetadataActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetUserMaintenanceMetadataActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.SearchActivityActionPermissions;

@Component
public class ActivityDomainPermissions extends DomainPermissions {
    public static final String GET_ACB_METADATA = "GET_ACB_METADATA";
    public static final String GET_METADATA_BY_ACB = "GET_METADATA_BY_ACB";
    public static final String GET_ATL_METADATA = "GET_ATL_METADATA";
    public static final String GET_METADATA_BY_ATL = "GET_METADATA_BY_ATL";
    public static final String GET_USER_ACTIVITY = "GET_USER_ACTIVITY";
    public static final String GET_ACTIVITY_DETAILS = "GET_ACTIVITY_DETAILS";
    public static final String GET_USER_MAINTENANCE_METADATA = "GET_USER_MAINTENANCE_METADATA";
    public static final String GET_ACTIVITY_METADATA_BY_CONCEPT = "GET_ACTIVITY_METADATA_BY_CONCEPT";
    public static final String GET_PENDING_SURVEILLANCE_METADATA = "GET_PENDING_SURVEILLANCE_METADATA";
    public static final String GET_COMPLAINT_METADATA = "GET_COMPLAINT_METADATA";
    public static final String GET_QUARTERLY_REPORT_METADATA = "GET_QUARTERLY_REPORT_METADATA";
    public static final String GET_ANNUAL_REPORT_METADATA = "GET_ANNUAL_REPORT_METADATA";
    public static final String GET_CHANGE_REQUEST_METADATA = "GET_CHANGE_REQUEST_METADATA";
    public static final String GET_API_KEY_MANAGEMENT_METADATA = "GET_API_KEY_MANAGEMENT_METADATA";
    public static final String GET_FUNCTIONALITY_TESTED_METADATA = "GET_FUNCTIONALITY_TESTED_METADATA";
    public static final String GET_STANDARD_METADATA = "GET_STANDARD_METADATA";
    public static final String GET_SVAP_METADATA = "GET_SVAP_METADATA";
    public static final String SEARCH = "SEARCH";


    @Autowired
    public ActivityDomainPermissions(
            @Qualifier("actionGetAcbActivityMetadataActionPermissions") GetAcbActivityMetadataActionPermissions getAcbActivityMetadataActionPermissions,
            @Qualifier("actionGetActivityMetadataByAcbActionPermissions") GetActivityMetadataByAcbActionPermissions getActivityMetadataByAcbActionPermissions,
            @Qualifier("actionGetAtlActivityMetadataActionPermissions") GetAtlActivityMetadataActionPermissions getAtlActivityMetadataActionPermissions,
            @Qualifier("actionGetActivityMetadataByAtlActionPermissions") GetActivityMetadataByAtlActionPermissions getActivityMetadataByAtlActionPermissions,
            @Qualifier("actionGetActivityDetailsActionPermissions") GetActivityDetailsActionPermissions getActivityDetails,
            @Qualifier("activityGetUserMaintenanceMetadataActionPermissions") GetUserMaintenanceMetadataActionPermissions getUserMaintenanceMetadataActionPermissions,
            @Qualifier("activityGetActivityMetadataByConceptActionPermissions") GetActivityMetadataByConceptActionPermissions getActivityMetadataByConceptActionPermissions,
            @Qualifier("activityGetPendingSurveillanceMetadataActionPermissions") GetPendingSurveillanceMetadataActionPermissions getPendingSurveillanceMetadataActionPermissions,
            @Qualifier("activityGetComplaintActivityMetadataActionPermissions") GetComplaintActivityMetadataActionPermissions getComplaintActivityMetadataActionPermissions,
            @Qualifier("activityGetQuarterlyReportActivityMetadataActionPermissions") GetQuarterlyReportActivityMetadataActionPermissions getQuarterlyReportActivityMetadataActionPermissions,
            @Qualifier("activityGetAnnualReportActivityMetadataActionPermissions") GetAnnualReportActivityMetadataActionPermissions getAnnualReportActivityMetadataActionPermissions,
            @Qualifier("activityGetChangeRequestActivityMetadataActionPermissions") GetChangeRequestActivityMetadataActionPermissions getChangeRequestActivityMetadataActionPermissions,
            @Qualifier("activityGetApiKeyManagementActivityMetadataActionPermissions") GetApiKeyManagementActivityMetadataActionPermissions getApiKeyManagementMetadataActionPermissions,
            @Qualifier("getFunctionalityTestedActivityMetadataActionPermissions") GetFunctionalityTestedMetadataActionPermissions getFunctionalityTestedMetadataActionPermissions,
            @Qualifier("getStandardActivityMetadataActionPermissions") GetStandardMetadataActionPermissions getStandardMetadataActionPermissions,
            @Qualifier("getSvapActivityMetadataActionPermissions") GetSvapMetadataActionPermissions getSvapMetadataActionPermissions,
            @Qualifier("searchActivityActionPermissions") SearchActivityActionPermissions searchActivityActionPermissions){

        getActionPermissions().put(GET_ACB_METADATA, getAcbActivityMetadataActionPermissions);
        getActionPermissions().put(GET_METADATA_BY_ACB, getActivityMetadataByAcbActionPermissions);
        getActionPermissions().put(GET_ATL_METADATA, getAtlActivityMetadataActionPermissions);
        getActionPermissions().put(GET_METADATA_BY_ATL, getActivityMetadataByAtlActionPermissions);
        getActionPermissions().put(GET_ACTIVITY_DETAILS, getActivityDetails);
        getActionPermissions().put(GET_USER_MAINTENANCE_METADATA, getUserMaintenanceMetadataActionPermissions);
        getActionPermissions().put(GET_ACTIVITY_METADATA_BY_CONCEPT, getActivityMetadataByConceptActionPermissions);
        getActionPermissions().put(GET_PENDING_SURVEILLANCE_METADATA, getPendingSurveillanceMetadataActionPermissions);
        getActionPermissions().put(GET_COMPLAINT_METADATA, getComplaintActivityMetadataActionPermissions);
        getActionPermissions().put(GET_QUARTERLY_REPORT_METADATA, getQuarterlyReportActivityMetadataActionPermissions);
        getActionPermissions().put(GET_ANNUAL_REPORT_METADATA, getAnnualReportActivityMetadataActionPermissions);
        getActionPermissions().put(GET_CHANGE_REQUEST_METADATA, getChangeRequestActivityMetadataActionPermissions);
        getActionPermissions().put(GET_API_KEY_MANAGEMENT_METADATA, getApiKeyManagementMetadataActionPermissions);
        getActionPermissions().put(GET_FUNCTIONALITY_TESTED_METADATA, getFunctionalityTestedMetadataActionPermissions);
        getActionPermissions().put(GET_STANDARD_METADATA, getStandardMetadataActionPermissions);
        getActionPermissions().put(GET_SVAP_METADATA, getSvapMetadataActionPermissions);
        getActionPermissions().put(SEARCH, searchActivityActionPermissions);
    }
}
