package gov.healthit.chpl.scheduler.job.subscriptions.subjects.formatter;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import gov.healthit.chpl.subscription.subject.processor.CertificationStatusChangedActivityProcessor;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "subscriptionObservationsNotificationJobLogger")
public class CertificationStatusChangedFormatter extends ObservationSubjectFormatter {
    private static final String DESCRIPTION_UNFORMATTED = "Certification status changed from '%s' to '%s'";

    private ActivityDAO activityDao;
    private ListingActivityUtil listingActivityUtil;

    @Autowired
    public CertificationStatusChangedFormatter(@Qualifier("activityDAO") ActivityDAO activityDao,
            ListingActivityUtil listingActivityUtil) {
        this.activityDao = activityDao;
        this.listingActivityUtil = listingActivityUtil;
    }

    public List<String> toListOfStrings(SubscriptionObservation observation) {
        ActivityDTO activity = null;
        try {
            activity = activityDao.getById(observation.getActivityId());
        } catch (Exception ex) {
            LOGGER.error("Could not get activity with ID " + observation.getActivityId(), ex);
        }

        if (activity == null) {
            return null;
        }

        CertifiedProductSearchDetails before = null, after = null;
        try {
            before = listingActivityUtil.getListing(activity.getOriginalData());
        } catch (Exception ex) {
            LOGGER.error("Could not convert 'originalData' from activity " + activity.getId() + " to a listing details.", ex);
        }

        try {
            after = listingActivityUtil.getListing(activity.getNewData());
        } catch (Exception ex) {
            LOGGER.error("Could not convert 'newData' from activity " + activity.getId() + " to a listing details.", ex);
        }

        if (before == null || after == null) {
            return null;
        }

        return Stream.of(CertificationStatusChangedActivityProcessor.SUBJECT_NAME,
                String.format(DESCRIPTION_UNFORMATTED, before.getCurrentStatus().getStatus().getName(),
                        after.getCurrentStatus().getStatus().getName())).toList();
    }
}
