package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.time.LocalDate;

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

    //When uploading a listing, there should be an error for any attested criteria
    //with active date range outside of the listing active date range.
    //There should be an additional check on upload that any criteria being attested to, if it's removed,
    //was removed less than 1 year ago.
    public void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (listing == null || certResult == null) {
            return;
        }

        if (isCriterionAttested(certResult)
                && !doCriterionDatesOverlapCertificationDay(listing, certResult)) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.unavailableCriteriaAddNotAllowed",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (isCriterionAttested(certResult)
                && !certResult.getCriterion().isEditable()) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.unavailableCriteriaRemovedTooLongAgo",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private boolean isCriterionAttested(CertificationResult certResult) {
        return BooleanUtils.isTrue(certResult.isSuccess());
    }

    private boolean doCriterionDatesOverlapCertificationDay(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        LocalDate listingEndDay = listing.getDecertificationDay() != null ? listing.getDecertificationDay() : LocalDate.now();
        return certResult.getCriterion() != null
                && DateUtil.datesOverlap(Pair.of(listing.getCertificationDay(), listingEndDay),
                        Pair.of(certResult.getCriterion().getStartDay(), certResult.getCriterion().getEndDay()));
    }
}
