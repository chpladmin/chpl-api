package gov.healthit.chpl.questionableactivity.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.questionableactivity.QuestionableActivityDAO;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.certificationResult.CertificationResultActivity;
import gov.healthit.chpl.questionableactivity.certificationResult.RemovedFunctionalityTestedAddedActivity;
import gov.healthit.chpl.questionableactivity.certificationResult.RemovedTestToolAddedActivity;
import gov.healthit.chpl.questionableactivity.certificationResult.ReplacedSvapAddedActivity;
import gov.healthit.chpl.questionableactivity.certificationResult.UpdatedG1SuccessActivity;
import gov.healthit.chpl.questionableactivity.certificationResult.UpdatedG2SuccessActivity;
import gov.healthit.chpl.questionableactivity.certificationResult.UpdatedGapActivity;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityTrigger;
import lombok.extern.log4j.Log4j2;

/**
 * Checker for certification criteria questionable activity.
 */
@Log4j2
@Component
public class CertificationResultQuestionableActivityService {

    private QuestionableActivityDAO questionableActivityDao;
    private List<CertificationResultActivity> certResultActivities;
    private List<QuestionableActivityTrigger> triggerTypes;

    @Autowired
    CertificationResultQuestionableActivityService(QuestionableActivityDAO questionableActivityDao,
            List<CertificationResultActivity> certResultActivities) {
        this.questionableActivityDao = questionableActivityDao;
        this.certResultActivities = certResultActivities;
        triggerTypes = questionableActivityDao.getAllTriggers();
    }

    public void processQuestionableActivity(CertificationResult origCertResult,
            CertificationResult newCertResult,
            ActivityDTO activity, String activityReason) {
        processCertificationResultActivity(UpdatedG1SuccessActivity.class.getName(), origCertResult, newCertResult, activity, activityReason);
        processCertificationResultActivity(UpdatedG2SuccessActivity.class.getName(), origCertResult, newCertResult, activity, activityReason);
        processCertificationResultActivity(UpdatedGapActivity.class.getName(), origCertResult, newCertResult, activity, activityReason);
        processCertificationResultActivity(ReplacedSvapAddedActivity.class.getName(), origCertResult, newCertResult, activity, activityReason);
        processCertificationResultActivity(RemovedTestToolAddedActivity.class.getName(), origCertResult, newCertResult, activity, activityReason);
        processCertificationResultActivity(RemovedFunctionalityTestedAddedActivity.class.getName(), origCertResult, newCertResult, activity, activityReason);
        //TODO: Add activity to detect removed Standard added with OCD-4333
    }

    private Integer processCertificationResultActivity(String className, CertificationResult origCertResult,
            CertificationResult newCertResult,
            ActivityDTO activity, String activityReason) {
         Integer activitiesCreated = 0;
         Optional<CertificationResultActivity> certResultActivity = getCertificationResultActivity(className);
         if (!certResultActivity.isPresent()) {
             LOGGER.error("Could not find class: " + className);
         } else {
             List<QuestionableActivityCertificationResult> activities = certResultActivity.get().check(origCertResult, newCertResult);
             if (activities != null && activities.size() > 0) {
                 for (QuestionableActivityCertificationResult questAct : activities) {
                     if (questAct != null) {
                         createCertificationResultActivity(questAct, origCertResult.getId(),
                                 certResultActivity.get().getTriggerType(), activity, activityReason);
                     }
                 }
             }
         }
         return activitiesCreated;
     }

     private void createCertificationResultActivity(QuestionableActivityCertificationResult questionableActivity,
             Long certResultId, QuestionableActivityTriggerConcept triggerConcept,
             ActivityDTO activity, String activityReason) {
         questionableActivity.setActivityId(activity.getId());
         questionableActivity.setCertResultId(certResultId);
         questionableActivity.setActivityDate(activity.getActivityDate());
         questionableActivity.setUserId(activity.getUser().getId());
         questionableActivity.setReason(activityReason);
         QuestionableActivityTrigger trigger = getTrigger(triggerConcept);
         questionableActivity.setTrigger(trigger);
         questionableActivityDao.create(questionableActivity);
     }

     private QuestionableActivityTrigger getTrigger(QuestionableActivityTriggerConcept triggerConcept) {
         QuestionableActivityTrigger result = null;
         for (QuestionableActivityTrigger currTrigger : triggerTypes) {
             if (triggerConcept.getName().equalsIgnoreCase(currTrigger.getName())) {
                 result = currTrigger;
             }
         }
         return result;
     }

     private Optional<CertificationResultActivity> getCertificationResultActivity(String className) {
         return certResultActivities.stream()
                 .filter(la -> la.getClass().getName().equals(className))
                 .findAny();
     }
}
