package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("listingUploadRemovedCriteriaReviewer")
public class RemovedCriteriaReviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public RemovedCriteriaReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (CollectionUtils.isNotEmpty(listing.getCertificationResults())) {
            listing.getCertificationResults().stream()
                .filter(certResult -> attestedCriterionIsRemoved(certResult))
                .forEach(removedAttestedCriterion -> listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.removedCriteriaAddNotAllowed",
                                Util.formatCriteriaNumber(removedAttestedCriterion.getCriterion()))));
        }
    }

    private boolean attestedCriterionIsRemoved(CertificationResult certResult) {
        return BooleanUtils.isTrue(certResult.isSuccess())
                && certResult.getCriterion() != null
                && BooleanUtils.isTrue(certResult.getCriterion().getRemoved());
    }
}
