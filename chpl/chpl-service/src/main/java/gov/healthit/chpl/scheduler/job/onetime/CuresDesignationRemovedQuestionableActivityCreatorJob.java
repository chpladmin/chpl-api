package gov.healthit.chpl.scheduler.job.onetime;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.explorer.ListingActivityExplorer;
import gov.healthit.chpl.activity.history.query.ListingActivityQuery;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.questionableactivity.QuestionableActivityDAO;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityTriggerDTO;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityListingEntity;
import gov.healthit.chpl.scheduler.job.CertifiedProduct2015Gatherer;
import gov.healthit.chpl.util.NullSafeEvaluator;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "curesDesignationRemovedQuestionableActivityCreatorJobLogger")
public class CuresDesignationRemovedQuestionableActivityCreatorJob extends CertifiedProduct2015Gatherer implements Job {
    @Autowired
    private CuresDesignationRemovedActivityExplorer curesDesignationRemovedActivityExplorer;

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private ListingActivityUtil activityUtil;

    @Autowired
    private QuestionableActivityDAO questionableActivityDAO;

    @Autowired
    private QuestionableActivityExtDAO questionableActivityExtDAO;

    private List<QuestionableActivityTriggerDTO> triggerTypes;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM YYYY");

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting Cures Designation Removed Questionable Activity Creator job. *********");
        triggerTypes = questionableActivityDAO.getAllTriggers();

        LOGGER.info("Retrieving all 2015 listings");
        List<CertifiedProductDetailsDTO> listings = certifiedProductDAO.findByEdition(
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        LOGGER.info("Completed retreiving all 2015 listings");

        LOGGER.info("Processing listings...");
        listings.stream()
                .flatMap(listing -> curesDesignationRemovedActivityExplorer.getActivities(new ListingActivityQuery(listing.getId())).stream())
                .forEach(activity -> createListingActivityForCuresDesignationRemoved(
                        activity, QuestionableActivityTriggerConcept.CURES_UPDATE_REMOVED));

        LOGGER.info("********* Completed Cures Designation Removed Questionable Activity Creator job. *********");
    }

    private void createListingActivityForCuresDesignationRemoved(ActivityDTO activity, QuestionableActivityTriggerConcept trigger) {
        getQuestionableActivityListingForCuresDesignationRemoved(activity).forEach(qal -> {
                qal.setListingId(activity.getActivityObjectId());
                qal.setActivityDate(activity.getActivityDate());
                qal.setUserId(activity.getLastModifiedUser());
                QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
                qal.setTriggerId(triggerDto.getId());

                if (!questionableActivityExtDAO.questionableActivtyExists(qal)) {
                    questionableActivityDAO.create(qal);
                    LOGGER.info("Need to add Questionable Activity for listing: {}, {} updated on {}",
                            qal.getListingId(),
                            QuestionableActivityTriggerConcept.CURES_UPDATE_REMOVED.getName(),
                            sdf.format(qal.getActivityDate()));
                } else {
                    LOGGER.info("Questionable Activity already exists for listing : {}, {} updated on {}",
                            qal.getListingId(),
                            QuestionableActivityTriggerConcept.CURES_UPDATE_REMOVED.getName(),
                            sdf.format(qal.getActivityDate()));
                }

        });
    }

    private List<QuestionableActivityListingDTO> getQuestionableActivityListingForCuresDesignationRemoved(ActivityDTO activity) {
        CertifiedProductSearchDetails origListing = activityUtil.getListing(activity.getOriginalData());
        CertifiedProductSearchDetails newListing = activityUtil.getListing(activity.getNewData());

        return List.of(QuestionableActivityListingDTO.builder()
                .before(origListing.getCuresUpdate().toString())
                .after(newListing.getCuresUpdate().toString())
                .build());
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


    @Service
    private static class CuresDesignationRemovedActivityExplorer extends ListingActivityExplorer {

        private ActivityDAO activityDao;
        private ListingActivityUtil activityUtil;

        @Autowired
        CuresDesignationRemovedActivityExplorer(ActivityDAO activityDao, ListingActivityUtil activityUtil) {
            this.activityDao = activityDao;
            this.activityUtil = activityUtil;
        }

        @Override
        public ActivityDTO getActivity(ListingActivityQuery query) {
            return null;
        }

        @Override
        @Transactional
        public List<ActivityDTO> getActivities(ListingActivityQuery query) {
            return getSortedActivities(query).stream()
                    .filter(activity -> hasCuresDesignationBeenUpdatedFromTrueToFalse(activity))
                    .toList();
        }

        private List<ActivityDTO> getSortedActivities(ListingActivityQuery query) {
            if (query == null || !(query instanceof ListingActivityQuery)) {
                LOGGER.error("listing activity query was null or of the wrong type");
                return null;
            }

            if (query.getListingId() == null) {
                LOGGER.info("Values must be provided for listing ID and was missing.");
                return null;
            }

            List<ActivityDTO> listingActivities = activityDao.findByObjectId(query.getListingId(), ActivityConcept.CERTIFIED_PRODUCT, new Date(0), new Date());
            if (CollectionUtils.isEmpty(listingActivities)) {
                return Collections.emptyList();
            }
            sortOldestActivityFirst(listingActivities);
            return listingActivities;
        }

        private boolean hasCuresDesignationBeenUpdatedFromTrueToFalse(ActivityDTO activity) {
            if (activity.getOriginalData() == null || activity.getOriginalData().equals("")) {
                return false;
            }
            Boolean originalCuresDesignation = NullSafeEvaluator.eval(() -> activityUtil.getListing(activity.getOriginalData()).getCuresUpdate(), null);
            Boolean updatedCuresDesignation = NullSafeEvaluator.eval(() -> activityUtil.getListing(activity.getNewData()).getCuresUpdate(), null);
            return BooleanUtils.isTrue(originalCuresDesignation) && BooleanUtils.isFalse(updatedCuresDesignation);

        }
     }

    @Service
    private static class QuestionableActivityExtDAO extends BaseDAOImpl {
        public Boolean questionableActivtyExists(QuestionableActivityListingDTO qal) {
            Query query = entityManager.createQuery("SELECT activity "
                    + "FROM QuestionableActivityListingEntity activity "
                    + "WHERE activity.deleted <> true "
                    + "AND activity.triggerId = :triggerId "
                    + "AND activity.activityDate = :activityDate "
                    + "AND activity.listingId = :listingId ",
                    QuestionableActivityListingEntity.class);
            query.setParameter("triggerId", qal.getTriggerId());
            query.setParameter("activityDate", qal.getActivityDate());
            query.setParameter("listingId", qal.getListingId());
            List<QuestionableActivityListingEntity> queryResults = query.getResultList();

            return !CollectionUtils.isEmpty(queryResults);
        }
    }
}
