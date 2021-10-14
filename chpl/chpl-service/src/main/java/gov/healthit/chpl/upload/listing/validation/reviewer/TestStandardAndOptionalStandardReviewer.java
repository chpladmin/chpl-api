package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("listingUploadTestStandardAndOptionalStandardReviewer")
public class TestStandardAndOptionalStandardReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestStandardAndOptionalStandardReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(cert -> meetsCriteriaAndHasBothTestStandardsAndOptionalStandards(cert))
            .forEach(cert -> addErrors(listing, cert));
    }

    private boolean meetsCriteriaAndHasBothTestStandardsAndOptionalStandards(CertificationResult cert) {
        return cert.isSuccess() != null
                && cert.isSuccess().equals(Boolean.TRUE)
                && !CollectionUtils.isEmpty(cert.getTestStandards())
                && !CollectionUtils.isEmpty(cert.getOptionalStandards());
    }

    private void addErrors(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        listing.getErrorMessages().add(
                msgUtil.getMessage("listing.criteria.hasBothTestAndOptionalStandards",
                        Util.formatCriteriaNumber(certResult.getCriterion())));
    }
}
