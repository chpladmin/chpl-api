package gov.healthit.chpl.scheduler.job.certificationStatus;

import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.subscription.dao.SubscriptionDao;
import gov.healthit.chpl.subscription.dao.SubscriptionObservationDao;
import gov.healthit.chpl.subscription.service.SubscriptionLookupUtil;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "updateCurrentCertificationStatusJobLogger")
public class TransactionalSubscriptionObservationHelper {

    private ActivityDAO activityDao;
    private SubscriptionDao subscriptionDao;
    private SubscriptionObservationDao observationDao;
    private Long certificationStatusChangedSubjectId;

    @Autowired
    public TransactionalSubscriptionObservationHelper(ActivityDAO activityDao,
            SubscriptionLookupUtil lookupUtil,
            SubscriptionDao subscriptionDao, SubscriptionObservationDao observationDao) {
        this.activityDao = activityDao;
        this.subscriptionDao = subscriptionDao;
        this.observationDao = observationDao;
        this.certificationStatusChangedSubjectId = lookupUtil.getCertificationStatusChangedSubjectId();
    }

    @Transactional
    public void handleCertificationStatusChange(CertifiedProductSearchDetails listing, Long activityId) {
        ActivityDTO activity = null;
        try {
            activity = activityDao.getById(activityId);
        } catch (Exception ex) {
            LOGGER.warn("Unable to find activity " + activityId);
            return;
        }

        if (activity == null) {
            return;
        }

        if (dayOfActivityIsBeforeDayOfStatusChange(activity, listing)) {
            LOGGER.info("Activity ID " + activityId + " on " + activity.getActivityDate() + " occurred before"
                    + " the new certification status on " + listing.getCurrentStatus().getEventDay());
            List<Long> subscriptionIds = subscriptionDao.getSubscriptionIdsForConfirmedSubscribers(certificationStatusChangedSubjectId, listing.getId());
            LOGGER.info("Creating observations for " + subscriptionIds.size()
                + " 'Certification Status Change' subscriptions for listing " + listing.getId());
            if (!CollectionUtils.isEmpty(subscriptionIds)) {
                observationDao.createObservations(subscriptionIds, activityId);
            }
        } else {
            LOGGER.info("Activity ID " + activityId + " on " + activity.getActivityDate() + " did not occur before "
                    + "the new certification status on " + listing.getCurrentStatus().getEventDay());
        }
    }

    private boolean dayOfActivityIsBeforeDayOfStatusChange(ActivityDTO activity, CertifiedProductSearchDetails listing) {
        return DateUtil.toLocalDate(activity.getActivityDate().getTime()).isBefore(listing.getCurrentStatus().getEventDay());
    }
}
