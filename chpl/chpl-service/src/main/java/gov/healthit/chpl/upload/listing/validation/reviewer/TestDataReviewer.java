package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

@Component("listingUploadTestDataReviewer")
public class TestDataReviewer {
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private CertificationCriterionService criteriaSevice;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestDataReviewer(CertificationResultRules certResultRules,
            ValidationUtils validationUtils,
            CertificationCriterionService criteriaSevice,
            ErrorMessageUtil msgUtil) {
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.criteriaSevice = criteriaSevice;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> validationUtils.isEligibleForErrors(certResult))
            .forEach(certResult -> review(listing, certResult));
    }

    private void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveTestData(listing, certResult);
        removeTestDataWithoutIds(listing, certResult);
        reviewTestDataRequiredForG1AndG2WhenCertResultIsNotGap(listing, certResult);
        if (certResult.getTestDataUsed() != null && certResult.getTestDataUsed().size() > 0) {
            certResult.getTestDataUsed().stream()
                .forEach(testData -> reviewTestDataFields(listing, certResult, testData));
        }
    }

    private void reviewCriteriaCanHaveTestData(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.TEST_DATA)) {
            if (!CollectionUtils.isEmpty(certResult.getTestDataUsed())) {
                listing.getWarningMessages().add(msgUtil.getMessage(
                    "listing.criteria.testDataNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setTestDataUsed(null);
        }
    }

    private void removeTestDataWithoutIds(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (CollectionUtils.isEmpty(certResult.getTestDataUsed())) {
            return;
        }
        Iterator<CertificationResultTestData> testDataIter = certResult.getTestDataUsed().iterator();
        while (testDataIter.hasNext()) {
            CertificationResultTestData testData = testDataIter.next();
            if (testData.getTestData() != null && testData.getTestData().getId() == null
                    && !StringUtils.isEmpty(testData.getTestData().getName())) {
                testDataIter.remove();
                listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.invalidTestDataRemoved",
                        testData.getTestData().getName(),
                        Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

    private void reviewTestDataRequiredForG1AndG2WhenCertResultIsNotGap(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        CertificationCriterion g1 = criteriaSevice.get(Criteria2015.G_1);
        CertificationCriterion g2 = criteriaSevice.get(Criteria2015.G_2);

        if (!isGapEligibileAndHasGap(certResult)
                && (certResult.getCriterion().getId().equals(g1.getId()) || certResult.getCriterion().getId().equals(g2.getId()))
                && (certResult.getTestDataUsed() == null || certResult.getTestDataUsed().size() == 0)) {
                    listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.testDataRequired",
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

    private void reviewTestDataFields(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestData testData) {
        reviewNameRequired(listing, certResult, testData);
        reviewVersionRequired(listing, certResult, testData);
    }

    private void reviewNameRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestData testData) {
        if (testData.getTestData() != null && !StringUtils.isEmpty(testData.getVersion())
                && StringUtils.isEmpty(testData.getTestData().getName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingTestDataName",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewVersionRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestData testData) {
        if (testData.getTestData() != null && !StringUtils.isEmpty(testData.getTestData().getName())
                && StringUtils.isEmpty(testData.getVersion())) {
            listing.getErrorMessages().add(msgUtil.getMessage(
                    "listing.criteria.missingTestDataVersion",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }
}
