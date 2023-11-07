package gov.healthit.chpl.validation.listing.reviewer;

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

@Component("listingUnavailableCriteriaReviewer")
public class UnavailableCriteriaReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public UnavailableCriteriaReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess()))
            .forEach(certResult -> review(listing, certResult));
    }

    //When viewing/editing a listing, there should be an error for any attested criteria
    //with active date range outside of the listing active date range.
    //It is allowed for an existing listing to attest to (unchanged) criteria removed
    //more than 1 year ago.
    private void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (listing == null || certResult == null) {
            return;
        }

        if (isCriterionAttested(certResult)
                && !doCriterionDatesOverlapCertificationDay(listing, certResult)) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.unavailableCriteriaAddNotAllowed",
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
