package gov.healthit.chpl.scheduler.job.subscriptions.subjects.formatter;

import java.util.List;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "subscriptionObservationsNotificationJobLogger")
public abstract class ObservationSubjectFormatter {
    private ActivityDAO activityDao;
    private ListingActivityUtil listingActivityUtil;

    public ObservationSubjectFormatter(ActivityDAO activityDao,
            ListingActivityUtil listingActivityUtil) {
        this.activityDao = activityDao;
        this.listingActivityUtil = listingActivityUtil;
    }

    ActivityDTO getActivity(Long activityId) {
        ActivityDTO activity = null;
        try {
            activity = activityDao.getById(activityId);
        } catch (Exception ex) {
            LOGGER.error("Could not get activity with ID " + activityId, ex);
        }

        if (activity == null) {
            return null;
        }
        return activity;
    }

    CertifiedProductSearchDetails getListing(String listingJson) {
        CertifiedProductSearchDetails listing = null;
        try {
            listing = listingActivityUtil.getListing(listingJson);
        } catch (Exception ex) {
            LOGGER.error("Could not convert listing JSON '" + listingJson + "' to a listing details object.", ex);
        }
        return listing;
    }

    public abstract List<List<String>> toListsOfStrings(SubscriptionObservation observation);
}
