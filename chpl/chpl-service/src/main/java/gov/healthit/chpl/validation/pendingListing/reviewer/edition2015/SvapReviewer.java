package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

@Component("pendingSvapReviewer")
public class SvapReviewer implements Reviewer {
    private ValidationUtils validationUtils;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public SvapReviewer(ValidationUtils validationUtils, ErrorMessageUtil errorMessageUtil) {
        this.validationUtils = validationUtils;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        validateSvapNoticeUrl(listing);
    }

    private void validateSvapNoticeUrl(PendingCertifiedProductDTO listing) {
        if (!StringUtils.isBlank(listing.getSvapNoticeUrl())
                && !validationUtils.isWellFormedUrl(listing.getSvapNoticeUrl())) {
            listing.getErrorMessages().add(
                    errorMessageUtil.getMessage("listing.svap.url.invalid"));
        }
    }
}
