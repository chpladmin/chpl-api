package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("listingUploadTestDataReviewer")
public class TestDataReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestDataReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
                .forEach(certResult -> removeTestData(listing, certResult));
    }


    private void removeTestData(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!CollectionUtils.isEmpty(certResult.getTestDataUsed())) {
            listing.addWarningMessage(msgUtil.getMessage(
                    "listing.criteria.testDataNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
        }
        certResult.setTestDataUsed(null);    }
}
