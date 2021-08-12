package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ActivityDTO;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CertificationResultActivityHistoryHelper {

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
}
