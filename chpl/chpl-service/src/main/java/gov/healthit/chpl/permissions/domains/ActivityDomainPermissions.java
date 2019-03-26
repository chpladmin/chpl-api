package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.activity.GetActivityDetailsActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetByAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetPendingListingActivityActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetPendingListingActivityByAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.activity.GetUserActivityActionPermissions;

@Component
public class ActivityDomainPermissions extends DomainPermissions {
    public static final String GET_BY_ACB = "GET_BY_ACB";
    public static final String GET_PENDING_LISTING_ACTIVITY_BY_ACB = "GET_PENDING_LISTING_ACTIVITY_BY_ACB";
    public static final String GET_PENDING_LISTING_ACTIVITY = "GET_PENDING_LISTING_ACTIVITY";
    public static final String GET_USER_ACTIVITY = "GET_USER_ACTIVITY";
    public static final String GET_ACTIVITY_DETAILS = "GET_ACTIVITY_DETAILS";

    @Autowired
    public ActivityDomainPermissions(
            @Qualifier("actionGetByAcbActionPermissions") GetByAcbActionPermissions getByAcbActionPermissions,
            @Qualifier("actionGetPendingListingActivityByAcbActionPermissions") GetPendingListingActivityByAcbActionPermissions getPendingListingActivityByAcb,
            @Qualifier("actionGetPendingListingActivityActionPermissions") GetPendingListingActivityActionPermissions getPendingListingActivity,
            @Qualifier("actionGetUserActivityActionPermissions") GetUserActivityActionPermissions getUserActivity,
            @Qualifier("actionGetActivityDetailsActionPermissions") GetActivityDetailsActionPermissions getActivityDetails) {
        getActionPermissions().put(GET_BY_ACB, getByAcbActionPermissions);
        getActionPermissions().put(GET_PENDING_LISTING_ACTIVITY_BY_ACB, getPendingListingActivityByAcb);
        getActionPermissions().put(GET_PENDING_LISTING_ACTIVITY, getPendingListingActivity);
        getActionPermissions().put(GET_USER_ACTIVITY, getUserActivity);
        getActionPermissions().put(GET_ACTIVITY_DETAILS, getActivityDetails);
    }
}
