package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("removedCriteriaReviewer")
public class RemovedCriteriaReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public RemovedCriteriaReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(PendingCertifiedProductDTO listing) {
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (BooleanUtils.isTrue(cert.getMeetsCriteria())
                    && cert.getCriterion().getRemoved()) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.removedCriteriaAddNotAllowed",
                                Util.formatCriteriaNumber(cert.getCriterion())));
            }
        }
    }
}
