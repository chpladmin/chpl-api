package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class AccessibilityStandardReviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public AccessibilityStandardReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getAccessibilityStandards() == null || listing.getAccessibilityStandards().size() == 0) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.accessibilityStandardsNotFound"));
        } else {
            listing.getAccessibilityStandards().stream()
                .forEach(accessibilityStandard -> checkAccessibilityStandardNameRequired(listing, accessibilityStandard));
        }
    }

    private void checkAccessibilityStandardNameRequired(CertifiedProductSearchDetails listing, CertifiedProductAccessibilityStandard accessibilityStandard) {
        if (StringUtils.isEmpty(accessibilityStandard.getAccessibilityStandardName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.accessibilityStandardMissingName"));
        }
    }
}
