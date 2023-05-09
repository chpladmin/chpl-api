package gov.healthit.chpl.scheduler.job.onetime.questionableActivity;

import java.util.Date;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityTrigger;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityCertificationResultEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityDeveloperEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityListingEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityProductEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityVersionEntity;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component("updatableQuestionableActivityDao")
@NoArgsConstructor
@Log4j2(topic = "fixupQuestionableActivityJobLogger")
public class UpdatableQuestionableActivityDao extends BaseDAOImpl {

    @Transactional
    public void deleteQuestionableActivityForTriggerBetweenDates(QuestionableActivityTrigger trigger, Date startDate, Date endDate) {
        LOGGER.info("Deleting " + trigger.getName() + " questionable activities since " + startDate);
        String hql = "UPDATE QuestionableActivityListingEntity "
                + "SET deleted = true "
                + "WHERE triggerId = :triggerId "
                + "AND activityDate > :startDate "
                + "AND activityDate < :endDate";
        Query query = entityManager.createQuery(hql);
        query.setParameter("triggerId", trigger.getId());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        int numItemsUpdated = query.executeUpdate();
        LOGGER.info("Marked " + numItemsUpdated + " questionable activities as deleted.");
    }

    @Transactional
    public void updateActivityIdForListingQuestionableActivity(Long questionableActivityId, Long activityId) {
        LOGGER.info("Setting activity ID " + activityId + " for questionable activity " + questionableActivityId);
        QuestionableActivityListingEntity entity
            = entityManager.find(QuestionableActivityListingEntity.class, questionableActivityId);
        if (entity == null) {
            LOGGER.error("No listing questionable activity found with ID " + questionableActivityId);
        } else {
            entity.setActivityId(activityId);
            update(entity);
        }
    }

    @Transactional
    public void updateActivityIdForCertificationResultQuestionableActivity(Long questionableActivityId, Long activityId) {
        LOGGER.info("Setting activity ID " + activityId + " for questionable activity " + questionableActivityId);
        QuestionableActivityCertificationResultEntity entity
            = entityManager.find(QuestionableActivityCertificationResultEntity.class, questionableActivityId);
        if (entity == null) {
            LOGGER.error("No certification result questionable activity found with ID " + questionableActivityId);
        } else {
            entity.setActivityId(activityId);
            update(entity);
        }
    }

    @Transactional
    public void updateActivityIdForDeveloperQuestionableActivity(Long questionableActivityId, Long activityId) {
        LOGGER.info("Setting activity ID " + activityId + " for questionable activity " + questionableActivityId);
        QuestionableActivityDeveloperEntity entity
            = entityManager.find(QuestionableActivityDeveloperEntity.class, questionableActivityId);
        if (entity == null) {
            LOGGER.error("No developer questionable activity found with ID " + questionableActivityId);
        } else {
            entity.setActivityId(activityId);
            update(entity);
        }
    }

    @Transactional
    public void updateActivityIdForProductQuestionableActivity(Long questionableActivityId, Long activityId) {
        LOGGER.info("Setting activity ID " + activityId + " for questionable activity " + questionableActivityId);
        QuestionableActivityProductEntity entity
            = entityManager.find(QuestionableActivityProductEntity.class, questionableActivityId);
        if (entity == null) {
            LOGGER.error("No product questionable activity found with ID " + questionableActivityId);
        } else {
            entity.setActivityId(activityId);
            update(entity);
        }
    }

    @Transactional
    public void updateActivityIdForVersionQuestionableActivity(Long questionableActivityId, Long activityId) {
        LOGGER.info("Setting activity ID " + activityId + " for questionable activity " + questionableActivityId);
        QuestionableActivityVersionEntity entity
            = entityManager.find(QuestionableActivityVersionEntity.class, questionableActivityId);
        if (entity == null) {
            LOGGER.error("No version questionable activity found with ID " + questionableActivityId);
        } else {
            entity.setActivityId(activityId);
            update(entity);
        }
    }
}
