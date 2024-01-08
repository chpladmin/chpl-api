package gov.healthit.chpl.subscription.dao;

import java.util.List;
import java.util.UUID;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import gov.healthit.chpl.subscription.entity.SubscriptionEntity;
import gov.healthit.chpl.subscription.entity.SubscriptionObservationEntity;
import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class SubscriptionObservationDao extends BaseDAOImpl {
    private static final String OBSERVATION_HQL = "SELECT observation "
            + "FROM SubscriptionObservationEntity observation "
            + "JOIN FETCH observation.subscription subscription "
            + "JOIN FETCH subscription.subscriber subscriber "
            + "JOIN FETCH subscriber.subscriberStatus "
            + "JOIN FETCH subscriber.subscriberRole "
            + "JOIN FETCH subscription.subscriptionSubject subject "
            + "JOIN FETCH subject.subscriptionObjectType "
            + "JOIN FETCH subscription.subscriptionConsolidationMethod consolidationMethod ";

    public void createObservations(List<Long> subscriptionIds, Long activityId) {
        //TODO: figure out how to batch insert these observations
        subscriptionIds.stream()
            .forEach(subscriptionId -> createObservation(subscriptionId, activityId));
    }

    public void createObservation(Long subscriptionId, Long activityId) {
        try {
            SubscriptionObservationEntity observationToCreate = new SubscriptionObservationEntity();
            observationToCreate.setActivityId(activityId);
            observationToCreate.setSubscriptionId(subscriptionId);
            create(observationToCreate);
        } catch (Exception ex) {
            LOGGER.error("Unable to save observation for subscription ID " + subscriptionId + " and activity ID " + activityId, ex);
        }
    }

    public List<SubscriptionObservation> getObservations(Long consolidationMethodId) {
        Query query = entityManager.createQuery(OBSERVATION_HQL
                + "WHERE consolidationMethod.id = :consolidationMethodId ",
                SubscriptionObservationEntity.class);
        query.setParameter("consolidationMethodId", consolidationMethodId);

        List<SubscriptionObservationEntity> results = query.getResultList();
        return results.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public void deleteObservations(List<Long> observationIds) {
        Query query = entityManager.createQuery("UPDATE SubscriptionObservationEntity observations "
                + "SET observations.deleted = true "
                + "WHERE observations.id IN (:observationIds)");
        query.setParameter("observationIds", observationIds);
        query.executeUpdate();
    }

    public void deleteAllObservationsForSubscriber(UUID subscriberId) {
        LOGGER.info("Deleting observations for subscriber " + subscriberId);
        //get the subscription IDs that are being deleted
        Query subscriptionIdsQuery = entityManager.createQuery("SELECT sub "
                + "FROM SubscriptionEntity sub "
                + "WHERE sub.subscriberId = :subscriberId",
                SubscriptionEntity.class);
        subscriptionIdsQuery.setParameter("subscriberId", subscriberId);
        List<Long> subscriptionIds = subscriptionIdsQuery.getResultList().stream()
                .map(sub -> ((SubscriptionEntity) sub).getId())
                .toList();
        deleteObservationsForSubscriptions(subscriptionIds);
    }

    public void deleteObservationsForSubscribedObject(UUID subscriberId, Long subscribedObjectTypeId, Long subscribedObjectId) {
        LOGGER.info("Deleting observations for subscriber " + subscriberId + " and object type " + subscribedObjectTypeId + " and object ID " + subscribedObjectId);
        //get the subscription IDs that are being deleted
        Query subscriptionIdsQuery = entityManager.createQuery("SELECT sub "
                + "FROM SubscriptionEntity sub "
                + "WHERE sub.subscriberId = :subscriberId "
                + "AND sub.subscriptionSubject.subscriptionObjectType.id = :subscribedObjectTypeId "
                + "AND sub.subscribedObjectId = :subscribedObjectId",
                SubscriptionEntity.class);
        subscriptionIdsQuery.setParameter("subscriberId", subscriberId);
        subscriptionIdsQuery.setParameter("subscribedObjectTypeId", subscribedObjectTypeId);
        subscriptionIdsQuery.setParameter("subscribedObjectId", subscribedObjectId);
        List<Long> subscriptionIds = subscriptionIdsQuery.getResultList().stream()
                .map(sub -> ((SubscriptionEntity) sub).getId())
                .toList();
        deleteObservationsForSubscriptions(subscriptionIds);
    }

    public void deleteObservationsForSubscription(Long subscriptionId) {
        LOGGER.info("Deleting observations for subscription " + subscriptionId);
        int numUpdates = entityManager.createQuery(
                "UPDATE SubscriptionObservationEntity "
                + "SET deleted = true "
                + "WHERE subscriptionId = :subscriptionId")
        .setParameter("subscriptionId", subscriptionId)
        .executeUpdate();
        LOGGER.info("Deleted " + numUpdates + " observations.");
    }

    private void deleteObservationsForSubscriptions(List<Long> subscriptionIds) {
        LOGGER.info("Deleting observations for " + subscriptionIds + " subscriptions.");
        int numUpdates = entityManager.createQuery(
                "UPDATE SubscriptionObservationEntity "
                + "SET deleted = true "
                + "WHERE subscriptionId IN (:subscriptionIds)")
        .setParameter("subscriptionIds", subscriptionIds)
        .executeUpdate();
        LOGGER.info("Deleted " + numUpdates + " observations.");
    }
}
