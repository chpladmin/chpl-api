package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("listingUploadTestStandardReviewer")
public class TestStandardReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestStandardReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess()))
            .forEach(certResult -> review(listing, certResult));
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!CollectionUtils.isEmpty(certResult.getTestStandards())) {
            certResult.getTestStandards().stream()
                .forEach(testStandard -> reviewTestStandardsNotAllowed(listing, certResult, testStandard));
        }
    }

    private void reviewTestStandardsNotAllowed(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestStandard testStandard) {
        listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.testStandardNotAllowed",
                Util.formatCriteriaNumber(certResult.getCriterion()),
                testStandard.getTestStandardName()));
    }
}
