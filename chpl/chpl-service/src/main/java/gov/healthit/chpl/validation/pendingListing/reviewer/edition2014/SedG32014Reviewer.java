package gov.healthit.chpl.validation.pendingListing.reviewer.edition2014;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

@Component("pendingSedG32014Reviewer")
public class SedG32014Reviewer implements Reviewer {
    private static final String G3_2014 = "170.314 (g)(3)";
    private ErrorMessageUtil msgUtil;

    @Autowired
    public SedG32014Reviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        boolean foundSedCriteria = false;
        boolean attestsToSed = false;

        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria().booleanValue()) {
                if (cert.getSed() != null && cert.getSed().booleanValue()) {
                    foundSedCriteria = true;
                }
                if (cert.getNumber().equalsIgnoreCase(G3_2014)) {
                    attestsToSed = true;
                }
            }
        }
        if (foundSedCriteria && !attestsToSed) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.foundSedCriteriaWithoutAttestingSed"));
        }
        if (!foundSedCriteria && attestsToSed
                && !listing.getIcs()) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.foundNoSedCriteriaButAttestingSed"));
        }
    }
}
