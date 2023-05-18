package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("certificationIdReviewer")
public class CertificationIdReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CertificationIdReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }
    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (StringUtils.isBlank(listing.getAcbCertificationId())) {
            listing.addWarningMessage(msgUtil.getMessage("listing.certificationIdMissing"));
        }
    }
}
