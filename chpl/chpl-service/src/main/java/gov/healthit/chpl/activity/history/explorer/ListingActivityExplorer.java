package gov.healthit.chpl.activity.history.explorer;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.ObjectUtils;

import gov.healthit.chpl.activity.history.query.ListingActivityQuery;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class ListingActivityExplorer {

    public abstract List<ActivityDTO> getActivities(ListingActivityQuery query);

    protected ActivityDTO getActivityNearestAndBeforeDay(List<ActivityDTO> activities, Long listingId, LocalDate day) {
        sortOldestActivityFirst(activities);
        ActivityDTO result = null;
        ListIterator<ActivityDTO> activityIter = activities.listIterator();
        while (activityIter.hasNext()) {
            ActivityDTO currActivity = activityIter.next();
            if (day.isAfter(DateUtil.toLocalDate(currActivity.getActivityDate().getTime()))) {
                if (activityIter.hasPrevious()) {
                    result = activityIter.previous();
                } else {
                    result = currActivity;
                }
                activityIter.next();
            }
        }
        LOGGER.info("Listing " + listingId + " activity nearest but before " + day + " occurred on " + DateUtil.toLocalDate(result.getActivityDate().getTime()));
        return result;
    }

    protected void sortNewestActivityFirst(List<ActivityDTO> activities) {
        activities.sort(new Comparator<ActivityDTO>() {
            @Override
            public int compare(ActivityDTO o1, ActivityDTO o2) {
                if (ObjectUtils.allNotNull(o1, o2, o1.getActivityDate(), o2.getActivityDate())) {
                    return o2.getActivityDate().compareTo(o1.getActivityDate());
                }
                return 0;
            }
        });
    }

    protected void sortOldestActivityFirst(List<ActivityDTO> activities) {
        activities.sort(new Comparator<ActivityDTO>() {
            @Override
            public int compare(ActivityDTO o1, ActivityDTO o2) {
                if (ObjectUtils.allNotNull(o1, o2, o1.getActivityDate(), o2.getActivityDate())) {
                    return o1.getActivityDate().compareTo(o2.getActivityDate());
                }
                return 0;
            }
        });
    }
}
