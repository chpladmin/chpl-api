package gov.healthit.chpl.subscription.dao;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
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
            + "LEFT JOIN FETCH subscription.subscriptionReason "
            + "JOIN FETCH subscription.subscriptionSubject subject "
            + "JOIN FETCH subject.subscriptionObjectType "
            + "JOIN FETCH subscription.subscriptionConsolidationMethod consolidationMethod ";

    public void createObservations(List<Long> subscriptionIds, Long activityId) {
        //TODO: I want to batch insert these observations
        subscriptionIds.stream()
            .forEach(subscriptionId -> createObservation(subscriptionId, activityId));
    }

    public void createObservation(Long subscriptionId, Long activityId) {
        try {
            SubscriptionObservationEntity observationToCreate = new SubscriptionObservationEntity();
            observationToCreate.setLastModifiedUser(User.DEFAULT_USER_ID);
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
}
