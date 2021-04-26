package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class QmsStandardReviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public QmsStandardReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getQmsStandards() == null || listing.getQmsStandards().size() == 0) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.qmsStandardsNotFound"));
        } else {
            listing.getQmsStandards().stream()
                .forEach(qmsStandard -> {
                    checkQmsStandardNameRequired(listing, qmsStandard);
                    checkApplicableCriteriaRequired(listing, qmsStandard);
                });
        }
    }

    private void checkQmsStandardNameRequired(CertifiedProductSearchDetails listing, CertifiedProductQmsStandard qmsStandard) {
        if (StringUtils.isEmpty(qmsStandard.getQmsStandardName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.qmsStandardMissingName"));
        }
    }

    private void checkApplicableCriteriaRequired(CertifiedProductSearchDetails listing, CertifiedProductQmsStandard qmsStandard) {
        if (StringUtils.isEmpty(qmsStandard.getApplicableCriteria())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.qmsStandardMissingApplicableCriteria"));
        }
    }
}
