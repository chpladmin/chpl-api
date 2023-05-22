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
        //TODO: I want to batch insert these observations but when I turn on SQL printing
        //it doesn't appear as though that is being done. Need to investigate more or remove the code.
        for (int i = 0; i < subscriptionIds.size(); i++) {
            if (i > 0 && i % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
            SubscriptionObservationEntity observationToCreate = new SubscriptionObservationEntity();
            observationToCreate.setLastModifiedUser(User.DEFAULT_USER_ID);
            observationToCreate.setActivityId(activityId);
            observationToCreate.setSubscriptionId(subscriptionIds.get(i));
            entityManager.persist(observationToCreate);
        }
    }

    public List<SubscriptionObservation> getObservations(Long consolidationMethodId) {
        Query query = entityManager.createQuery(OBSERVATION_HQL
                + "WHERE consolidationMethod.id = :consolidationMethodid ",
                SubscriptionObservationEntity.class);

        List<SubscriptionObservationEntity> results = query.getResultList();
        return results.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public void deleteObservations(List<Long> observationIds) {
        entityManager.createQuery("UPDATE SubscriptionObservationEntity observations "
                + "SET obsrevations.deleted = true "
                + "WHERE obsrevations.id IN (:observationIds)")
        .executeUpdate();
    }
}
