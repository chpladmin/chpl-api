package gov.healthit.chpl.scheduler.job.onetime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.explorer.ListingActivityExplorer;
import gov.healthit.chpl.activity.history.query.ListingActivityQuery;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.NullSafeEvaluator;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RwtQuestionableActivityCreatorJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

    }


    public class RwtActivityExplorer extends ListingActivityExplorer {

        private ActivityDAO activityDao;
        private ListingActivityUtil activityUtil;
        private String planStartMonthDay;  //   MM/DD
        private String planEndMonthDay;  //   MM/DD

        @Autowired
        public RwtActivityExplorer(ActivityDAO activityDao, ListingActivityUtil activityUtil, @Value("${rwtPlanStartDayOfYear}") String planStartMonthDay,
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
                    .filter(activity -> hasRwtPlanInformationBeenUpdated(activity) && wasPlanUpdatedOutsideStandardWindow(activity))
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

            LOGGER.info("Finding all activity for listing ID " + query.getListingId() + ".");
            List<ActivityDTO> listingActivities = activityDao.findByObjectId(query.getListingId(), ActivityConcept.CERTIFIED_PRODUCT, new Date(0), new Date());
            if (CollectionUtils.isEmpty(listingActivities)) {
                LOGGER.warn("No listing activities were found for listing ID " + query.getListingId() + ". Is the ID valid?");
                return Collections.emptyList();
            }
            LOGGER.info("There are " + listingActivities.size() + " activities for listing ID " + query.getListingId());

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

        private boolean wasPlanUpdatedOutsideStandardWindow(ActivityDTO activity) {
            LocalDateTime activityDate = DateUtil.toLocalDateTime(activity.getActivityDate().getTime());
            return activityDate.isAfter(getPlanStartPeriodBasedOnAcvitityYear(activityDate.getYear()))
                    && activityDate.isBefore(getPlanEndPeriodBasedOnAcvitityYear(activityDate.getYear()));
        }

        private LocalDateTime getPlanStartPeriodBasedOnAcvitityYear(Integer year) {
            String[] dateParts = planStartMonthDay.split("/");
            return LocalDateTime.of(year, Integer.valueOf(dateParts[0]), Integer.valueOf(dateParts[1]), 0, 0);
        }

        private LocalDateTime getPlanEndPeriodBasedOnAcvitityYear(Integer year) {
            String[] dateParts = planStartMonthDay.split("/");
            return LocalDateTime.of(year, Integer.valueOf(dateParts[0]), Integer.valueOf(dateParts[1]), 23, 59);
        }
     }
}
