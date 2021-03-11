package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("certificationBodyReviewer")
public class CertificationBodyReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CertificationBodyReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        Map<String, Object> acbMap = listing.getCertifyingBody();
        if (acbMap == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingCertificationBody"));
            return;
        }

        String acbId = null, acbName = null, acbCode = null;

        Object acbNameValue = acbMap.get(CertifiedProductSearchDetails.ACB_NAME_KEY);
        if (acbNameValue != null) {
            acbName = acbNameValue.toString();
            if (StringUtils.isEmpty(acbName)) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.missingCertificationBodyName"));
            }
        } else {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingCertificationBodyName"));
        }

        Object acbCodeValue = acbMap.get(CertifiedProductSearchDetails.ACB_CODE_KEY);
        if (acbCodeValue != null) {
            acbCode = acbCodeValue.toString();
            if (StringUtils.isEmpty(acbCode)) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.missingCertificationBodyCode"));
            }
        } else {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingCertificationBodyCode"));
        }

        Object acbIdValue = acbMap.get(CertifiedProductSearchDetails.ACB_ID_KEY);
        if (acbIdValue != null) {
            acbId = acbIdValue.toString();
            if (!StringUtils.isEmpty(acbName) && StringUtils.isEmpty(acbId)) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.invalidCertificationBody", acbName));
            }
        } else if (acbIdValue == null && !StringUtils.isEmpty(acbName)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.invalidCertificationBody", acbName));
        }
    }
}
