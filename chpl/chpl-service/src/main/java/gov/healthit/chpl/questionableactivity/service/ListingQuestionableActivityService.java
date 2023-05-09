package gov.healthit.chpl.questionableactivity.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.questionableactivity.QuestionableActivityDAO;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListing;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityTrigger;
import gov.healthit.chpl.questionableactivity.listing.AddedCqmsActivity;
import gov.healthit.chpl.questionableactivity.listing.AddedRwtPlanNonEligibleListingActivity;
import gov.healthit.chpl.questionableactivity.listing.AddedRwtResultsNonEligibleListingActivity;
import gov.healthit.chpl.questionableactivity.listing.CuresUpdateDesignationRemoved;
import gov.healthit.chpl.questionableactivity.listing.DeletedCertificationsActivity;
import gov.healthit.chpl.questionableactivity.listing.DeletedCqmsActivity;
import gov.healthit.chpl.questionableactivity.listing.DeletedMeasuresActivity;
import gov.healthit.chpl.questionableactivity.listing.DeletedRwtPlanActivity;
import gov.healthit.chpl.questionableactivity.listing.DeletedRwtResultsActivity;
import gov.healthit.chpl.questionableactivity.listing.DeletedSurveillanceActivity;
import gov.healthit.chpl.questionableactivity.listing.ListingActivity;
import gov.healthit.chpl.questionableactivity.listing.NonActiveCertificateEdited;
import gov.healthit.chpl.questionableactivity.listing.RwtPlansUpdatedOutsideNormalPeriod;
import gov.healthit.chpl.questionableactivity.listing.RwtResultsUpdatedOutsideNormalPeriod;
import gov.healthit.chpl.questionableactivity.listing.UpdateCurrentCertificationStatusActivity;
import gov.healthit.chpl.questionableactivity.listing.Updated2011EditionListingActivity;
import gov.healthit.chpl.questionableactivity.listing.Updated2014EditionListingActivity;
import gov.healthit.chpl.questionableactivity.listing.UpdatedCertificationDateActivity;
import gov.healthit.chpl.questionableactivity.listing.UpdatedCertificationStatusHistoryActivity;
import gov.healthit.chpl.questionableactivity.listing.UpdatedCertificationStatusWithdrawnByDeveloperUnderReviewActivity;
import gov.healthit.chpl.questionableactivity.listing.UpdatedPromotingInteroperabilityActivity;
import gov.healthit.chpl.questionableactivity.listing.UpdatedTestingLabActivity;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ListingQuestionableActivityService {
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

    private QuestionableActivityDAO questionableActivityDao;
    private List<ListingActivity> listingActivities;
    private CertifiedProductDAO certifiedProductDAO;
    private Environment env;

    private List<QuestionableActivityTrigger> triggerTypes;

    @Autowired
    ListingQuestionableActivityService(QuestionableActivityDAO questionableActivityDao, List<ListingActivity> listingActivities,
            CertifiedProductDAO certifiedProductDAO, Environment env) {
        this.questionableActivityDao = questionableActivityDao;
        this.listingActivities = listingActivities;
        this.certifiedProductDAO = certifiedProductDAO;
        this.env = env;

        triggerTypes = questionableActivityDao.getAllTriggers();
    }

    public void processQuestionableActivity(CertifiedProductSearchDetails origListing,  CertifiedProductSearchDetails newListing,
            ActivityDTO activity, String activityReason) {
        if (processListingActivity(Updated2011EditionListingActivity.class.getName(), origListing, newListing, activity, activityReason) > 0) {
            return;
        }
        processListingActivity(Updated2014EditionListingActivity.class.getName(), origListing, newListing, activity, activityReason);
        processListingActivity(UpdatedCertificationStatusWithdrawnByDeveloperUnderReviewActivity.class.getName(), origListing, newListing, activity, activityReason);
        processListingActivity(UpdatedCertificationStatusHistoryActivity.class.getName(), origListing, newListing, activity, activityReason);
        processListingActivity(UpdatedTestingLabActivity.class.getName(), origListing, newListing, activity, activityReason);
        processListingActivity(DeletedRwtPlanActivity.class.getName(), origListing, newListing, activity, activityReason);
        processListingActivity(DeletedRwtResultsActivity.class.getName(), origListing, newListing, activity, activityReason);
        processListingActivity(AddedRwtPlanNonEligibleListingActivity.class.getName(), origListing, newListing, activity, activityReason);
        processListingActivity(AddedRwtResultsNonEligibleListingActivity.class.getName(), origListing, newListing, activity, activityReason);
        processListingActivity(UpdatedPromotingInteroperabilityActivity.class.getName(), origListing, newListing, activity, activityReason);
        processListingActivity(DeletedMeasuresActivity.class.getName(), origListing, newListing, activity, activityReason);
        processListingActivity(RwtResultsUpdatedOutsideNormalPeriod.class.getName(), origListing, newListing, activity, activityReason);
        processListingActivity(RwtPlansUpdatedOutsideNormalPeriod.class.getName(), origListing, newListing, activity, activityReason);
        processListingActivity(CuresUpdateDesignationRemoved.class.getName(), origListing, newListing, activity, activityReason);
        processListingActivity(NonActiveCertificateEdited.class.getName(), origListing, newListing, activity, activityReason);

        // finally check for other changes that are only questionable
        // outside of the acceptable activity threshold

        // get the confirm date of the listing to check against the threshold
        Date confirmDate = certifiedProductDAO.getConfirmDate(origListing.getId());
        if (confirmDate != null && activity.getActivityDate() != null
                && (activity.getActivityDate().getTime() - confirmDate.getTime() > getListingActivityThresholdInMillis())) {

            processListingActivity(UpdateCurrentCertificationStatusActivity.class.getName(), origListing, newListing, activity, activityReason);
            processListingActivity(UpdatedCertificationDateActivity.class.getName(), origListing, newListing, activity, activityReason);
            processListingActivity(DeletedSurveillanceActivity.class.getName(), origListing, newListing, activity, activityReason);
            processListingActivity(AddedCqmsActivity.class.getName(), origListing, newListing, activity, activityReason);
            processListingActivity(DeletedCqmsActivity.class.getName(), origListing, newListing, activity, activityReason);
            processListingActivity(DeletedCertificationsActivity.class.getName(), origListing, newListing, activity, activityReason);
        }
    }

    private Integer processListingActivity(String className, CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing,
           ActivityDTO activity, String activityReason) {
        Integer activitiesCreated = 0;
        Optional<ListingActivity> listingActivity = getListingActivity(className);
        if (!listingActivity.isPresent()) {
            LOGGER.error("Could not find class: " + className);
        } else {
            List<QuestionableActivityListing> activities = listingActivity.get().check(origListing, newListing);
            if (activities != null && activities.size() > 0) {
                for (QuestionableActivityListing dto : activities) {
                    if (dto != null) {
                        //Need to get the real user here
                        createListingActivity(dto, origListing.getId(), listingActivity.get().getTriggerType(), activity, activityReason);
                    }
                }
            }
        }
        return activitiesCreated;
    }

    private void createListingActivity(QuestionableActivityListing questionableActivity, Long listingId,
            QuestionableActivityTriggerConcept triggerConcept, ActivityDTO activity, String activityReason) {
        questionableActivity.setListingId(listingId);
        questionableActivity.setUserId(activity.getUser().getId());
        questionableActivity.setActivityDate(activity.getActivityDate());
        questionableActivity.setActivityId(activity.getId());
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

    private Optional<ListingActivity> getListingActivity(String className) {
        return listingActivities.stream()
                .filter(la -> la.getClass().getName().equals(className))
                .findAny();
    }

    private Long getListingActivityThresholdInMillis() {
        String activityThresholdDaysStr = env.getProperty("questionableActivityThresholdDays");
        int activityThresholdDays = Integer.parseInt(activityThresholdDaysStr);
        return activityThresholdDays * MILLIS_PER_DAY;
    }
}
