package gov.healthit.chpl.subscription;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.subscription.dao.SubscriberDao;
import gov.healthit.chpl.subscription.dao.SubscriptionDao;
import gov.healthit.chpl.subscription.domain.SubscribedObjectType;
import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.domain.SubscriberStatus;
import gov.healthit.chpl.subscription.domain.Subscription;
import gov.healthit.chpl.subscription.domain.SubscriptionReason;
import gov.healthit.chpl.subscription.domain.SubscriptionRequest;

@Component
public class SubscriptionManager {
    private SubscriberDao subscriberDao;
    private SubscriptionDao subscriptionDao;

    @Autowired
    public SubscriptionManager(SubscriberDao subscriberDao,
            SubscriptionDao subscriptionDao) {
        this.subscriberDao = subscriberDao;
        this.subscriptionDao = subscriptionDao;
    }

    public List<SubscriptionReason> getAllReasons() {
        return subscriptionDao.getAllReasons();
    }

    public List<SubscribedObjectType> getAllSubscribedObjectTypes() {
        return subscriptionDao.getAllSubscribedObjectTypes();
    }

    public Subscription subscribe(SubscriptionRequest subscriptionRequest) {
        //TODO:  we should validate this request
            // that the subscribed object type ID is valid
            // that there is an object of the appropriate type with the ID that the user specified
            // that the reason ID is valid
            // I think we should throw a validation exception and return bad request

        Subscriber subscriber = subscriberDao.getSubscriberByEmail(subscriptionRequest.getEmail());
        if (subscriber == null) {
            UUID newSubscriberId = subscriberDao.createSubscriber(subscriptionRequest.getEmail());
            subscriber = subscriberDao.getSubscriberById(newSubscriberId);
        }

        if (subscriber.getStatus().getName().equals(SubscriberStatus.SUBSCRIBER_STATUS_PENDING)) {
            //TOOD: send a confirmation email to this email address with a link using their GUID
        }

        //TODO Create a new subscription for this subscriber
        //TODO: If the subscription is a duplicate (same subsciber id, type, and object id) do nothing

        return null;
    }
}