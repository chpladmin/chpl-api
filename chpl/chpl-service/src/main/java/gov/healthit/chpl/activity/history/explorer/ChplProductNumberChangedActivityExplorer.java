package gov.healthit.chpl.activity.history.explorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.query.ListingActivityQuery;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ChplProductNumberChangedActivityExplorer extends ListingActivityExplorer {
    private static final Date EPOCH = new Date(0);

    private ActivityDAO activityDao;
    private ListingActivityUtil activityUtil;

    @Autowired
    public ChplProductNumberChangedActivityExplorer(ActivityDAO activityDao, ListingActivityUtil activityUtil) {
        this.activityDao = activityDao;
        this.activityUtil = activityUtil;
    }

    @Override
    @Transactional
    public List<ActivityDTO> getActivities(ListingActivityQuery query) {
        if (query == null || !(query instanceof ListingActivityQuery)) {
            LOGGER.error("listing activity query was null or of the wrong type");
            return null;
        }

        if (query.getListingId() == null) {
            LOGGER.info("Values must be provided for listing ID and was missing.");
            return null;
        }

        LOGGER.info("Finding all activity for listing ID " + query.getListingId() + ".");
        List<ActivityDTO> listingActivities = activityDao.findByObjectId(query.getListingId(), ActivityConcept.CERTIFIED_PRODUCT, EPOCH, new Date());
        if (CollectionUtils.isEmpty(listingActivities)) {
            LOGGER.warn("No listing activities were found for listing ID " + query.getListingId() + ". Is the ID valid?");
            return Collections.emptyList();
        }
        LOGGER.info("There are " + listingActivities.size() + " activities for listing ID " + query.getListingId());

        List<ActivityDTO> activitiesWhereChplProductNumberChanged = new ArrayList<ActivityDTO>();
        listingActivities.stream()
            .filter(activity -> didChplProductNumberChange(activity))
            .forEach(activity -> activitiesWhereChplProductNumberChanged.add(activity));
        return activitiesWhereChplProductNumberChanged;
    }

    private boolean didChplProductNumberChange(ActivityDTO activity) {
        CertifiedProductSearchDetails origListing = null, updatedListing = null;
        if (activity.getOriginalData() != null) {
            origListing = activityUtil.getListing(activity.getOriginalData());
        }
        if (activity.getNewData() != null) {
            updatedListing = activityUtil.getListing(activity.getNewData());
        }

        return origListing != null && updatedListing != null
                && !origListing.getChplProductNumber().equals(updatedListing.getChplProductNumber());
    }
}
