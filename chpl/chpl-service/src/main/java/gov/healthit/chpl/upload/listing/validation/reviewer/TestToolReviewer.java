package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

@Component("listingUploadTestToolReviewer")
public class TestToolReviewer extends PermissionBasedReviewer {
    private CertificationResultRules certResultRules;
    private ChplProductNumberUtil chplProductNumberUtil;

    @Autowired
    public TestToolReviewer(CertificationResultRules certResultRules,
            ChplProductNumberUtil chplProductNumberUtil,
            ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.certResultRules = certResultRules;
        this.chplProductNumberUtil = chplProductNumberUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .forEach(certResult -> review(listing, certResult));
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.isSuccess() != null && certResult.isSuccess()) {
            removeTestToolsWithoutIds(listing, certResult);
            reviewTestToolsApplicableToCriteria(listing, certResult);
            reviewTestToolsRequiredWhenCertResultIsNotGap(listing, certResult);
            if (certResult.getTestToolsUsed() != null && certResult.getTestToolsUsed().size() > 0) {
                certResult.getTestToolsUsed().stream()
                    .forEach(testTool -> reviewTestToolFields(listing, certResult, testTool));
            }
        }
    }

    private void reviewTestToolsApplicableToCriteria(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.TEST_TOOLS_USED)
                && certResult.getTestToolsUsed() != null && certResult.getTestToolsUsed().size() > 0) {
            listing.getErrorMessages().add(msgUtil.getMessage(
                    "listing.criteria.testToolsNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void removeTestToolsWithoutIds(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.getTestToolsUsed() == null || certResult.getTestToolsUsed().size() == 0) {
            return;
        }
        Iterator<CertificationResultTestTool> testToolIter = certResult.getTestToolsUsed().iterator();
        while (testToolIter.hasNext()) {
            CertificationResultTestTool testTool = testToolIter.next();
            if (testTool.getTestToolId() == null) {
                testToolIter.remove();
                addCriterionErrorOrWarningByPermission(listing, certResult, "listing.criteria.testToolNotFoundAndRemoved",
                        Util.formatCriteriaNumber(certResult.getCriterion()), testTool.getTestToolName());
            }
        }
    }

    private void reviewTestToolsRequiredWhenCertResultIsNotGap(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!isGapEligibileAndHasGap(certResult)
                && certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.TEST_TOOLS_USED)
                && (certResult.getTestToolsUsed() == null || certResult.getTestToolsUsed().size() == 0)) {
                    addCriterionErrorOrWarningByPermission(listing, certResult,
                            "listing.criteria.missingTestTool",
                            Util.formatCriteriaNumber(certResult.getCriterion()));
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

    private void reviewTestToolFields(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestTool testTool) {
        reviewNameAndVersionRequired(listing, certResult, testTool);
        reviewTestToolNotRetiredUnlessIcs(listing, certResult, testTool);
    }

    private void reviewNameAndVersionRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestTool testTool) {
        if (StringUtils.isEmpty(testTool.getTestToolName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingTestToolName",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        } else if (!StringUtils.isEmpty(testTool.getTestToolName())
                && StringUtils.isEmpty(testTool.getTestToolVersion())) {
            // require test tool version if a test tool name was entered
            addCriterionErrorOrWarningByPermission(listing, certResult, "listing.criteria.missingTestToolVersion",
                    testTool.getTestToolName(), Util.formatCriteriaNumber(certResult.getCriterion()));
        }
    }

    private void reviewTestToolNotRetiredUnlessIcs(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestTool testTool) {
        if (testTool.getTestToolId() != null && testTool.isRetired()
                && (!hasIcs(listing) || hasIcsMismatch(listing))) {
            addCriterionErrorOrWarningByPermission(listing, certResult,
                        "listing.criteria.retiredTestToolNoIcsNotAllowed",
                        testTool.getTestToolName(),
                        Util.formatCriteriaNumber(certResult.getCriterion()));
        }
    }

    private Boolean hasIcs(CertifiedProductSearchDetails listing) {
        Integer icsCodeInteger = chplProductNumberUtil.getIcsCode(listing.getChplProductNumber());
        return listing.getIcs() != null && listing.getIcs().getInherits() != null
                && listing.getIcs().getInherits() && !icsCodeInteger.equals(0);
    }

    private Boolean hasIcsMismatch(CertifiedProductSearchDetails listing) {
        boolean icsBoolean = false;
        if (listing.getIcs() != null && listing.getIcs().getInherits() != null
                && listing.getIcs().getInherits()) {
            icsBoolean = true;
        }
        return chplProductNumberUtil.hasIcsConflict(listing.getChplProductNumber(), icsBoolean);
    }
}
