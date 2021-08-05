package gov.healthit.chpl.activity.history;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CertificationResultActivityHistoryHelper {
    private static final Date EPOCH = new Date(0);

    private ActivityDAO activityDao;
    private ListingActivityUtil activityUtil;

    @Autowired
    public CertificationResultActivityHistoryHelper(ActivityDAO activityDao) {
        this.activityDao = activityDao;
        this.activityUtil = new ListingActivityUtil();
    }

    @Transactional
    public boolean didListingRemoveAttestationToCriterionDuringTimeInterval(Long listingId, CertificationCriterion criterion, Date startDate, Date endDate) {
        LOGGER.info("Determining if listing ID " + listingId + " removed attestation for " + criterion.getId() + " between " + startDate + " and " + endDate);
        List<ActivityDTO> listingActivities = activityDao.findByObjectId(listingId, ActivityConcept.CERTIFIED_PRODUCT, startDate, endDate);
        for (ActivityDTO listingActivity : listingActivities) {
            CertifiedProductSearchDetails originalListingInActivity = activityUtil.getListing(listingActivity.getOriginalData());
            CertifiedProductSearchDetails updatedListingInActivity = activityUtil.getListing(listingActivity.getNewData());
            if (originalListingInActivity != null && updatedListingInActivity != null) {
                CertificationResult originalListingCertResultForCriterion
                    = originalListingInActivity.getCertificationResults().stream()
                        .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId().equals(criterion.getId()))
                        .findAny().get();
                CertificationResult updatedListingCertResultForCriterion
                    = updatedListingInActivity.getCertificationResults().stream()
                        .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId().equals(criterion.getId()))
                        .findAny().get();
                if (originalListingCertResultForCriterion.isSuccess() && !updatedListingCertResultForCriterion.isSuccess()) {
                    LOGGER.info("Listing ID " + listingId + " unattested to criterion " + criterion.getId() +  " on " + listingActivity.getActivityDate());
                    return true;
                }
            }
        }
        LOGGER.info("Listing ID " + listingId + " never unattested to criterion " + criterion.getId() +  " during the dates specified.");
        return false;
    }

    @Transactional
    public ActivityDTO getActivityWhenCertificationResultHasSvap(CertifiedProductSearchDetails listing, CertificationCriterion criterion, Svap svap) {
        LOGGER.info("Finding activity when " + Util.formatCriteriaNumber(criterion) + " SVAP " + svap.getRegulatoryTextCitation() + " for listing ID " + listing.getId() + ".");
        List<ActivityDTO> listingActivities = activityDao.findByObjectId(listing.getId(), ActivityConcept.CERTIFIED_PRODUCT, EPOCH, new Date());
        if (listingActivities == null || listingActivities.size() == 0) {
            LOGGER.warn("No listing activities were found for listing ID " + listing.getId() + ". Is the ID valid?");
            return null;
        }
        LOGGER.info("There are " + listingActivities.size() + " activities for listing ID " + listing.getId());
        activityUtil.sortNewestActivityFirst(listingActivities);

        ActivityDTO activityThatAddedSvap = null;
        Iterator<ActivityDTO> listingActivityIter = listingActivities.iterator();
        while (activityThatAddedSvap == null && listingActivityIter.hasNext()) {
            ActivityDTO currActivity = listingActivityIter.next();
            CertifiedProductSearchDetails origListing = null, updatedListing = null;
            if (currActivity.getOriginalData() != null) {
                origListing = activityUtil.getListing(currActivity.getOriginalData());
            }
            if (currActivity.getNewData() != null) {
                updatedListing = activityUtil.getListing(currActivity.getNewData());
            }

            CertificationResult origCertResult = null, updatedCertResult = null;
            if (origListing != null) {
                Optional<CertificationResult> origCertResultOptional = origListing.getCertificationResults().stream()
                        .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId().equals(criterion.getId()))
                        .findAny();
                if (origCertResultOptional.isPresent()) {
                    origCertResult = origCertResultOptional.get();
                }
            }
            Optional<CertificationResult> updatedCertResultOptional = updatedListing.getCertificationResults().stream()
                    .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId().equals(criterion.getId()))
                    .findAny();
            if (updatedCertResultOptional.isPresent()) {
                updatedCertResult = updatedCertResultOptional.get();
            }

            if (wasSvapAddedWithCertificationResult(origCertResult, updatedCertResult, svap)) {
                LOGGER.info(Util.formatCriteriaNumber(criterion) + " added SVAP " + svap.getRegulatoryTextCitation() + " for listing ID " + listing.getId() + " on " + currActivity.getActivityDate() + ".");
                activityThatAddedSvap = currActivity;
            }
        }

        if (activityThatAddedSvap == null) {
            LOGGER.info("Unable to determine when " + Util.formatCriteriaNumber(criterion) + " SVAP " + svap.getRegulatoryTextCitation() + " for listing ID " + listing.getId() + ".");
        }
        return activityThatAddedSvap;
    }

    private boolean wasSvapAddedWithCertificationResult(CertificationResult orig, CertificationResult updated, Svap svap) {
        if (orig != null && updated != null && svapListContains(updated.getSvaps(), svap)
                && !svapListContains(orig.getSvaps(), svap)) {
            return true;
        } else if (orig == null && updated != null && svapListContains(updated.getSvaps(), svap)) {
            return true;
        }
        return false;
    }

    private boolean svapListContains(List<CertificationResultSvap> svaps, Svap svap) {
        if (CollectionUtils.isEmpty(svaps)) {
            return false;
        }
        List<Long> svapIds = svaps.stream()
                .map(currSvap -> currSvap.getSvapId())
                .collect(Collectors.toList());
        return svapIds.contains(svap.getSvapId());
    }
}
