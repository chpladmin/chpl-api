package gov.healthit.chpl.activity.history.explorer;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.activity.history.query.ListingActivityQuery;
import gov.healthit.chpl.activity.history.query.ListingOnDateActivityQuery;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ListingExistsOnDateActivityExplorer extends ListingActivityExplorer {
    private static final Date EPOCH = new Date(0);

    private ActivityDAO activityDao;

    @Autowired
    public ListingExistsOnDateActivityExplorer(ActivityDAO activityDao) {
        this.activityDao = activityDao;
    }

    @Override
    @Transactional
    public ActivityDTO getActivity(ListingActivityQuery query) {
        if (query == null || !(query instanceof ListingOnDateActivityQuery)) {
            LOGGER.error("listing activity query was null or of the wrong type");
            return null;
        }

        ListingOnDateActivityQuery listingQuery = (ListingOnDateActivityQuery) query;
        if (listingQuery.getListingId() == null || listingQuery.getDay() == null) {
            LOGGER.info("Values must be provided for listing ID and listing day and at least one was missing.");
            return null;
        }

        ActivityDTO activityNearestDay = null;
        LOGGER.info("Getting listing " + listingQuery.getListingId() + " on " + listingQuery.getDay() + ".");
        List<ActivityDTO> listingActivities = activityDao.findByObjectId(listingQuery.getListingId(), ActivityConcept.CERTIFIED_PRODUCT, EPOCH, new Date());
        if (listingActivities == null || listingActivities.size() == 0) {
            LOGGER.warn("No listing activities were found for listing ID " + listingQuery.getListingId() + ". Is the ID valid?");
            return null;
        }
        LOGGER.info("There are " + listingActivities.size() + " activities for listing ID " + listingQuery.getListingId());
        sortOldestActivityFirst(listingActivities);

        if (listingExistedBeforeDay(listingActivities, listingQuery.getListingId(), listingQuery.getDay())) {
            //get the activity representing the listing closest to and before the day in question
            //the "after" part of that activity will have the listing details as the listing looked on the day
            activityNearestDay = getActivityNearestAndBeforeDay(listingActivities, listingQuery.getListingId(), listingQuery.getDay());
        }
        return activityNearestDay;
    }

    private boolean listingExistedBeforeDay(List<ActivityDTO> listingActivities, Long listingId, LocalDate day) {
        ActivityDTO listingCreationActivity = listingActivities.get(0);
        LocalDate listingCreationDay = DateUtil.toLocalDate(listingCreationActivity.getActivityDate().getTime());
        if (listingCreationDay.isBefore(day)) {
            LOGGER.info("Listing " + listingId + " was created on " + listingCreationDay + " (before " + day + ")");
            return true;
        } else {
            LOGGER.info("Listing " + listingId + " was created on " + listingCreationDay + " (after " + day + ")");
            return false;
        }
    }
}
