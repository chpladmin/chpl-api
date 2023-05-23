package gov.healthit.chpl.subscription.dao;

import java.util.List;
import java.util.UUID;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.entity.SubscriberEntity;
import gov.healthit.chpl.subscription.service.SubscriptionLookupUtil;
import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class SubscriberDao extends BaseDAOImpl {
    private static final String SUBSCRIBER_HQL = "SELECT subscriber "
            + "FROM SubscriberEntity subscriber "
            + "JOIN FETCH subscriber.subscriberStatus ";

    private SubscriptionLookupUtil lookupUtil;

    @Autowired
    public SubscriberDao(SubscriptionLookupUtil lookupUtil) {
        this.lookupUtil = lookupUtil;
    }

    public UUID createSubscriber(String email) {
        SubscriberEntity subscriberToCreate = new SubscriberEntity();
        subscriberToCreate.setEmail(email);
        subscriberToCreate.setLastModifiedUser(User.DEFAULT_USER_ID);
        subscriberToCreate.setSubscriberStatusId(lookupUtil.getPendingSubscriberStatusId());
        create(subscriberToCreate);
        return subscriberToCreate.getId();
    }

    public void confirmSubscriber(UUID subscriberUuid) {
        SubscriberEntity subscriber = entityManager.find(SubscriberEntity.class, subscriberUuid);
        if (subscriber == null) {
            LOGGER.error("No subscriber was found with ID " + subscriberUuid);
            return;
        }
        subscriber.setSubscriberStatusId(lookupUtil.getConfirmedSubscriberStatusId());
        update(subscriber);
    }

    public Subscriber getSubscriberByEmail(String email) {
        Query query = entityManager.createQuery(SUBSCRIBER_HQL
                + "WHERE subscriber.email = :email",
                SubscriberEntity.class);
        query.setParameter("email", email);
        List<SubscriberEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.get(0).toDomain();
    }

    public Subscriber getSubscriberById(UUID id) {
        Query query = entityManager.createQuery(SUBSCRIBER_HQL
                + "WHERE subscriber.id = :id",
                SubscriberEntity.class);
        query.setParameter("id", id);
        List<SubscriberEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.get(0).toDomain();
    }
}
