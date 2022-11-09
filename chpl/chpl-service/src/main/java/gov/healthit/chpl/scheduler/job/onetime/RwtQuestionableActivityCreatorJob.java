package gov.healthit.chpl.scheduler.job.onetime;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.explorer.ListingActivityExplorer;
import gov.healthit.chpl.activity.history.query.ListingActivityQuery;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityTriggerDTO;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityListingEntity;
import gov.healthit.chpl.scheduler.job.CertifiedProduct2015Gatherer;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.NullSafeEvaluator;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "rwtQuestionableActivityCreatorJobLogger")
public class RwtQuestionableActivityCreatorJob extends CertifiedProduct2015Gatherer implements Job {
    @Autowired
    private RwtPlanActivityExplorer rwtPlanActivityExplorer;

    @Autowired
    private RwtResultsActivityExplorer rwtResultsActivityExplorer;

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

        LOGGER.info("********* Starting RWT Questionable Activity Creator job. *********");
        triggerTypes = questionableActivityDAO.getAllTriggers();

        LOGGER.info("Retrieving all 2015 listings");
        List<CertifiedProductDetailsDTO> listings = certifiedProductDAO.findByEdition(
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        LOGGER.info("Completed retreiving all 2015 listings");

        LOGGER.info("Processing Plans");
        listings.stream()
                .flatMap(listing -> rwtPlanActivityExplorer.getActivities(new ListingActivityQuery(listing.getId())).stream())
                .forEach(activity -> createListingActivityForRwtPlans(activity, QuestionableActivityTriggerConcept.RWT_PLANS_UPDATED_OUTSIDE_NORMAL_PERIOD));

        LOGGER.info("Processing Results");
        listings.stream()
                .flatMap(listing -> rwtResultsActivityExplorer.getActivities(new ListingActivityQuery(listing.getId())).stream())
                .forEach(activity -> createListingActivityForRwtPlans(activity, QuestionableActivityTriggerConcept.RWT_RESULTS_UPDATED_OUTSIDE_NORMAL_PERIOD));

        LOGGER.info("********* Completed RWT Questionable Activity Creator job. *********");
    }

    private String getFormatRwtInfo(String url, LocalDate checkDate) {
        return "{"
                + (checkDate != null ? checkDate.toString() : "NULL")
                + "; "
                + (url != null ? url : "NULL")
                + "}";
    }

    private void createListingActivityForRwtPlans(ActivityDTO activity, QuestionableActivityTriggerConcept trigger) {
        getQuestionableActivityListingForRwtPlans(activity).forEach(qal -> {
                qal.setListingId(activity.getActivityObjectId());
                qal.setActivityDate(activity.getActivityDate());
                qal.setUserId(activity.getLastModifiedUser());
                QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
                qal.setTriggerId(triggerDto.getId());

                if (!questionableActivityExtDAO.questionableActivtyExists(qal)) {
                    questionableActivityDAO.create(qal);
                    LOGGER.info("Added Questionable Activity for listing: {}, {} updated on {}",
                            qal.getListingId(),
                            triggerDto.getName().equals(QuestionableActivityTriggerConcept.RWT_PLANS_UPDATED_OUTSIDE_NORMAL_PERIOD.getName()) ? "Plan" : "Results",
                            sdf.format(qal.getActivityDate()));
                } else {
                    LOGGER.info("Questionable Activity already exists for listing : {}, {} updated on {}",
                            qal.getListingId(),
                            triggerDto.getName().equals(QuestionableActivityTriggerConcept.RWT_PLANS_UPDATED_OUTSIDE_NORMAL_PERIOD.getName()) ? "Plan" : "Results",
                            sdf.format(qal.getActivityDate()));
                }

        });
    }

