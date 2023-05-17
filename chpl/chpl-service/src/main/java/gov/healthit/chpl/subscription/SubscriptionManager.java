package gov.healthit.chpl.subscription;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.subscription.dao.SubscriptionDao;
import gov.healthit.chpl.subscription.domain.SubscriptionReason;

@Component
public class SubscriptionManager {
    private SubscriptionDao subscriptionDao;

    @Autowired
    public SubscriptionManager(SubscriptionDao subscriptionDao) {
        this.subscriptionDao = subscriptionDao;
    }

    public List<SubscriptionReason> getAllReasons() {
        return subscriptionDao.getAllReasons();
    }
}
