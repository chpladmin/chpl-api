package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.testdata.CertificationResultTestData;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("listingUploadTestDataReviewer")
public class TestDataReviewer implements Reviewer {
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;
    private Long gapConformanceMethodId;
    private CertificationCriterion g1, g2;

    @Autowired
    public TestDataReviewer(CertificationResultRules certResultRules,
            ValidationUtils validationUtils,
            CertificationCriterionService criteriaSevice,
            ErrorMessageUtil msgUtil,
            @Value("${conformancemethod.gap}") Long gapConformanceMethodId) {
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;

        this.gapConformanceMethodId = gapConformanceMethodId;
        g1 = criteriaSevice.get(Criteria2015.G_1);
        g2 = criteriaSevice.get(Criteria2015.G_2);
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
                .filter(certResult -> validationUtils.isEligibleForErrors(certResult))
                .forEach(certResult -> review(listing, certResult));
        listing.getCertificationResults().stream()
                .forEach(certResult -> removeTestDataIfNotApplicable(certResult));
    }

    private void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveTestData(listing, certResult);
        removeTestDataWithoutIds(listing, certResult);
        reviewTestDataRequiredForG1AndG2(listing, certResult);
        if (certResult.getTestDataUsed() != null && certResult.getTestDataUsed().size() > 0) {
            certResult.getTestDataUsed().stream()
                    .forEach(testData -> reviewTestDataFields(listing, certResult, testData));
        }
    }

    private void reviewCriteriaCanHaveTestData(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.TEST_DATA)) {
            if (!CollectionUtils.isEmpty(certResult.getTestDataUsed())) {
                listing.addWarningMessage(msgUtil.getMessage(
                        "listing.criteria.testDataNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setTestDataUsed(null);
        }
    }

    private void removeTestDataIfNotApplicable(CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.TEST_DATA)) {
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
                listing.addWarningMessage(msgUtil.getMessage("listing.criteria.invalidTestDataRemoved",
                        testData.getTestData().getName(),
                        Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

    private void reviewTestDataRequiredForG1AndG2(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (isCriteriaG1OrG2(certResult)
                && !hasGapConformanceMethod(certResult)
                && CollectionUtils.isEmpty(certResult.getTestDataUsed())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.testDataRequired",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private boolean isCriteriaG1OrG2(CertificationResult certResult) {
        return certResult.getCriterion().getId().equals(g1.getId())
                || certResult.getCriterion().getId().equals(g2.getId());
    }

    private boolean hasGapConformanceMethod(CertificationResult certResult) {
        if (CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
            return false;
        }

        return certResult.getConformanceMethods().stream()
                .filter(cm -> cm.getConformanceMethod().getId().equals(gapConformanceMethodId))
                .findAny()
                .isPresent();
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
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.missingTestDataName",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewVersionRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestData testData) {
        if (testData.getTestData() != null && !StringUtils.isEmpty(testData.getTestData().getName())
                && StringUtils.isEmpty(testData.getVersion())) {
            listing.addDataErrorMessage(msgUtil.getMessage(
                    "listing.criteria.missingTestDataVersion",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }
}
