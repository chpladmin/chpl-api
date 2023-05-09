package gov.healthit.chpl.scheduler.job.onetime.questionableActivity;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.questionableactivity.QuestionableActivityDAO;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityDeveloper;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityProduct;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityVersion;
import gov.healthit.chpl.questionableactivity.listing.DeletedCertificationsActivity;
import gov.healthit.chpl.questionableactivity.listing.NonActiveCertificateEdited;
import gov.healthit.chpl.questionableactivity.listing.UpdatedCertificationDateActivity;
import gov.healthit.chpl.questionableactivity.listing.UpdatedCertificationStatusHistoryActivity;
import gov.healthit.chpl.util.DateUtil;
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

    @Autowired
    private QuestionableActivityReprocessor reprocessor;

    @Autowired
    private DeletedCertificationsActivity deletedCertificationsActivity;

    @Autowired
    private NonActiveCertificateEdited nonActiveCertificateEditedActivity;

    @Autowired
    private UpdatedCertificationDateActivity updateCertificationDateActivity;

    @Autowired
    private UpdatedCertificationStatusHistoryActivity updatedCertificationStatusHistoryActivity;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Fixup Questionable Activity job. *********");
        LocalDateTime startDt = LocalDateTime.parse(START_DATE_STR);
        LocalDateTime endDt = LocalDateTime.now();

        reprocessor.reprocess(QuestionableActivityTriggerConcept.CRITERIA_REMOVED, deletedCertificationsActivity, startDt, endDt, true);
        reprocessor.reprocess(QuestionableActivityTriggerConcept.NON_ACTIVE_CERTIFIFCATE_EDITED, nonActiveCertificateEditedActivity, startDt, endDt, false);
        reprocessor.reprocess(QuestionableActivityTriggerConcept.CERTIFICATION_DATE_EDITED, updateCertificationDateActivity,
                startDt, endDt, true);
        reprocessor.reprocess(QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_HISTORY, updatedCertificationStatusHistoryActivity,
                startDt, endDt, false);

        linkQuestionableActivityToActivity(startDt, endDt);

        LOGGER.info("********* Completed the Fixup Questionable Activity job. *********");
    }

    private void linkQuestionableActivityToActivity(LocalDateTime startDt, LocalDateTime endDt) {
        LOGGER.info("Linking Activity to Listing Questionable Activity");
        List<QuestionableActivityListingDTO> listingQuestionableActivity
            = questionableActivityDao.findListingActivityBetweenDates(DateUtil.toDate(startDt), DateUtil.toDate(endDt));
        listingQuestionableActivity.stream()
            .filter(lqa -> lqa.getActivityId() == null)
            .peek(lqa -> LOGGER.info("Looking for listing activities on " + lqa.getActivityDate() + " for listing " + lqa.getListingId()
                    + ". Listing Questionable Activity ID " + lqa.getId()))
            .forEach(lqa -> addActivityId(lqa));

        LOGGER.info("Linking Activity to Certification Result Questionable Activity");
        List<QuestionableActivityCertificationResultDTO> certResultQuestionableActivity
            = questionableActivityDao.findCertificationResultActivityBetweenDates(DateUtil.toDate(startDt), DateUtil.toDate(endDt));
        certResultQuestionableActivity.stream()
            .filter(crqa -> crqa.getActivityId() == null)
            .peek(crqa -> LOGGER.info("Looking for listing activities on " + crqa.getActivityDate() + " for listing " + crqa.getListing().getId()
                    + ". Certification Result Questionable Activity ID " + crqa.getId()))
            .forEach(crqa -> addActivityId(crqa));

        LOGGER.info("Linking Activity to Developer Questionable Activity");
        List<QuestionableActivityDeveloper> developerQuestionableActivity
            = questionableActivityDao.findDeveloperActivityBetweenDates(DateUtil.toDate(startDt), DateUtil.toDate(endDt));
        developerQuestionableActivity.stream()
            .filter(dqa -> dqa.getActivityId() == null)
            .peek(dqa -> LOGGER.info("Looking for developer activities on " + dqa.getActivityDate() + " for developer " + dqa.getDeveloperId()
                    + ". Developer Questionable Activity ID " + dqa.getId()))
            .forEach(dqa -> addActivityId(dqa));

        LOGGER.info("Linking Activity to Product Questionable Activity");
        List<QuestionableActivityProduct> productQuestionableActivity
            = questionableActivityDao.findProductActivityBetweenDates(DateUtil.toDate(startDt), DateUtil.toDate(endDt));
        productQuestionableActivity.stream()
            .filter(pqa -> pqa.getActivityId() == null)
            .peek(pqa -> LOGGER.info("Looking for product activities on " + pqa.getActivityDate() + " for product " + pqa.getProductId()
                    + ". Product Questionable Activity ID " + pqa.getId()))
            .forEach(pqa -> addActivityId(pqa));

        LOGGER.info("Linking Activity to Version Questionable Activity");
        List<QuestionableActivityVersion> versionQuestionableActivity
            = questionableActivityDao.findVersionActivityBetweenDates(DateUtil.toDate(startDt), DateUtil.toDate(endDt));
        versionQuestionableActivity.stream()
            .filter(vqa -> vqa.getActivityId() == null)
            .peek(vqa -> LOGGER.info("Looking for version activities on " + vqa.getActivityDate() + " for version " + vqa.getVersionId()
                    + ". Version Questionable Activity ID " + vqa.getId()))
            .forEach(vqa -> addActivityId(vqa));
    }

    private void addActivityId(QuestionableActivityListingDTO questionableActivity) {
        ActivityConcept concept = ActivityConcept.CERTIFIED_PRODUCT;
        ActivityDTO activity = getActivity(concept, questionableActivity.getActivityDate(), questionableActivity.getListingId());
        if (activity != null) {
                updatableQuestionableActivityDao.updateActivityIdForListingQuestionableActivity(
                        questionableActivity.getId(), activity.getId());
        }
    }

    private void addActivityId(QuestionableActivityCertificationResultDTO questionableActivity) {
        ActivityConcept concept = ActivityConcept.CERTIFIED_PRODUCT;
        ActivityDTO activity = getActivity(concept, questionableActivity.getActivityDate(), questionableActivity.getListing().getId());
        if (activity != null) {
                updatableQuestionableActivityDao.updateActivityIdForCertificationResultQuestionableActivity(
                        questionableActivity.getId(), activity.getId());
        }
    }

    private void addActivityId(QuestionableActivityDeveloper questionableActivity) {
        ActivityConcept concept = ActivityConcept.DEVELOPER;
        ActivityDTO activity = getActivity(concept, questionableActivity.getActivityDate(), questionableActivity.getDeveloperId());
        if (activity != null) {
                updatableQuestionableActivityDao.updateActivityIdForDeveloperQuestionableActivity(
                        questionableActivity.getId(), activity.getId());
        }
    }

    private void addActivityId(QuestionableActivityProduct questionableActivity) {
        ActivityConcept concept = ActivityConcept.PRODUCT;
        ActivityDTO activity = getActivity(concept, questionableActivity.getActivityDate(), questionableActivity.getProductId());
        if (activity != null) {
                updatableQuestionableActivityDao.updateActivityIdForProductQuestionableActivity(
                        questionableActivity.getId(), activity.getId());
        }
    }

    private void addActivityId(QuestionableActivityVersion questionableActivity) {
        ActivityConcept concept = ActivityConcept.VERSION;
        ActivityDTO activity = getActivity(concept, questionableActivity.getActivityDate(), questionableActivity.getVersionId());
        if (activity != null) {
                updatableQuestionableActivityDao.updateActivityIdForVersionQuestionableActivity(
                        questionableActivity.getId(), activity.getId());
        }
    }

    private ActivityDTO getActivity(ActivityConcept concept, Date activityDate, Long objectId) {
        ActivityDTO activity = null;
        //Check activity for the whole second.
        //For various periods of time we have not recorded the Questionable Activity date as the same
        //Activity date. We sometimes had code to use the Activity date and other times had code that used
        //new Date() as the Questionable Activity date so it would be milliseconds off of the Activity date.
        //I think querying for activity events during the same second is acceptable especially since
        //we are not proceeding if there is more than 1 match.
        //The results are MUCH better when doing it this way.
        LocalDateTime oneSecondAfterActivityDate = DateUtil.toLocalDateTime(activityDate.getTime()).plusSeconds(1);
        LocalDateTime oneSecondBeforeActivityDate = DateUtil.toLocalDateTime(activityDate.getTime()).minusSeconds(1);

        List<ActivityDTO> activitiesOnDate = activityDao.findByConcept(concept, DateUtil.toDate(oneSecondBeforeActivityDate),
                DateUtil.toDate(oneSecondAfterActivityDate));
        if (CollectionUtils.isEmpty(activitiesOnDate)) {
            LOGGER.warn("No " + concept + " activities were found between " + oneSecondBeforeActivityDate
                    + " and " + oneSecondAfterActivityDate);
        } else {
            List<ActivityDTO> activitiesForObjectOnDate = activitiesOnDate.stream()
                .filter(actForObj -> actForObj.getActivityObjectId().equals(objectId))
                .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(activitiesForObjectOnDate)) {
                LOGGER.warn("No " + concept + " activities were found for " + concept + " "
                        + objectId + " between " + oneSecondBeforeActivityDate + " and "
                        + oneSecondAfterActivityDate);
            } else if (activitiesForObjectOnDate.size() > 1) {
                LOGGER.warn("Multiple " + concept + " activities were found for " + concept + " "
                        + objectId + " between " + oneSecondBeforeActivityDate + " and "
                        + oneSecondAfterActivityDate + "."
                        + " [" + activitiesForObjectOnDate.stream().map(act -> act.getId() + "")
                            .collect(Collectors.joining(",")) + "]");
            } else {
                activity = activitiesForObjectOnDate.get(0);
                LOGGER.info("Found activity ID " + activity.getId() + " for " + concept + " "
                        + objectId + " between " + oneSecondBeforeActivityDate + " and " + oneSecondAfterActivityDate);
            }
        }
        return activity;
    }
}
