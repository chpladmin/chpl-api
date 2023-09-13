package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("listingUploadUnavailableCriteriaReviewer")
public class UnavailableCriteriaReviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public UnavailableCriteriaReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (listing == null || certResult == null) {
            return;
        }

        if (!isCriterionAttestedAndUnavailable(listing, certResult)) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.unavailableCriteriaAddNotAllowed",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    DateUtil.format(certResult.getCriterion().getStartDay()),
                    DateUtil.format(certResult.getCriterion().getEndDay())));
        }
    }

    private boolean isCriterionAttestedAndUnavailable(CertifiedProductSearchDetails listing,
            CertificationResult certResult) {

        return BooleanUtils.isTrue(certResult.isSuccess())
                && certResult.getCriterion() != null
                && DateUtil.datesOverlap(Pair.of(listing.getCertificationDay(), listing.getDecertificationDay()),
                        Pair.of(certResult.getCriterion().getStartDay(), certResult.getCriterion().getEndDay()));
    }
}
