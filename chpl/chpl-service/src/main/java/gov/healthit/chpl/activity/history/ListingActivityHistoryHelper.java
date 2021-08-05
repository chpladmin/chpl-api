package gov.healthit.chpl.activity.history;

import java.time.LocalDate;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ListingActivityHistoryHelper {
    private static final Date EPOCH = new Date(0);
    private ActivityDAO activityDao;
    private ListingActivityUtil activityUtil;

    @Autowired
    public ListingActivityHistoryHelper(ActivityDAO activityDao) {
        this.activityDao = activityDao;
        activityUtil = new ListingActivityUtil();
    }

    @Transactional
    public LocalDate getLastUpdateDateForSvapNoticeUrl(CertifiedProductSearchDetails listing) {
        if (StringUtils.isBlank(listing.getSvapNoticeUrl())) {
            LOGGER.info("No SVAP Notice URL for listing ID " + listing.getId());
            return null;
        }

        LOGGER.info("Getting last update date for SVAP Notice URL for listing ID " + listing.getId() + ".");
        List<ActivityDTO> listingActivities = activityDao.findByObjectId(listing.getId(), ActivityConcept.CERTIFIED_PRODUCT, EPOCH, new Date());
        if (listingActivities == null || listingActivities.size() == 0) {
            LOGGER.warn("No listing activities were found for listing ID " + listing.getId() + ". Is the ID valid?");
            return null;
        }
        LOGGER.info("There are " + listingActivities.size() + " activities for listing ID " + listing.getId());
        activityUtil.sortNewestActivityFirst(listingActivities);

        LocalDate svapNoticeLastUpdate = null;
        Iterator<ActivityDTO> listingActivityIter = listingActivities.iterator();
        while (svapNoticeLastUpdate == null && listingActivityIter.hasNext()) {
            ActivityDTO currActivity = listingActivityIter.next();
            CertifiedProductSearchDetails orig = null, updated = null;
            if (currActivity.getOriginalData() != null) {
                orig = activityUtil.getListing(currActivity.getOriginalData());
            }
            if (currActivity.getNewData() != null) {
                updated = activityUtil.getListing(currActivity.getNewData());
            }

            if (wasSvapNoticeUrlSetToCurrent(orig, updated, listing.getSvapNoticeUrl())) {
                svapNoticeLastUpdate = DateUtil.toLocalDate(currActivity.getActivityDate().getTime());
                LOGGER.info("Listing " + listing.getId() + " SVAP Notice URL was set to " + listing.getSvapNoticeUrl() + " on " + svapNoticeLastUpdate + ".");
            }
        }

        if (svapNoticeLastUpdate == null) {
            LOGGER.warn("Unable to determine when listing " + listing.getId() + " set SVAP Notice URL to " + listing.getSvapNoticeUrl());
        }
        return svapNoticeLastUpdate;
    }

    private boolean wasSvapNoticeUrlSetToCurrent(CertifiedProductSearchDetails orig, CertifiedProductSearchDetails updated,
            String currentSvapNoticeUrl) {
        if (orig != null && updated != null && !StringUtils.isBlank(updated.getSvapNoticeUrl())
                && updated.getSvapNoticeUrl().equals(currentSvapNoticeUrl)
                && (orig.getSvapNoticeUrl() == null || !currentSvapNoticeUrl.equals(orig.getSvapNoticeUrl()))) {
            return true;
        } else if (orig == null && updated != null && !StringUtils.isBlank(updated.getSvapNoticeUrl())
                && updated.getSvapNoticeUrl().equals(currentSvapNoticeUrl)) {
            return true;
        }
        return false;
    }

    @Transactional
    public CertifiedProductSearchDetails getListingOnDate(Long listingId, Date date) {
        LOGGER.info("Getting listing details for ID " + listingId + " as of " + date + ".");
        List<ActivityDTO> listingActivities = activityDao.findByObjectId(listingId, ActivityConcept.CERTIFIED_PRODUCT, EPOCH, date);
        if (listingActivities == null || listingActivities.size() == 0) {
            LOGGER.warn("No listing activities were found for listing ID " + listingId + ". Is the ID valid?");
            return null;
        }
        LOGGER.info("There are " + listingActivities.size() + " activities for listing with ID " + listingId);
        activityUtil.sortOldestActivityFirst(listingActivities);
        CertifiedProductSearchDetails listingOnDate = null;
        Date closestWithoutGoingOver = null;
        Iterator<ActivityDTO> listingActivityIter = listingActivities.iterator();
        while (listingActivityIter.hasNext()) {
            ActivityDTO currActivity = listingActivityIter.next();
            if ((closestWithoutGoingOver == null || currActivity.getActivityDate().after(closestWithoutGoingOver))
                    && (currActivity.getActivityDate().equals(date) || currActivity.getActivityDate().before(date))) {
                closestWithoutGoingOver = currActivity.getActivityDate();
                listingOnDate = activityUtil.getListing(currActivity.getNewData());
            }
        }
        if (listingOnDate == null) {
            LOGGER.warn("No representation of listing with ID " + listingId + " was found on " + date + ". Did the listing exist at this time?");
        }
        return listingOnDate;
    }
}
