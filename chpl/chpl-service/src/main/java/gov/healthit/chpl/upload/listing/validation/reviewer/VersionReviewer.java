package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("versionReviewer")
public class VersionReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public VersionReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getVersion() == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingVersion"));
        }
    }
}
