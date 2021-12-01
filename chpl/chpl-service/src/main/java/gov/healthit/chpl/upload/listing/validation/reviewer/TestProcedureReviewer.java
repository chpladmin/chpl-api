package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("listingUploadTestProcedureReviewer")
public class TestProcedureReviewer {
    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;

    @Autowired
    public TestProcedureReviewer(CertificationResultRules certResultRules,
            ErrorMessageUtil msgUtil, FF4j ff4j) {
        this.certResultRules = certResultRules;
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
    }

    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess()))
            .forEach(certResult -> review(listing, certResult));
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveTestProcedures(listing, certResult);
        reviewTestProceduresRequiredWhenCertResultIsNotGap(listing, certResult);
        if (!CollectionUtils.isEmpty(certResult.getTestProcedures())) {
            certResult.getTestProcedures().stream()
                .forEach(testProcedure -> reviewTestProcedureFields(listing, certResult, testProcedure));
        }
    }

    private void reviewCriteriaCanHaveTestProcedures(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.TEST_PROCEDURE)) {
            if (!CollectionUtils.isEmpty(certResult.getTestProcedures())) {
                listing.getWarningMessages().add(msgUtil.getMessage(
                    "listing.criteria.testProcedureNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setTestProcedures(null);
        }
    }

    private void reviewTestProceduresRequiredWhenCertResultIsNotGap(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!isGapEligibileAndHasGap(certResult)
                && certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.TEST_PROCEDURE)
                && CollectionUtils.isEmpty(certResult.getTestProcedures())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingTestProcedure",
                        Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private boolean isGapEligibileAndHasGap(CertificationResult certResult) {
        boolean result = false;
        if (certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.GAP)
                && certResult.isGap() != null && certResult.isGap()) {
            result = true;
        }
        return result;
    }

    private void reviewTestProcedureFields(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestProcedure testProcedure) {
        reviewByFlag(listing, certResult, testProcedure);
        reviewIdRequired(listing, certResult, testProcedure);
        reviewNameRequired(listing, certResult, testProcedure);
        reviewVersionRequired(listing, certResult, testProcedure);
    }

    private void reviewByFlag(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestProcedure testProcedure) {
        if (!ff4j.check(FeatureList.CONFORMANCE_METHOD)) {
            return;
        }
        if (testProcedure.getTestProcedure() != null && testProcedure.getTestProcedure().getId() == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.testProcedureNotApplicable",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewIdRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestProcedure testProcedure) {
        if (testProcedure.getTestProcedure() != null && testProcedure.getTestProcedure().getId() == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.badTestProcedureName",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    testProcedure.getTestProcedure().getName()));
        }
    }

    private void reviewNameRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestProcedure testProcedure) {
        if (testProcedure.getTestProcedure() != null && StringUtils.isEmpty(testProcedure.getTestProcedure().getName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingTestProcedureName",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewVersionRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestProcedure testProcedure) {
        if (testProcedure.getTestProcedure() != null && !StringUtils.isEmpty(testProcedure.getTestProcedure().getName())
                && StringUtils.isEmpty(testProcedure.getTestProcedureVersion())) {
            listing.getErrorMessages().add(msgUtil.getMessage(
                    "listing.criteria.missingTestProcedureVersion",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }
}
