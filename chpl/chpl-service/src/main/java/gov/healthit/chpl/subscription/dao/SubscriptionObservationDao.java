package gov.healthit.chpl.subscription.dao;

import java.util.List;

import org.springframework.stereotype.Service;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.subscription.entity.SubscriptionObservationEntity;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class SubscriptionObservationDao extends BaseDAOImpl {

    public void create(List<Long> subscriptionIds, Long activityId) {
        //batch insert observations to keep performance reasonable if we get many subscriptions
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
}
