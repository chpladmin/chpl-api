package gov.healthit.chpl.activity.history.explorer;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.activity.history.query.ListingActivityQuery;
import gov.healthit.chpl.activity.history.query.RealWorldTestingEligibilityQuery;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class RealWorldTestingEligibilityActivityExplorer extends ListingActivityExplorer {
    private static final Date EPOCH = new Date(0);
    private ActivityDAO activityDao;

    @Autowired
    public RealWorldTestingEligibilityActivityExplorer(ActivityDAO activityDao) {
        this.activityDao = activityDao;
    }

    @Override
    @Transactional
    public ActivityDTO getActivity(ListingActivityQuery query) {
        RealWorldTestingEligibilityQuery realWorldTestingEligibilityQuery = (RealWorldTestingEligibilityQuery) query;
        List<ActivityDTO> listingActivities = getAllActivityforListing(realWorldTestingEligibilityQuery.getCertifiedProductId());

        ActivityDTO activityReturn = null;
        Iterator<ActivityDTO> listingActivityIter = listingActivities.iterator();
        while (listingActivityIter.hasNext()) {
            ActivityDTO currActivity = listingActivityIter.next();

            if (currActivity.getActivityDate().before(new Date(DateUtil.toEpochMillis(realWorldTestingEligibilityQuery.getAsOfDate())))) {
                activityReturn = currActivity;
            }
        }
        return activityReturn;
    }

    private List<ActivityDTO> getAllActivityforListing(Long listingId) {
        List<ActivityDTO> activities = activityDao.findByObjectId(listingId, ActivityConcept.CERTIFIED_PRODUCT, EPOCH, new Date());
        sortOldestActivityFirst(activities);
        return activities;
    }
}
