package gov.healthit.chpl.scheduler.job.certificationStatus;

import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.subscription.dao.SubscriptionDao;
import gov.healthit.chpl.subscription.dao.SubscriptionObservationDao;
import gov.healthit.chpl.subscription.service.SubscriptionLookupUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "updateCertificationStatusJobLogger")
public class TransactionalSubscriptionObservationHelper {

    private SubscriptionDao subscriptionDao;
    private SubscriptionObservationDao observationDao;
    private Long certificationStatusChangedSubjectId;

    @Autowired
    public TransactionalSubscriptionObservationHelper(SubscriptionLookupUtil lookupUtil,
            SubscriptionDao subscriptionDao, SubscriptionObservationDao observationDao) {
        this.subscriptionDao = subscriptionDao;
        this.observationDao = observationDao;
        this.certificationStatusChangedSubjectId = lookupUtil.getCertificationStatusChangedSubjectId();
    }

    @Transactional
    public void handleCertificationStatusChange(CertifiedProductSearchDetails listing, Long activityId) {
        List<Long> subscriptionIds = subscriptionDao.getSubscriptionIdsForConfirmedSubscribers(certificationStatusChangedSubjectId, listing.getId());
        if (!CollectionUtils.isEmpty(subscriptionIds)) {
            observationDao.createObservations(subscriptionIds, activityId);
        }
    }
}
