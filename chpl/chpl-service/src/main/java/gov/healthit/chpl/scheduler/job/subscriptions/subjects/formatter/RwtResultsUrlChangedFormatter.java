package gov.healthit.chpl.scheduler.job.subscriptions.subjects.formatter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "subscriptionObservationsNotificationJobLogger")
public class RwtResultsUrlChangedFormatter extends ObservationSubjectFormatter {
    private static final String DESCRIPTION_REMOVED = "RWT Results URL was removed.";
    private static final String DESCRIPTION_ADDED_UNFORMATTED = "RWT Results URL %s was added.";
    private static final String DESCRIPTION_UPDATED_UNFORMATTED = "RWT Results URL was changed from %s to %s";

    @Autowired
    public RwtResultsUrlChangedFormatter(@Qualifier("activityDAO") ActivityDAO activityDao,
            ListingActivityUtil listingActivityUtil) {
        super(activityDao, listingActivityUtil);
    }

    @Override
    public List<List<String>> toListsOfStrings(SubscriptionObservation observation) {
        ActivityDTO activity = getActivity(observation.getActivityId());

        CertifiedProductSearchDetails before = getListing(activity.getOriginalData());
        CertifiedProductSearchDetails after = getListing(activity.getNewData());

        if (before == null || after == null) {
            LOGGER.error("There was a problem turning activityID " + activity.getId() + " into listing details objects.");
            return null;
        }

        List<List<String>> formattedObservations = new ArrayList<List<String>>();
        if (!StringUtils.isEmpty(before.getRwtResultsUrl()) && StringUtils.isEmpty(after.getRwtResultsUrl())) {
            formattedObservations.add(Stream.of(DESCRIPTION_REMOVED).toList());
        } else if (StringUtils.isEmpty(before.getRwtResultsUrl()) && !StringUtils.isEmpty(after.getRwtResultsUrl())) {
            formattedObservations.add(Stream.of(
                    String.format(DESCRIPTION_ADDED_UNFORMATTED, after.getRwtResultsUrl()))
                    .toList());
        } else {
            String updatedObservation = String.format(DESCRIPTION_UPDATED_UNFORMATTED, before.getRwtResultsUrl(), after.getRwtResultsUrl());
            formattedObservations.add(Stream.of(updatedObservation).toList());
        }

        return formattedObservations;
    }
}
