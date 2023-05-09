package gov.healthit.chpl.scheduler.job.onetime.questionableActivity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.questionableactivity.QuestionableActivityDAO;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityTrigger;
import gov.healthit.chpl.questionableactivity.listing.ListingActivity;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.JSONUtils;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "fixupQuestionableActivityJobLogger")
public class QuestionableActivityReprocessor {
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

    private TransactionalActivityDao activityDao;
    private QuestionableActivityDAO questionableActivityDao;
    private UpdatableQuestionableActivityDao updatableQuestionableActivityDao;
    private CertifiedProductDAO certifiedProductDao;
    private Long questionableActivityThresholdDays;
    private List<QuestionableActivityTrigger> triggerTypes;

    @Autowired
    public QuestionableActivityReprocessor(@Qualifier("transactionalActivityDao") TransactionalActivityDao activityDao,
            QuestionableActivityDAO questionableActivityDao,
            @Qualifier("updatableQuestionableActivityDao") UpdatableQuestionableActivityDao updatableQuestionableActivityDao,
            CertifiedProductDAO certifiedProductDao,
            @Value("${questionableActivityThresholdDays}") Long questionableActivityThresholdDays) {
        this.activityDao = activityDao;
        this.questionableActivityDao = questionableActivityDao;
        this.updatableQuestionableActivityDao = updatableQuestionableActivityDao;
        this.certifiedProductDao = certifiedProductDao;
        this.questionableActivityThresholdDays = questionableActivityThresholdDays;
        triggerTypes = questionableActivityDao.getAllTriggers();
    }

    public void reprocess(QuestionableActivityTriggerConcept trigger, ListingActivity activityChecker,
            LocalDateTime since, LocalDateTime until, boolean requiresThreshold) {
        LOGGER.info("Reprocessing all activity for " + trigger.getName() + " between " + since + " and " + until);
        QuestionableActivityTrigger triggerDto = getTrigger(trigger);
        List<QuestionableActivityListingDTO> allQuestionableActivity = questionableActivityDao.findListingActivityBetweenDates(
                DateUtil.toDate(since), DateUtil.toDate(until));

        List<QuestionableActivityListingDTO> previouslyExistingQuestionableActivity = allQuestionableActivity.stream()
            .filter(act -> act.getTrigger().getId().equals(triggerDto.getId()))
            .collect(Collectors.toList());

        updatableQuestionableActivityDao.deleteQuestionableActivityForTriggerBetweenDates(triggerDto, DateUtil.toDate(since), DateUtil.toDate(until));

        //Re-process all listing activity from startDt onward for this type of trigger
        //Getting all listing activity at once results in OutOfMemory error so I have to page through it...
        int pageNum = 0;
        int pageSize = 20;
        Long totalActivities = activityDao.findResultSetSizeByConcept(ActivityConcept.CERTIFIED_PRODUCT, DateUtil.toDate(since), DateUtil.toDate(until));
        LOGGER.info("Total # activites to reprocess: " + totalActivities);
        List<ActivityDTO> currPageOfActivities = activityDao.findPageByConcept(ActivityConcept.CERTIFIED_PRODUCT, DateUtil.toDate(since),
                DateUtil.toDate(until), pageNum, pageSize);
        while (!CollectionUtils.isEmpty(currPageOfActivities)) {
            LOGGER.info("Processing page " + pageNum + " of activities. Activity IDs: ["
                    + currPageOfActivities.stream().map(act -> act.getId() + "").collect(Collectors.joining(",")) + "]");
            currPageOfActivities.stream()
                .forEach(listingActivity -> {
                    try {
                        reprocessListingActivityForTrigger(listingActivity, previouslyExistingQuestionableActivity,
                                triggerDto, activityChecker, requiresThreshold);
                    } catch (Exception ex) {
                        LOGGER.error("Error reprocessing listing activity " + listingActivity.getId() + " for " + triggerDto.getName(), ex);
                    }
                });

            pageNum++;
            currPageOfActivities = activityDao.findPageByConcept(ActivityConcept.CERTIFIED_PRODUCT, DateUtil.toDate(since),
                    DateUtil.toDate(until), pageNum, pageSize);
        }
    }

