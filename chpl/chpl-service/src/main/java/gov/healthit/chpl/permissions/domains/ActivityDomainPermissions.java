package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.activity.GetActivityDetailsActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetActivityMetadataByAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetActivityMetadataByAtlActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetAtlActivityMetadataActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetByAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetAcbActivityMetadataActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetPendingListingActivityActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetPendingListingActivityByAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetUserActivityActionPermissions;

@Component
public class ActivityDomainPermissions extends DomainPermissions {
    public static final String GET_BY_ACB = "GET_BY_ACB";
    public static final String GET_ACB_METADATA = "GET_ACB_METADATA";
    public static final String GET_METADATA_BY_ACB = "GET_METADATA_BY_ACB";
    public static final String GET_ATL_METADATA = "GET_ATL_METADATA";
    public static final String GET_METADATA_BY_ATL = "GET_METADATA_BY_ATL";
    public static final String GET_PENDING_LISTING_ACTIVITY_BY_ACB = "GET_PENDING_LISTING_ACTIVITY_BY_ACB";
    public static final String GET_PENDING_LISTING_ACTIVITY = "GET_PENDING_LISTING_ACTIVITY";
    public static final String GET_USER_ACTIVITY = "GET_USER_ACTIVITY";
    public static final String GET_ACTIVITY_DETAILS = "GET_ACTIVITY_DETAILS";

    @Autowired
    public ActivityDomainPermissions(
            @Qualifier("actionGetByAcbActionPermissions") GetByAcbActionPermissions getByAcbActionPermissions,
            @Qualifier("actionGetAcbActivityMetadataActionPermissions") GetAcbActivityMetadataActionPermissions getAcbActivityMetadataActionPermissions,
            @Qualifier("actionGetActivityMetadataByAcbActionPermissions") GetActivityMetadataByAcbActionPermissions getActivityMetadataByAcbActionPermissions,
            @Qualifier("actionGetAtlActivityMetadataActionPermissions") GetAtlActivityMetadataActionPermissions getAtlActivityMetadataActionPermissions,
            @Qualifier("actionGetActivityMetadataByAtlActionPermissions") GetActivityMetadataByAtlActionPermissions getActivityMetadataByAtlActionPermissions,
            @Qualifier("actionGetPendingListingActivityByAcbActionPermissions") GetPendingListingActivityByAcbActionPermissions getPendingListingActivityByAcb,
            @Qualifier("actionGetPendingListingActivityActionPermissions") GetPendingListingActivityActionPermissions getPendingListingActivity,
            @Qualifier("actionGetUserActivityActionPermissions") GetUserActivityActionPermissions getUserActivity,
            @Qualifier("actionGetActivityDetailsActionPermissions") GetActivityDetailsActionPermissions getActivityDetails) {
        getActionPermissions().put(GET_BY_ACB, getByAcbActionPermissions);
        getActionPermissions().put(GET_ACB_METADATA, getAcbActivityMetadataActionPermissions);
        getActionPermissions().put(GET_METADATA_BY_ACB, getActivityMetadataByAcbActionPermissions);
        getActionPermissions().put(GET_ATL_METADATA, getAtlActivityMetadataActionPermissions);
        getActionPermissions().put(GET_METADATA_BY_ATL, getActivityMetadataByAtlActionPermissions);
        getActionPermissions().put(GET_PENDING_LISTING_ACTIVITY_BY_ACB, getPendingListingActivityByAcb);
        getActionPermissions().put(GET_PENDING_LISTING_ACTIVITY, getPendingListingActivity);
        getActionPermissions().put(GET_USER_ACTIVITY, getUserActivity);
        getActionPermissions().put(GET_ACTIVITY_DETAILS, getActivityDetails);
    }
}
