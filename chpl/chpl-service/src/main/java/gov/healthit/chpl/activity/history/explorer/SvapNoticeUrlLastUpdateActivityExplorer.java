package gov.healthit.chpl.activity.history.explorer;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.query.ListingActivityQuery;
import gov.healthit.chpl.activity.history.query.SvapNoticeUrlLastUpdateActivityQuery;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class SvapNoticeUrlLastUpdateActivityExplorer extends ListingActivityExplorer {
    private static final Date EPOCH = new Date(0);
    private ActivityDAO activityDao;
    private ListingActivityUtil activityUtil;

    @Autowired
    public SvapNoticeUrlLastUpdateActivityExplorer(ActivityDAO activityDao, ListingActivityUtil activityUtil) {
        this.activityDao = activityDao;
        this.activityUtil = activityUtil;
    }

    @Override
    public List<ActivityDTO> getActivities(ListingActivityQuery query) {
        return null;
    }

    @Override
    @Transactional
    public ActivityDTO getActivity(ListingActivityQuery query) {
        if (query == null || !(query instanceof SvapNoticeUrlLastUpdateActivityQuery)) {
            LOGGER.error("listing activity query was null or of the wrong type");
            return null;
        }

        SvapNoticeUrlLastUpdateActivityQuery svapQuery = (SvapNoticeUrlLastUpdateActivityQuery) query;
        if (svapQuery.getListingId() == null || StringUtils.isBlank(svapQuery.getSvapNoticeUrl())) {
            LOGGER.info("No value found for listing ID or SVAP Notice URL in the activity query. Both must be present.");
            return null;
        }

        LOGGER.info("Getting last update date for SVAP Notice URL for listing ID " + svapQuery.getListingId() + ".");
        List<ActivityDTO> listingActivities = activityDao.findByObjectId(svapQuery.getListingId(), ActivityConcept.CERTIFIED_PRODUCT, EPOCH, new Date());
        if (listingActivities == null || listingActivities.size() == 0) {
            LOGGER.warn("No listing activities were found for listing ID " + svapQuery.getListingId() + ". Is the ID valid?");
            return null;
        }
        LOGGER.info("There are " + listingActivities.size() + " activities for listing ID " + svapQuery.getListingId());
        sortNewestActivityFirst(listingActivities);

        ActivityDTO svapNoticeLastUpdateActivity = null;
        Iterator<ActivityDTO> listingActivityIter = listingActivities.iterator();
        while (svapNoticeLastUpdateActivity == null && listingActivityIter.hasNext()) {
            ActivityDTO currActivity = listingActivityIter.next();
            CertifiedProductSearchDetails orig = null, updated = null;
            if (currActivity.getOriginalData() != null) {
                orig = activityUtil.getListing(currActivity.getOriginalData());
            }
            if (currActivity.getNewData() != null) {
                updated = activityUtil.getListing(currActivity.getNewData());
            }

            if (wasSvapNoticeUrlSetToCurrent(orig, updated, svapQuery.getSvapNoticeUrl())) {
                svapNoticeLastUpdateActivity = currActivity;
                LOGGER.info("Listing " + svapQuery.getListingId() + " SVAP Notice URL was set to " + svapQuery.getSvapNoticeUrl() + " on " + currActivity.getActivityDate() + ".");
            }
        }

        if (svapNoticeLastUpdateActivity == null) {
            LOGGER.warn("Unable to determine when listing " + svapQuery.getListingId() + " set SVAP Notice URL to " + svapQuery.getSvapNoticeUrl());
        }
        return svapNoticeLastUpdateActivity;
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
}
