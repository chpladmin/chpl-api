package gov.healthit.chpl.subscription.dao;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.subscription.domain.SubscriptionReason;
import gov.healthit.chpl.subscription.entity.SubscriptionReasonEntity;

@Service
public class SubscriptionDao extends BaseDAOImpl {

    public List<SubscriptionReason> getAllReasons() {
        Query query = entityManager.createQuery("SELECT reasons "
                + "FROM SubscriptionReasonEntity reasons "
                + "ORDER BY sortOrder",
                SubscriptionReasonEntity.class);

        List<SubscriptionReasonEntity> results = query.getResultList();
        return results.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }
}
