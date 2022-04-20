package gov.healthit.chpl.activity.history.explorer;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.query.CertificationResultContainsSvapActivityQuery;
import gov.healthit.chpl.activity.history.query.ListingActivityQuery;
import gov.healthit.chpl.dao.ActivityDAO;
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
public class CertificationResultContainsSvapActivityExplorer extends ListingActivityExplorer {
    private static final Date EPOCH = new Date(0);

    private ActivityDAO activityDao;
    private ListingActivityUtil activityUtil;

    @Autowired
    public CertificationResultContainsSvapActivityExplorer(ActivityDAO activityDao, ListingActivityUtil activityUtil) {
        this.activityDao = activityDao;
        this.activityUtil = activityUtil;
    }

    @Override
    @Transactional
    public List<ActivityDTO> getActivities(ListingActivityQuery query) {
        if (query == null || !(query instanceof CertificationResultContainsSvapActivityQuery)) {
            LOGGER.error("listing activity query was null or of the wrong type");
            return null;
        }

        CertificationResultContainsSvapActivityQuery svapQuery = (CertificationResultContainsSvapActivityQuery) query;
        if (svapQuery.getListingId() == null || svapQuery.getCriterion() == null || svapQuery.getSvap() == null) {
            LOGGER.info("Values must be provided for listing ID, criterion, and svap and at least one was missing.");
            return null;
        }

        LOGGER.info("Finding activity when " + Util.formatCriteriaNumber(svapQuery.getCriterion()) + " SVAP " + svapQuery.getSvap().getRegulatoryTextCitation() + " for listing ID " + svapQuery.getListingId() + ".");
        List<ActivityDTO> listingActivities = activityDao.findByObjectId(svapQuery.getListingId(), ActivityConcept.CERTIFIED_PRODUCT, EPOCH, new Date());
        if (CollectionUtils.isEmpty(listingActivities)) {
            LOGGER.warn("No listing activities were found for listing ID " + svapQuery.getListingId() + ". Is the ID valid?");
            return Collections.emptyList();
        }
        LOGGER.info("There are " + listingActivities.size() + " activities for listing ID " + svapQuery.getListingId());
        sortNewestActivityFirst(listingActivities);

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
                        .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId().equals(svapQuery.getCriterion().getId()))
                        .findAny();
                if (origCertResultOptional.isPresent()) {
                    origCertResult = origCertResultOptional.get();
                }
            }
            Optional<CertificationResult> updatedCertResultOptional = updatedListing.getCertificationResults().stream()
                    .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId().equals(svapQuery.getCriterion().getId()))
                    .findAny();
            if (updatedCertResultOptional.isPresent()) {
                updatedCertResult = updatedCertResultOptional.get();
            }

            if (wasSvapAddedWithCertificationResult(origCertResult, updatedCertResult, svapQuery.getSvap())) {
                LOGGER.info(Util.formatCriteriaNumber(svapQuery.getCriterion()) + " added SVAP " + svapQuery.getSvap().getRegulatoryTextCitation() + " for listing ID " + svapQuery.getListingId() + " on " + currActivity.getActivityDate() + ".");
                activityThatAddedSvap = currActivity;
            }
        }

        if (activityThatAddedSvap == null) {
            LOGGER.info("Unable to determine when " + Util.formatCriteriaNumber(svapQuery.getCriterion()) + " SVAP " + svapQuery.getSvap().getRegulatoryTextCitation() + " for listing ID " + svapQuery.getListingId() + ".");
            return Collections.emptyList();
        }
        return Stream.of(activityThatAddedSvap).toList();
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
