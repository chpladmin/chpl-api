package gov.healthit.chpl.subscription.dao;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.subscription.domain.Subscription;
import gov.healthit.chpl.subscription.domain.SubscriptionConsolidationMethod;
import gov.healthit.chpl.subscription.domain.SubscriptionObjectType;
import gov.healthit.chpl.subscription.domain.SubscriptionSubject;
import gov.healthit.chpl.subscription.entity.SubscriptionConsolidationMethodEntity;
import gov.healthit.chpl.subscription.entity.SubscriptionEntity;
import gov.healthit.chpl.subscription.entity.SubscriptionObjectTypeEntity;
import gov.healthit.chpl.subscription.entity.SubscriptionSearchResultEntity;
import gov.healthit.chpl.subscription.entity.SubscriptionSubjectEntity;
import gov.healthit.chpl.subscription.search.SubscriptionSearchResult;
import gov.healthit.chpl.subscription.service.SubscriptionLookupUtil;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class SubscriptionDao extends BaseDAOImpl {
    private static final String SUBSCRIPTION_HQL = "SELECT subscription "
            + "FROM SubscriptionEntity subscription "
            + "JOIN FETCH subscription.subscriber subscriber "
            + "JOIN FETCH subscriber.subscriberStatus "
            + "JOIN FETCH subscription.subscriptionSubject subject "
            + "JOIN FETCH subject.subscriptionObjectType "
            + "JOIN FETCH subscription.subscriptionConsolidationMethod "
            + "WHERE subscription.deleted = false "
            + "AND subscriber.deleted = false ";

    private SubscriptionLookupUtil lookupUtil;

    public SubscriptionDao(SubscriptionLookupUtil lookupUtil) {
        this.lookupUtil = lookupUtil;
    }

    public List<SubscriptionObjectType> getAllSubscriptionObjectTypes() {
        Query query = entityManager.createQuery("SELECT types "
                + "FROM SubscriptionObjectTypeEntity types "
                + "WHERE types.deleted = false ",
                SubscriptionObjectTypeEntity.class);

        List<SubscriptionObjectTypeEntity> results = query.getResultList();
        return results.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    @Transactional
    public List<SubscriptionSubject> getAllSubjects() {
        Query query = entityManager.createQuery("SELECT subjects "
                + "FROM SubscriptionSubjectEntity subjects "
                + "WHERE subjects.deleted = false ",
                SubscriptionSubjectEntity.class);

        List<SubscriptionSubjectEntity> results = query.getResultList();
        return results.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public List<SubscriptionConsolidationMethod> getAllConsolidationMethods() {
        Query query = entityManager.createQuery("SELECT methods "
                + "FROM SubscriptionConsolidationMethodEntity methods "
                + "WHERE methods.deleted = false ",
                SubscriptionConsolidationMethodEntity.class);

        List<SubscriptionConsolidationMethodEntity> results = query.getResultList();
        return results.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    //making this public because I suspect we will need it eventually for the management page
    public List<SubscriptionSubject> getAllSubjectsForObjectType(Long subscriptionObjectTypeId) {
        Query query = entityManager.createQuery("SELECT subjects "
                + "FROM SubscriptionSubjectEntity subjects "
                + "WHERE subjects.subscriptionObjectTypeId = :subscriptionObjectTypeId "
                + "AND subjects.deleted = false",
                SubscriptionSubjectEntity.class);
        query.setParameter("subscriptionObjectTypeId", subscriptionObjectTypeId);

        List<SubscriptionSubjectEntity> results = query.getResultList();
        return results.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public void createSubscription(UUID subscriberId, Long subscribedObjectTypeId, Long subscribedObjectId) {
        //Subscribing to a particular object may create create multiple subscriptions for each
        //"subject" related to that type of object. i.e. different things updated on a listing are different subjects.
        //A future management page might give a user more fine-grained control over the "subjects"
        //they subscribe to, so they could subscribe to surveillance added to a listing but not certification status changes.
        //For now, when you create a new subscription it will default to subscribing to all related subjects.
        List<SubscriptionSubject> subjectsForObjectType = getAllSubjectsForObjectType(subscribedObjectTypeId);

        subjectsForObjectType.stream()
            .forEach(subject -> createSubscriptionIfNotExists(
                    subscriberId, subscribedObjectId, subject.getId(),
                    lookupUtil.getDailyConsolidationMethodId()));
    }

    private void createSubscriptionIfNotExists(UUID subscriberId, Long subscribedObjectId, Long subjectId,
            Long consolidationMethodId) {
        if (!doesSubscriptionExist(subscriberId, subscribedObjectId, subjectId)) {
            SubscriptionEntity subscriptionToCreate = new SubscriptionEntity();
            subscriptionToCreate.setSubscribedObjectId(subscribedObjectId);
            subscriptionToCreate.setSubscriberId(subscriberId);
            subscriptionToCreate.setSubscriptionConsolidationMethodId(consolidationMethodId);
            subscriptionToCreate.setSubscriptionSubjectId(subjectId);
            create(subscriptionToCreate);
        } else {
            LOGGER.info("A subscription for subscriber " + subscriberId + ", subjectID "
                    + subjectId + ", and object ID " + subscribedObjectId + " already exists"
                    + "and will not be created again.");
        }
    }

    private boolean doesSubscriptionExist(UUID subscriberId, Long subscribedObjectId, Long subjectId) {
        Query query = entityManager.createQuery("SELECT subscription "
                + "FROM SubscriptionEntity subscription "
                + "WHERE subscription.subscriberId = :subscriberId "
                + "AND subscription.subscribedObjectId = :subscribedObjectId "
                + "AND subscription.subscriptionSubjectId = :subscriptionSubjectId "
                + "AND subscription.deleted = false ");
        query.setParameter("subscriberId", subscriberId);
        query.setParameter("subscribedObjectId", subscribedObjectId);
        query.setParameter("subscriptionSubjectId", subjectId);

        List<SubscriptionEntity> results = query.getResultList();
        return results != null && results.size() > 0;
    }

    public Subscription getSubscriptionById(Long subscriptionId) {
        Query query = entityManager.createQuery(SUBSCRIPTION_HQL
                + "AND susbscription.id = :subscriptionId",
                SubscriptionEntity.class);
        query.setParameter("subscriptionId", subscriptionId);

        List<SubscriptionEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.get(0).toDomain();
    }

    public List<Subscription> getSubscriptionsForSubscriber(UUID subscriberId) {
        Query query = entityManager.createQuery(SUBSCRIPTION_HQL
                + "AND subscriber.id = :subscriberId",
                SubscriptionEntity.class);
        query.setParameter("subscriberId", subscriberId);

        List<SubscriptionEntity> results = query.getResultList();
        return results.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public void deleteSubscriptions(UUID subscriberId) {
        entityManager.createQuery(
                "UPDATE SubscriptionEntity sub "
                + "SET sub.deleted = true "
                + "WHERE sub.subscriberId = :subscriberId")
        .setParameter("subscriberId", subscriberId)
        .executeUpdate();
    }

    public void deleteSubscription(Long subscriptionId) {
        entityManager.createQuery(
                "UPDATE SubscriptionEntity sub "
                + "SET sub.deleted = true "
                + "WHERE sub.id = :subscriptionId")
        .setParameter("subscriptionId", subscriptionId)
        .executeUpdate();
    }

    public void deleteSubscriptions(UUID subscriberId, Long subscribedObjectTypeId, Long subscribedObjectId) {
        Query query = entityManager.createQuery("SELECT subscription "
                + "FROM SubscriptionEntity subscription "
                + "JOIN subscription.subscriptionSubject subject "
                + "JOIN subject.subscriptionObjectType objType "
                + "WHERE subscription.subscriberId = :subscriberId "
                + "AND subscription.subscribedObjectId = :subscribedObjectId "
                + "AND objType.id = :subscribedObjectTypeId "
                + "AND subscription.deleted = false ",
                SubscriptionEntity.class);
        query.setParameter("subscriberId", subscriberId);
        query.setParameter("subscribedObjectId", subscribedObjectId);
        query.setParameter("subscribedObjectTypeId", subscribedObjectTypeId);
        List<SubscriptionEntity> results = query.getResultList();

        if (!CollectionUtils.isEmpty(results)) {
            entityManager.createQuery(
                    "UPDATE SubscriptionEntity sub "
                    + "SET sub.deleted = true "
                    + "WHERE sub.id IN (:subscriptionIds)")
            .setParameter("subscriptionIds", results.stream().map(result -> result.getId()).toList())
            .executeUpdate();
        }
    }

    public List<Long> getSubscriptionIdsForConfirmedSubscribers(Long subjectId, Long subscribedObjectId) {
        Query query = entityManager.createQuery("SELECT subscription "
                + "FROM SubscriptionEntity subscription "
                + "JOIN FETCH subscription.subscriber subscriber "
                + "JOIN FETCH subscriber.subscriberStatus subscriberStatus "
                + "WHERE subscription.subscribedObjectId = :subscribedObjectId "
                + "AND subscription.subscriptionSubjectId = :subscriptionSubjectId "
                + "AND subscriberStatus.id = :confirmedSubscriberStatusId "
                + "AND subscription.deleted = false "
                + "AND subscriber.deleted = false ");
        query.setParameter("confirmedSubscriberStatusId", lookupUtil.getConfirmedSubscriberStatusId());
        query.setParameter("subscribedObjectId", subscribedObjectId);
        query.setParameter("subscriptionSubjectId", subjectId);

        List<SubscriptionEntity> results = query.getResultList();
        return results.stream()
            .map(result -> result.getId())
            .collect(Collectors.toList());
    }

    public List<SubscriptionSearchResult> getAllSubscriptions() {
        Query query = entityManager.createQuery("SELECT srs "
                + "FROM SubscriptionSearchResultEntity srs ", SubscriptionSearchResultEntity.class);

        List<SubscriptionSearchResultEntity> results = query.getResultList();
        return results.stream()
            .map(result -> result.toDomain())
            .collect(Collectors.toList());
    }
}
