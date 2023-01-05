package gov.healthit.chpl.questionableactivity;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityTriggerDTO;
import gov.healthit.chpl.questionableactivity.listing.AddedCertificationsActivity;
import gov.healthit.chpl.questionableactivity.listing.AddedCqmsActivity;
import gov.healthit.chpl.questionableactivity.listing.AddedMeasureActivity;
import gov.healthit.chpl.questionableactivity.listing.AddedRwtPlanNonEligibleListingActivity;
import gov.healthit.chpl.questionableactivity.listing.AddedRwtResultsNonEligibleListingActivity;
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
import gov.healthit.chpl.questionableactivity.listing.UpdatedCertificationStatusDate;
import gov.healthit.chpl.questionableactivity.listing.UpdatedCertificationStatusHistoryActivity;
import gov.healthit.chpl.questionableactivity.listing.UpdatedCertificationStatusWithdrawnByDeveloperUnderReviewActivity;
import gov.healthit.chpl.questionableactivity.listing.UpdatedCriteriaB3AndListingHasIcsActivity;
import gov.healthit.chpl.questionableactivity.listing.UpdatedPromotingInteroperabilityActivity;
import gov.healthit.chpl.questionableactivity.listing.UpdatedTestingLabActivity;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ListingQuestionableActivityService {
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

    private QuestionableActivityDAO questionableActivityDao;
    private List<ListingActivity> listingActivities;
    private CertifiedProductDAO certifiedProductDAO;
    private Environment env;

    private List<QuestionableActivityTriggerDTO> triggerTypes;

    @Autowired
    ListingQuestionableActivityService(QuestionableActivityDAO questionableActivityDao, List<ListingActivity> listingActivities,
            CertifiedProductDAO certifiedProductDAO, Environment env) {
        this.questionableActivityDao = questionableActivityDao;
        this.listingActivities = listingActivities;
        this.certifiedProductDAO = certifiedProductDAO;
        this.env = env;

        triggerTypes = questionableActivityDao.getAllTriggers();
    }

    public void processQuestionableActivity(CertifiedProductSearchDetails origListing,  CertifiedProductSearchDetails newListing, String activityReason) {
        if (processListingActivity(Updated2011EditionListingActivity.class.getName(), origListing, newListing, activityReason) > 0) {
            return;
        }
        processListingActivity(Updated2014EditionListingActivity.class.getName(), origListing, newListing, activityReason);
        processListingActivity(UpdatedCertificationStatusWithdrawnByDeveloperUnderReviewActivity.class.getName(), origListing, newListing, activityReason);
        processListingActivity(UpdatedCertificationStatusHistoryActivity.class.getName(), origListing, newListing, activityReason);
        processListingActivity(UpdatedTestingLabActivity.class.getName(), origListing, newListing, activityReason);
        processListingActivity(UpdatedCriteriaB3AndListingHasIcsActivity.class.getName(), origListing, newListing, activityReason);
        processListingActivity(DeletedRwtPlanActivity.class.getName(), origListing, newListing, activityReason);
        processListingActivity(DeletedRwtResultsActivity.class.getName(), origListing, newListing, activityReason);
        processListingActivity(AddedRwtPlanNonEligibleListingActivity.class.getName(), origListing, newListing, activityReason);
        processListingActivity(AddedRwtResultsNonEligibleListingActivity.class.getName(), origListing, newListing, activityReason);
        processListingActivity(UpdatedPromotingInteroperabilityActivity.class.getName(), origListing, newListing, activityReason);
        processListingActivity(AddedMeasureActivity.class.getName(), origListing, newListing, activityReason);
        processListingActivity(DeletedMeasuresActivity.class.getName(), origListing, newListing, activityReason);
        processListingActivity(RwtResultsUpdatedOutsideNormalPeriod.class.getName(), origListing, newListing, activityReason);
        processListingActivity(RwtPlansUpdatedOutsideNormalPeriod.class.getName(), origListing, newListing, activityReason);
        processListingActivity(NonActiveCertificateEdited.class.getName(), origListing, newListing, activityReason);

        // finally check for other changes that are only questionable
        // outside of the acceptable activity threshold

        // get the confirm date of the listing to check against the threshold
        Date confirmDate = certifiedProductDAO.getConfirmDate(origListing.getId());
        if (confirmDate != null && newListing.getLastModifiedDate() != null
                && (newListing.getLastModifiedDate().longValue()
                        - confirmDate.getTime() > getListingActivityThresholdInMillis())) {

            processListingActivity(UpdateCurrentCertificationStatusActivity.class.getName(), origListing, newListing, activityReason);
            processListingActivity(UpdatedCertificationStatusDate.class.getName(), origListing, newListing, activityReason);
            processListingActivity(DeletedSurveillanceActivity.class.getName(), origListing, newListing, activityReason);
            processListingActivity(AddedCqmsActivity.class.getName(), origListing, newListing, activityReason);
            processListingActivity(DeletedCqmsActivity.class.getName(), origListing, newListing, activityReason);
            processListingActivity(AddedCertificationsActivity.class.getName(), origListing, newListing, activityReason);
            processListingActivity(DeletedCertificationsActivity.class.getName(), origListing, newListing, activityReason);
        }
    }

    private Integer processListingActivity(String className, CertifiedProductSearchDetails origListing,  CertifiedProductSearchDetails newListing, String activityReason) {
        Integer activitiesCreated = 0;
        Optional<ListingActivity> listingActivity = getListingActivity(className);
        if (!listingActivity.isPresent()) {
            LOGGER.error("Could not find class: " + className);
        } else {
            List<QuestionableActivityListingDTO> activities = listingActivity.get().check(origListing, newListing);
            if (activities != null && activities.size() > 0) {
                for (QuestionableActivityListingDTO dto : activities) {
                    if (dto != null) {
                        //Need to ge the real user here
                        createListingActivity(dto, origListing.getId(), AuthUtil.getAuditId(), listingActivity.get().getTriggerType(), activityReason);
                    }
                }
            }
        }
        return activitiesCreated;
    }

    private void createListingActivity(QuestionableActivityListingDTO activity, Long listingId,
            Long activityUser, QuestionableActivityTriggerConcept trigger, String activityReason) {
        activity.setListingId(listingId);
        activity.setActivityDate(new Date());
        activity.setUserId(activityUser);
        activity.setReason(activityReason);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private QuestionableActivityTriggerDTO getTrigger(QuestionableActivityTriggerConcept trigger) {
        QuestionableActivityTriggerDTO result = null;
        for (QuestionableActivityTriggerDTO currTrigger : triggerTypes) {
            if (trigger.getName().equalsIgnoreCase(currTrigger.getName())) {
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
