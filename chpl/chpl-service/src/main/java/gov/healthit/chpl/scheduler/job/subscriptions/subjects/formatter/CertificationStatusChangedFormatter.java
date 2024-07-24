package gov.healthit.chpl.scheduler.job.subscriptions.subjects.formatter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "subscriptionObservationsNotificationJobLogger")
public class CertificationStatusChangedFormatter extends ObservationSubjectFormatter {
    private static final String STATUS_CHANGED = "Certification status changed from '%s' on %s "
            + "to '%s' on %s";

    @Autowired
    public CertificationStatusChangedFormatter(@Qualifier("activityDAO") ActivityDAO activityDao,
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

        LocalDate today = LocalDate.now();
        List<List<String>> formattedObservations = new ArrayList<List<String>>();
        CertificationStatusEvent beforeStatusEvent = before.getCurrentStatus();
        CertificationStatusEvent afterStatusEvent = after.getCurrentStatus();

        formattedObservations.add(
            Stream.of(observation.getSubscription().getSubject().getSubject(),
                String.format(STATUS_CHANGED, beforeStatusEvent.getStatus().getName(),
                        beforeStatusEvent.getEventDay(),
                        afterStatusEvent.getStatus().getName(),
                        afterStatusEvent.getEventDay()),
                DateUtil.formatInEasternTime(activity.getActivityDate()))
            .toList());
        return formattedObservations;
    }
}
