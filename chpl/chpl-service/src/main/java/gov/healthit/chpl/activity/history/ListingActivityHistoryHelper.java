package gov.healthit.chpl.activity.history;

import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ListingActivityHistoryHelper {
    private static final Date EPOCH = new Date(0);
    private ActivityDAO activityDao;
    private ObjectMapper jsonMapper;

    @Autowired
    public ListingActivityHistoryHelper(ActivityDAO activityDao) {
        this.activityDao = activityDao;
        jsonMapper = new ObjectMapper();
    }

    public CertifiedProductSearchDetails getListingOnDate(Long listingId, Date date) {
        LOGGER.info("Getting listing details for ID " + listingId + " as of " + date + ".");
        List<ActivityDTO> listingActivities = activityDao.findByObjectId(listingId, ActivityConcept.CERTIFIED_PRODUCT, EPOCH, date);
        if (listingActivities == null || listingActivities.size() == 0) {
            LOGGER.warn("No listing activities were found for listing ID " + listingId + ". Is the ID valid?");
            return null;
        }
        LOGGER.info("There are " + listingActivities.size() + " activities for listing with ID " + listingId);
        listingActivities.sort(new Comparator<ActivityDTO>() {
            @Override
            public int compare(ActivityDTO o1, ActivityDTO o2) {
                if (ObjectUtils.allNotNull(o1, o2, o1.getActivityDate(), o2.getActivityDate())) {
                    return o1.getActivityDate().compareTo(o2.getActivityDate());
                }
                return 0;
            }
        });
        CertifiedProductSearchDetails listingOnDate = null;
        Date closestWithoutGoingOver = null;
        Iterator<ActivityDTO> listingActivityIter = listingActivities.iterator();
        while (listingActivityIter.hasNext()) {
            ActivityDTO currActivity = listingActivityIter.next();
            if ((closestWithoutGoingOver == null || currActivity.getActivityDate().after(closestWithoutGoingOver))
                    && (currActivity.getActivityDate().equals(date) || currActivity.getActivityDate().before(date))) {
                closestWithoutGoingOver = currActivity.getActivityDate();
                listingOnDate = getListing(currActivity.getNewData());
            }
        }
        if (listingOnDate == null) {
            LOGGER.warn("No representation of listing with ID " + listingId + " was found on " + date + ". Did the listing exist at this time?");
        }
        return listingOnDate;
    }

    private CertifiedProductSearchDetails getListing(String listingJson) {
        CertifiedProductSearchDetails listing = null;
        if (!StringUtils.isEmpty(listingJson)) {
            try {
                listing =
                    jsonMapper.readValue(listingJson, CertifiedProductSearchDetails.class);
            } catch (Exception ex) {
                LOGGER.error("Could not parse activity JSON " + listingJson, ex);
            }
        }
        return listing;
    }
}
