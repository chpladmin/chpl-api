package gov.healthit.chpl.scheduler.job.onetime.questionableActivity;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.questionableactivity.QuestionableActivityDAO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityProductDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityVersionDTO;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityCertificationResultEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityDeveloperEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityListingEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityProductEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityVersionEntity;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.UserMapper;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "fixupQuestionableActivityJobLogger")
public class FixupQuestionableActivity  implements Job {
    private static final String START_DATE_STR = "2022-01-01T00:00:00";

    @Autowired
    private QuestionableActivityDAO questionableActivityDao;

    @Autowired
    @Qualifier("updatableQuestionableActivityDao")
    private UpdatableQuestionableActivityDao updatableQuestionableActivityDao;

    @Autowired
    @Qualifier("transactionalActivityDao")
    private TransactionalActivityDao activityDao;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Fixup Questionable Activity job. *********");
        LocalDateTime startDt = LocalDateTime.parse(START_DATE_STR);

        linkQuestionableActivityToActivity(startDt);

        LOGGER.info("********* Completed the Fixup Questionable Activity job. *********");
    }

    private void linkQuestionableActivityToActivity(LocalDateTime startDt) {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);

        LOGGER.info("Linking Activity to Listing Questionable Activity");
        List<QuestionableActivityListingDTO> listingQuestionableActivity
            = questionableActivityDao.findListingActivityBetweenDates(DateUtil.toDate(startDt), DateUtil.toDate(tomorrow));
        listingQuestionableActivity.stream()
            .filter(lqa -> lqa.getActivityId() == null)
            .peek(lqa -> LOGGER.info("Looking for listing activities on " + lqa.getActivityDate() + " for listing " + lqa.getListingId()
                    + ". Listing Questionable Activity ID " + lqa.getId()))
            .forEach(lqa -> addActivityIdIfNotPresent(lqa));

        LOGGER.info("Linking Activity to Certification Result Questionable Activity");
        List<QuestionableActivityCertificationResultDTO> certResultQuestionableActivity
            = questionableActivityDao.findCertificationResultActivityBetweenDates(DateUtil.toDate(startDt), DateUtil.toDate(tomorrow));
        certResultQuestionableActivity.stream()
            .filter(crqa -> crqa.getActivityId() == null)
            .peek(crqa -> LOGGER.info("Looking for listing activities on " + crqa.getActivityDate() + " for listing " + crqa.getListing().getId()
                    + ". Certification Result Questionable Activity ID " + crqa.getId()))
            .forEach(crqa -> addActivityIdIfNotPresent(crqa));

        LOGGER.info("Linking Activity to Developer Questionable Activity");
        List<QuestionableActivityDeveloperDTO> developerQuestionableActivity
            = questionableActivityDao.findDeveloperActivityBetweenDates(DateUtil.toDate(startDt), DateUtil.toDate(tomorrow));
        developerQuestionableActivity.stream()
            .filter(dqa -> dqa.getActivityId() == null)
            .peek(dqa -> LOGGER.info("Looking for developer activities on " + dqa.getActivityDate() + " for developer " + dqa.getDeveloperId()
                    + ". Developer Questionable Activity ID " + dqa.getId()))
            .forEach(dqa -> addActivityIdIfNotPresent(dqa));

        LOGGER.info("Linking Activity to Product Questionable Activity");
        List<QuestionableActivityProductDTO> productQuestionableActivity
            = questionableActivityDao.findProductActivityBetweenDates(DateUtil.toDate(startDt), DateUtil.toDate(tomorrow));
        productQuestionableActivity.stream()
            .filter(pqa -> pqa.getActivityId() == null)
            .peek(pqa -> LOGGER.info("Looking for product activities on " + pqa.getActivityDate() + " for product " + pqa.getProductId()
                    + ". Product Questionable Activity ID " + pqa.getId()))
            .forEach(pqa -> addActivityIdIfNotPresent(pqa));

        LOGGER.info("Linking Activity to Version Questionable Activity");
        List<QuestionableActivityVersionDTO> versionQuestionableActivity
            = questionableActivityDao.findVersionActivityBetweenDates(DateUtil.toDate(startDt), DateUtil.toDate(tomorrow));
        versionQuestionableActivity.stream()
            .filter(vqa -> vqa.getActivityId() == null)
            .peek(vqa -> LOGGER.info("Looking for version activities on " + vqa.getActivityDate() + " for version " + vqa.getVersionId()
                    + ". Version Questionable Activity ID " + vqa.getId()))
            .forEach(vqa -> addActivityIdIfNotPresent(vqa));
    }

    private void addActivityIdIfNotPresent(QuestionableActivityListingDTO questionableActivity) {
        ActivityConcept concept = ActivityConcept.CERTIFIED_PRODUCT;
        ActivityDTO activity = getActivity(concept, questionableActivity.getActivityDate(), questionableActivity.getListingId());
        if (activity != null) {
                updatableQuestionableActivityDao.updateActivityIdForListingQuestionableActivity(
                        questionableActivity.getId(), activity.getId());
        }
    }

    private void addActivityIdIfNotPresent(QuestionableActivityCertificationResultDTO questionableActivity) {
        ActivityConcept concept = ActivityConcept.CERTIFIED_PRODUCT;
        ActivityDTO activity = getActivity(concept, questionableActivity.getActivityDate(), questionableActivity.getListing().getId());
        if (activity != null) {
                updatableQuestionableActivityDao.updateActivityIdForCertificationResultQuestionableActivity(
                        questionableActivity.getId(), activity.getId());
        }
    }

    private void addActivityIdIfNotPresent(QuestionableActivityDeveloperDTO questionableActivity) {
        ActivityConcept concept = ActivityConcept.DEVELOPER;
        ActivityDTO activity = getActivity(concept, questionableActivity.getActivityDate(), questionableActivity.getDeveloperId());
        if (activity != null) {
                updatableQuestionableActivityDao.updateActivityIdForDeveloperQuestionableActivity(
                        questionableActivity.getId(), activity.getId());
        }
    }

    private void addActivityIdIfNotPresent(QuestionableActivityProductDTO questionableActivity) {
        ActivityConcept concept = ActivityConcept.PRODUCT;
        ActivityDTO activity = getActivity(concept, questionableActivity.getActivityDate(), questionableActivity.getProductId());
        if (activity != null) {
                updatableQuestionableActivityDao.updateActivityIdForProductQuestionableActivity(
                        questionableActivity.getId(), activity.getId());
        }
    }

    private void addActivityIdIfNotPresent(QuestionableActivityVersionDTO questionableActivity) {
        ActivityConcept concept = ActivityConcept.VERSION;
        ActivityDTO activity = getActivity(concept, questionableActivity.getActivityDate(), questionableActivity.getVersionId());
        if (activity != null) {
                updatableQuestionableActivityDao.updateActivityIdForVersionQuestionableActivity(
                        questionableActivity.getId(), activity.getId());
        }
    }

    private ActivityDTO getActivity(ActivityConcept concept, Date activityDate, Long objectId) {
        ActivityDTO activity = null;
        List<ActivityDTO> activitiesOnDate = activityDao.findByConcept(concept, activityDate, activityDate);
        if (CollectionUtils.isEmpty(activitiesOnDate)) {
            LOGGER.info("No " + concept + " activities were found on " + activityDate);
        } else {
            List<ActivityDTO> activitiesForObjectOnDate = activitiesOnDate.stream()
                .filter(actForObj -> actForObj.getActivityObjectId().equals(objectId))
                .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(activitiesForObjectOnDate)) {
                LOGGER.info("No " + concept + " activities were found for " + concept + " "
                        + objectId + " on " + activityDate);
            } else if (activitiesForObjectOnDate.size() > 1) {
                LOGGER.info("Multiple " + concept + " activities were found for " + concept + " "
                        + objectId + " on " + activityDate + "."
                        + " [" + activitiesForObjectOnDate.stream().map(act -> act.getId() + "")
                            .collect(Collectors.joining(",")) + "]");
            } else {
                activity = activitiesForObjectOnDate.get(0);
            }
        }
        return activity;
    }

    @Component("transactionalActivityDao")
    private static class TransactionalActivityDao extends ActivityDAO {

        @Autowired
        TransactionalActivityDao(UserMapper userMapper) {
            super(userMapper);
        }

        @Transactional
        @Override
        public List<ActivityDTO> findByConcept(ActivityConcept concept, Date startDate, Date endDate) {
            return super.findByConcept(concept, startDate, endDate);
        }
    }

    @Component("updatableQuestionableActivityDao")
    @NoArgsConstructor
    private static class UpdatableQuestionableActivityDao extends BaseDAOImpl {

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
}