    private List<QuestionableActivityListingDTO> getQuestionableActivityListingForRwtPlans(ActivityDTO activity) {
        CertifiedProductSearchDetails origListing = activityUtil.getListing(activity.getOriginalData());
        CertifiedProductSearchDetails newListing = activityUtil.getListing(activity.getNewData());

        return List.of(QuestionableActivityListingDTO.builder()
                .before(getFormatRwtInfo(origListing.getRwtPlansUrl(), origListing.getRwtPlansCheckDate()))
                .after(getFormatRwtInfo(newListing.getRwtPlansUrl(), newListing.getRwtPlansCheckDate()))
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
    private static class RwtPlanActivityExplorer extends ListingActivityExplorer {

        private ActivityDAO activityDao;
        private ListingActivityUtil activityUtil;
        private String planStartMonthDay;  //   MM/DD
        private String planEndMonthDay;  //   MM/DD

        @Autowired
        RwtPlanActivityExplorer(ActivityDAO activityDao, ListingActivityUtil activityUtil,
                @Value("${rwtPlanStartDayOfYear}") String planStartMonthDay,
                @Value("${rwtPlanDueDate}") String planEndMonthDay) {
            this.activityDao = activityDao;
            this.activityUtil = activityUtil;
            this.planStartMonthDay = planStartMonthDay;
            this.planEndMonthDay = planEndMonthDay;
        }

        @Override
        public ActivityDTO getActivity(ListingActivityQuery query) {
            return null;
        }

        @Override
        @Transactional
        public List<ActivityDTO> getActivities(ListingActivityQuery query) {
            return getSortedActivities(query).stream()
                    .filter(activity -> hasRwtPlanInformationBeenUpdated(activity) && !isPlanUpdatedInStandardWindow(activity))
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

        private boolean hasRwtPlanInformationBeenUpdated(ActivityDTO activity) {
            if (activity.getOriginalData() == null || activity.getOriginalData().equals("")) {
                return false;
            }
            String originalPlan = NullSafeEvaluator.eval(() -> activityUtil.getListing(activity.getOriginalData()).getRwtPlansUrl(), "");
            String updatedPlan = NullSafeEvaluator.eval(() -> activityUtil.getListing(activity.getNewData()).getRwtPlansUrl(), "");
            LocalDate originalPlanCheckDate = NullSafeEvaluator.eval(() -> activityUtil.getListing(activity.getOriginalData()).getRwtPlansCheckDate(), LocalDate.MIN);
            LocalDate updatedPlanCheckDate = NullSafeEvaluator.eval(() -> activityUtil.getListing(activity.getNewData()).getRwtPlansCheckDate(), LocalDate.MIN);
            return !originalPlan.equals(updatedPlan) || !originalPlanCheckDate.equals(updatedPlanCheckDate);

        }

        private boolean isPlanUpdatedInStandardWindow(ActivityDTO activity) {
            LocalDateTime activityDate = DateUtil.toLocalDateTime(activity.getActivityDate().getTime());
            return activityDate.isAfter(getPlanStartPeriodBasedOnActivityYear(activityDate.getYear()))
                    && activityDate.isBefore(getPlanEndPeriodBasedOnAcvitityYear(activityDate.getYear()));
        }

        private LocalDateTime getPlanStartPeriodBasedOnActivityYear(Integer year) {
            String[] dateParts = planStartMonthDay.split("/");
            return LocalDateTime.of(year, Integer.valueOf(dateParts[0]), Integer.valueOf(dateParts[1]), 0, 0);
        }

        private LocalDateTime getPlanEndPeriodBasedOnAcvitityYear(Integer year) {
            String[] dateParts = planEndMonthDay.split("/");
            return LocalDateTime.of(year, Integer.valueOf(dateParts[0]), Integer.valueOf(dateParts[1]), 23, 59);
        }
     }

    @Service
    private static class RwtResultsActivityExplorer extends ListingActivityExplorer {

        private ActivityDAO activityDao;
        private ListingActivityUtil activityUtil;
        private String resultsStartMonthDay;  //   MM/DD
        private String resultsEndMonthDay;  //   MM/DD

        @Autowired
        RwtResultsActivityExplorer(ActivityDAO activityDao, ListingActivityUtil activityUtil,
                @Value("${rwtResultsStartDayOfYear}") String resultsStartMonthDay,
                @Value("${rwtResultsDueDate}") String resultsEndMonthDay) {
            this.activityDao = activityDao;
            this.activityUtil = activityUtil;
            this.resultsStartMonthDay = resultsStartMonthDay;
            this.resultsEndMonthDay = resultsEndMonthDay;
        }

        @Override
        public ActivityDTO getActivity(ListingActivityQuery query) {
            return null;
        }

        @Override
        @Transactional
        public List<ActivityDTO> getActivities(ListingActivityQuery query) {
            return getSortedActivities(query).stream()
                    .filter(activity -> hasRwtResultsInformationBeenUpdated(activity) && isResultsUpdatedInStandardWindow(activity))
                    .peek(activity -> LOGGER.info("Found activity for {}", activity.getActivityObjectId()))
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

        private boolean hasRwtResultsInformationBeenUpdated(ActivityDTO activity) {
            if (activity.getOriginalData() == null || activity.getOriginalData().equals("")) {
                return false;
            }
            String originalResults = NullSafeEvaluator.eval(() -> activityUtil.getListing(activity.getOriginalData()).getRwtResultsUrl(), "");
            String updatedResults = NullSafeEvaluator.eval(() -> activityUtil.getListing(activity.getNewData()).getRwtResultsUrl(), "");
            LocalDate originalResultsCheckDate = NullSafeEvaluator.eval(() -> activityUtil.getListing(activity.getOriginalData()).getRwtResultsCheckDate(), LocalDate.MIN);
            LocalDate updatedResultsCheckDate = NullSafeEvaluator.eval(() -> activityUtil.getListing(activity.getNewData()).getRwtResultsCheckDate(), LocalDate.MIN);
            return !originalResults.equals(updatedResults) || !originalResultsCheckDate.equals(updatedResultsCheckDate);

        }

        private boolean isResultsUpdatedInStandardWindow(ActivityDTO activity) {
            LocalDateTime activityDate = DateUtil.toLocalDateTime(activity.getActivityDate().getTime());
            return activityDate.isAfter(getResultsStartPeriodBasedOnActivityYear(activityDate.getYear()))
                    && activityDate.isBefore(getResultsEndPeriodBasedOnAcvitityYear(activityDate.getYear()));
        }

        private LocalDateTime getResultsStartPeriodBasedOnActivityYear(Integer year) {
            String[] dateParts = resultsStartMonthDay.split("/");
            return LocalDateTime.of(year, Integer.valueOf(dateParts[0]), Integer.valueOf(dateParts[1]), 0, 0);
        }

        private LocalDateTime getResultsEndPeriodBasedOnAcvitityYear(Integer year) {
            String[] dateParts = resultsEndMonthDay.split("/");
            return LocalDateTime.of(year, Integer.valueOf(dateParts[0]), Integer.valueOf(dateParts[1]), 23, 59);
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
