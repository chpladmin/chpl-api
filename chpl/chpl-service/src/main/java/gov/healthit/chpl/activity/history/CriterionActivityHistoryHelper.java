package gov.healthit.chpl.activity.history;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import lombok.Setter;

public class CriterionActivityHistoryHelper {
    @Setter
    private Logger logger;
    private ObjectMapper jsonMapper;

    @Autowired
    public CriterionActivityHistoryHelper() {
        jsonMapper = new ObjectMapper();
    }

    public boolean didListingRemoveAttestationToCriterionDuringTimeInterval(Long listingId, List<ActivityDTO> listingActivities,
            CertificationCriterion criterion, Date startDate, Date endDate) {
        logger.info("Determining if listing ID " + listingId + " removed attestation for " + criterion.getId() + " between " + startDate + " and " + endDate);
        for (ActivityDTO listingActivity : listingActivities) {
            CertifiedProductSearchDetails originalListingInActivity = getListing(listingActivity.getOriginalData());
            CertifiedProductSearchDetails updatedListingInActivity = getListing(listingActivity.getNewData());
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
                    logger.info("Listing ID " + listingId + " unattested to criterion " + criterion.getId() +  " on " + listingActivity.getActivityDate());
                    return true;
                }
            }
        }
        logger.info("Listing ID " + listingId + " never unattested to criterion " + criterion.getId() +  " during the dates specified.");
        return false;
    }

    public CertifiedProductSearchDetails getListing(String listingJson) {
        CertifiedProductSearchDetails listing = null;
        if (!StringUtils.isEmpty(listingJson)) {
            try {
                listing =
                    jsonMapper.readValue(listingJson, CertifiedProductSearchDetails.class);
            } catch (Exception ex) {
                logger.error("Could not parse activity JSON " + listingJson, ex);
            }
        }
        return listing;
    }
}