    private void reprocessListingActivityForTrigger(ActivityDTO activity,
            List<QuestionableActivityListingDTO> previouslyExistingQuestionableActivity, QuestionableActivityTrigger trigger,
            ListingActivity activityChecker, boolean requiresThreshold)
            throws IOException {
        LOGGER.info("Reprocessing " + activity.getConcept().name() + " activity for object ID " + activity.getActivityObjectId()
            + " with activity ID " + activity.getId() + " for " + trigger.getName());

        if (StringUtils.isEmpty(activity.getOriginalData())
                || StringUtils.isEmpty(activity.getNewData())) {
            LOGGER.debug("Activity " + activity.getId() + " is not for editing a listing. Nothing to do.");
            return;
        }

        CertifiedProductSearchDetails origListing = JSONUtils.fromJSON(activity.getOriginalData(), CertifiedProductSearchDetails.class);
        CertifiedProductSearchDetails newListing = JSONUtils.fromJSON(activity.getNewData(), CertifiedProductSearchDetails.class);

        Date confirmDate = certifiedProductDao.getConfirmDate(origListing.getId());
        if (!requiresThreshold || isActivityWithinThreshold(confirmDate, newListing.getLastModifiedDate())) {
            List<QuestionableActivityListingDTO> questionableActivities = activityChecker.check(origListing, newListing);
            if (!CollectionUtils.isEmpty(questionableActivities)) {
                LOGGER.info("Inserting " + questionableActivities.size() + " '" + trigger.getName() + "' questionable activities for listing ID "
                        + origListing.getId() + " on " + activity.getActivityDate());
                for (QuestionableActivityListingDTO questionableActivity : questionableActivities) {
                    if (questionableActivity != null) {
                        questionableActivity.setListingId(origListing.getId());
                        questionableActivity.setActivityDate(activity.getActivityDate());
                        questionableActivity.setUserId(activity.getUser().getId());
                        questionableActivity.setActivityDate(activity.getActivityDate());
                        questionableActivity.setActivityId(activity.getId());
                        questionableActivity.setTrigger(trigger);

                        String reason = getReasonIfAvailable(questionableActivity, previouslyExistingQuestionableActivity);
                        questionableActivity.setReason(reason);

                        questionableActivityDao.create(questionableActivity);
                    }
                }
            } else {
                LOGGER.info("No '" + trigger.getName() + "' questionable activities for listing ID " + origListing.getId()
                    + " on " + activity.getActivityDate());
            }
        } else {
            LOGGER.info("Activity is not questionable since it was shortly after listing confirmation.");
        }
    }

    private boolean isActivityWithinThreshold(Date confirmDate, Long lastModifiedDate) {
        return (confirmDate != null && lastModifiedDate != null
                && (lastModifiedDate.longValue() - confirmDate.getTime()
                        > getListingActivityThresholdInMillis()));
    }

    private String getReasonIfAvailable(QuestionableActivityListingDTO questionableActivity, List<QuestionableActivityListingDTO> previousCriteriaAddedQuestionableActivity) {
        //we need to try to match this new questionable activity to one of the old questionable activities
        //to find the "reason". This information is saved nowhere else. Ugh.
        LocalDateTime oneSecondAfterActivityDate = DateUtil.toLocalDateTime(questionableActivity.getActivityDate().getTime()).plusSeconds(1);
        LocalDateTime oneSecondBeforeActivityDate = DateUtil.toLocalDateTime(questionableActivity.getActivityDate().getTime()).minusSeconds(1);

        List<QuestionableActivityListingDTO> matchingPreviousQuestionableActivity
            = previousCriteriaAddedQuestionableActivity.stream()
                .filter(prevQa -> prevQa.getListingId().equals(questionableActivity.getListingId()))
                .filter(prevQa -> prevQa.getActivityDate().after(DateUtil.toDate(oneSecondBeforeActivityDate))
                        && prevQa.getActivityDate().before(DateUtil.toDate(oneSecondAfterActivityDate)))
                .collect(Collectors.toList());
        if (matchingPreviousQuestionableActivity.size() == 0) {
            LOGGER.warn("No matching old questionable activity for listing " + questionableActivity.getListingId() +
                    "between " + oneSecondBeforeActivityDate + " and " + oneSecondAfterActivityDate + ". The 'reason' will be lost.");
            return null;
        } else if (matchingPreviousQuestionableActivity.size() > 1) {
            Set<String> reasons = matchingPreviousQuestionableActivity.stream()
                .map(qa -> qa.getReason())
                .collect(Collectors.toSet());
            if (reasons.size() > 1) {
                LOGGER.warn("Multiple matching old questionable activities with different 'reasons' for listing " + questionableActivity.getListingId() +
                        "between " + oneSecondBeforeActivityDate + " and " + oneSecondAfterActivityDate + ". The 'reason' will be lost.");
                return null;
            } else {
                return reasons.iterator().next();
            }
        }
        return matchingPreviousQuestionableActivity.get(0).getReason();
    }

    private QuestionableActivityTrigger getTrigger(QuestionableActivityTriggerConcept trigger) {
        QuestionableActivityTrigger result = null;
        for (QuestionableActivityTrigger currTrigger : triggerTypes) {
            if (trigger.getName().equalsIgnoreCase(currTrigger.getName())) {
                result = currTrigger;
            }
        }
        return result;
    }

    private Long getListingActivityThresholdInMillis() {
        return questionableActivityThresholdDays * MILLIS_PER_DAY;
    }
}
