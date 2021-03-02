package gov.healthit.chpl.validation.listing.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("certificationDateReviewer")
public class CertificationDateReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CertificationDateReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }
    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationDate() == null && StringUtils.isEmpty(listing.getCertificationDateStr())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.certificationDateMissing"));
        } else if (listing.getCertificationDate() == null && !StringUtils.isEmpty(listing.getCertificationDateStr())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.badCertificationDate", listing.getCertificationDateStr()));
        }

        if (listing.getCertificationDate() != null && listing.getCertificationDate() > System.currentTimeMillis()) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.futureCertificationDate"));
        }
    }
}
