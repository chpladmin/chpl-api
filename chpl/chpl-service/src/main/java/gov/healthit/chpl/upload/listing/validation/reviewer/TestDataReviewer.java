package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

@Component("listingUploadTestDataReviewer")
public class TestDataReviewer extends PermissionBasedReviewer {
    private CertificationResultRules certResultRules;
    private CertificationCriterionService criteriaSevice;

    @Autowired
    public TestDataReviewer(CertificationResultRules certResultRules,
            CertificationCriterionService criteriaSevice,
            ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.certResultRules = certResultRules;
        this.criteriaSevice = criteriaSevice;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .forEach(certResult -> review(listing, certResult));
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.isSuccess() != null && certResult.isSuccess()) {
            reviewTestDataRequiredForG1AndG2WhenCertResultIsNotGap(listing, certResult);
            reviewReplacedTestData(listing, certResult);
            if (certResult.getTestDataUsed() != null && certResult.getTestDataUsed().size() > 0) {
                certResult.getTestDataUsed().stream()
                    .forEach(testData -> reviewTestDataFields(listing, certResult, testData));
            }
        }
    }

    private void reviewReplacedTestData(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.getTestDataUsed() == null || certResult.getTestDataUsed().size() == 0) {
            return;
        }
        certResult.getTestDataUsed().stream()
            .filter(testData -> !StringUtils.isEmpty(testData.getUserEnteredName())
                    && !testData.getTestData().getName().equals(testData.getUserEnteredName()))
            .forEach(testData -> addWarningMessageForTestDataReplacement(listing, certResult, testData));
    }

    private void addWarningMessageForTestDataReplacement(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestData testData) {
        listing.getWarningMessages().add(
                msgUtil.getMessage("listing.criteria.badTestDataName",
                        testData.getUserEnteredName(),
                        Util.formatCriteriaNumber(certResult.getCriterion()),
                        testData.getTestData().getName()));
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
        if (testData.getTestData() != null && StringUtils.isEmpty(testData.getUserEnteredName())
                && !StringUtils.isEmpty(testData.getTestData().getName())) {
            listing.getWarningMessages().add(msgUtil.getMessage("listing.criteria.missingTestDataName",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    testData.getTestData().getName()));
        }
    }

    private void reviewVersionRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestData testData) {
        if (testData.getTestData() != null && !StringUtils.isEmpty(testData.getTestData().getName())
                && StringUtils.isEmpty(testData.getVersion())) {
            addCriterionErrorOrWarningByPermission(listing, certResult,
                    "listing.criteria.missingTestDataVersion",
                    Util.formatCriteriaNumber(certResult.getCriterion()));
        }
    }
}
