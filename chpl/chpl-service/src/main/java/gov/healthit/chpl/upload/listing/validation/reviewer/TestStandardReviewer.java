package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
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
            .filter(certResult -> certResult.isSuccess() != null && certResult.isSuccess())
            .forEach(certResult -> review(listing, certResult));
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveTestStandards(listing, certResult);
        if (certResult.getTestStandards() != null && certResult.getTestStandards().size() > 0) {
            certResult.getTestStandards().stream()
                .forEach(testStandard -> reviewTestStandardFields(listing, certResult, testStandard));
        }
    }

    private void reviewCriteriaCanHaveTestStandards(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.STANDARDS_TESTED)
                && certResult.getTestStandards() != null && certResult.getTestStandards().size() > 0) {
            listing.getErrorMessages().add(msgUtil.getMessage(
                    "listing.criteria.testStandardsNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewTestStandardFields(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestStandard testStandard) {
        reviewIdRequired(listing, certResult, testStandard);
        reviewNameRequired(listing, certResult, testStandard);
    }

    private void reviewIdRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestStandard testStandard) {
        if (testStandard.getTestStandardId() == null
                && !StringUtils.isEmpty(testStandard.getTestStandardName())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.testStandardNotFound",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    testStandard.getTestStandardName(),
                    MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY)));
        }
    }

    private void reviewNameRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestStandard testStandard) {
        if (StringUtils.isEmpty(testStandard.getTestStandardName())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingTestStandardName",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }
}
