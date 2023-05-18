package gov.healthit.chpl.subscription;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.subscription.dao.SubscriptionDao;
import gov.healthit.chpl.subscription.domain.Subscription;
import gov.healthit.chpl.subscription.domain.SubscriptionReason;
import gov.healthit.chpl.subscription.domain.SubscriptionRequest;

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

    public Subscription subscribe(SubscriptionRequest subscriptionRequest) {
        //at some point we should validate this request - that there is an object of the appropriate
        //type with the ID that the user specified. Here? Would we notify them in the UI or just via an email?


//        -- Does a Confirmed subscriber with the email address already exist?
//                -- Create a new subscription for this subscriber
//            -- Does a Pending subscriber with the email address already exist?
//                -- Re-send the email with a link using their same subscriber token
//            -- Does no subscriber with the email address exist?
//                -- Create a Pending subscriber and the subscription for them
//                -- Send them an email with a link using their subscriber token
//            -- If subscription is a duplicate (same subscriber id, subject, object) then do nothing

        return null;
    }
}
