package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("listingUploadTestStandardReviewer")
public class TestStandardReviewer implements Reviewer {
    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestStandardReviewer(CertificationResultRules certResultRules,
            ErrorMessageUtil msgUtil) {
        this.certResultRules = certResultRules;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess()))
            .forEach(certResult -> review(listing, certResult));
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.STANDARDS_TESTED)) {
            if (!CollectionUtils.isEmpty(certResult.getTestStandards())) {
                listing.getWarningMessages().add(msgUtil.getMessage(
                    "listing.criteria.testStandardsNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setTestStandards(null);
        } else if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.STANDARDS_TESTED)
            && !CollectionUtils.isEmpty(certResult.getTestStandards())) {
            listing.getWarningMessages().add(msgUtil.getMessage(
                "listing.criteria.testStandardsNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            certResult.getTestStandards().clear();
        }
    }
}
