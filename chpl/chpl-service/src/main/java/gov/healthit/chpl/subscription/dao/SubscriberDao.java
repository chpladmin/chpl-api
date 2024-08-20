package gov.healthit.chpl.subscription.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.domain.SubscriberRole;
import gov.healthit.chpl.subscription.domain.SubscriberStatus;
import gov.healthit.chpl.subscription.entity.SubscriberEntity;
import gov.healthit.chpl.subscription.entity.SubscriberRoleEntity;
import gov.healthit.chpl.subscription.entity.SubscriberStatusEntity;
import gov.healthit.chpl.subscription.service.SubscriptionLookupUtil;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class SubscriberDao extends BaseDAOImpl {
    private static final String SUBSCRIBER_HQL = "SELECT subscriber "
            + "FROM SubscriberEntity subscriber "
            + "JOIN FETCH subscriber.subscriberStatus "
            + "LEFT JOIN FETCH subscriber.subscriberRole "
            + "WHERE subscriber.deleted = false ";

    private SubscriptionLookupUtil lookupUtil;

    @Autowired
    public SubscriberDao(SubscriptionLookupUtil lookupUtil) {
        this.lookupUtil = lookupUtil;
    }

    public List<SubscriberRole> getAllRoles() {
        Query query = entityManager.createQuery("SELECT roles "
                + "FROM SubscriberRoleEntity roles "
                + "WHERE roles.deleted = false "
                + "ORDER BY sortOrder",
                SubscriberRoleEntity.class);

        List<SubscriberRoleEntity> results = query.getResultList();
        return results.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public List<SubscriberStatus> getAllStatuses() {
        Query query = entityManager.createQuery("SELECT statuses "
                + "FROM SubscriberStatusEntity statuses "
                + "WHERE statuses.deleted = false ",
                SubscriberStatusEntity.class);
        List<SubscriberStatusEntity> results = query.getResultList();
        return results.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public UUID createSubscriber(String email) {
        SubscriberEntity subscriberToCreate = new SubscriberEntity();
        subscriberToCreate.setEmail(email);
        subscriberToCreate.setSubscriberStatusId(lookupUtil.getPendingSubscriberStatusId());
        create(subscriberToCreate);
        return subscriberToCreate.getId();
    }

    public void confirmSubscriber(UUID subscriberUuid, Long roleId) {
        SubscriberEntity subscriber = entityManager.find(SubscriberEntity.class, subscriberUuid);
        if (subscriber == null || subscriber.getDeleted().equals(Boolean.TRUE)) {
            LOGGER.error("No subscriber was found with ID " + subscriberUuid);
            return;
        }
        subscriber.setSubscriberStatusId(lookupUtil.getConfirmedSubscriberStatusId());
        subscriber.setSubscriberRoleId(roleId);
        update(subscriber);
    }

    public void deleteSubscriber(UUID subscriberUuid) {
        SubscriberEntity subscriber = entityManager.find(SubscriberEntity.class, subscriberUuid);
        if (subscriber == null || subscriber.getDeleted().equals(Boolean.TRUE)) {
            LOGGER.error("No subscriber was found with ID " + subscriberUuid);
            return;
        }
        subscriber.setDeleted(true);
        update(subscriber);
    }

    public Subscriber getSubscriberByEmail(String email) {
        Query query = entityManager.createQuery(SUBSCRIBER_HQL
                + "AND subscriber.email = :email",
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
                + "AND subscriber.id = :id",
                SubscriberEntity.class);
        query.setParameter("id", id);
        List<SubscriberEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.get(0).toDomain();
    }
}
