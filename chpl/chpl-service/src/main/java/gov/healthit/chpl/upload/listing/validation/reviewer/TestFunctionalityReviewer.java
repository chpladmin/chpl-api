package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.TestFunctionalityAllowedByCriteriaReviewer;

@Component("listingUploadTestFunctionalityReviewer")
public class TestFunctionalityReviewer extends PermissionBasedReviewer {
    private CertificationResultRules certResultRules;
    private TestFunctionalityAllowedByCriteriaReviewer testFunctionalityCriteriaReviewer;

    @Autowired
    public TestFunctionalityReviewer(CertificationResultRules certResultRules,
            TestFunctionalityAllowedByCriteriaReviewer testFunctionalityCriteriaReviewer,
            ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
        this.certResultRules = certResultRules;
        this.testFunctionalityCriteriaReviewer = testFunctionalityCriteriaReviewer;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .forEach(certResult -> review(listing, certResult));
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.isSuccess() != null && certResult.isSuccess()) {
            removeTestFunctionalityWithoutIds(listing, certResult);
            reviewTestFunctionalityApplicableToCriteria(listing, certResult);
            if (certResult.getTestFunctionality() != null && certResult.getTestFunctionality().size() > 0) {
                certResult.getTestFunctionality().stream()
                    .forEach(testFunc -> reviewTestFunctionalityFields(listing, certResult, testFunc));
            }
        }
    }

    private void reviewTestFunctionalityApplicableToCriteria(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)
                && certResult.getTestFunctionality() != null && certResult.getTestFunctionality().size() > 0) {
            listing.getErrorMessages().add(msgUtil.getMessage(
                    "listing.criteria.testFunctionalityNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void removeTestFunctionalityWithoutIds(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.getTestFunctionality() == null || certResult.getTestFunctionality().size() == 0) {
            return;
        }
        Iterator<CertificationResultTestFunctionality> testFunctionalityIter = certResult.getTestFunctionality().iterator();
        while (testFunctionalityIter.hasNext()) {
            CertificationResultTestFunctionality testFunctionality = testFunctionalityIter.next();
            if (testFunctionality.getTestFunctionalityId() == null) {
                testFunctionalityIter.remove();
                addCriterionErrorOrWarningByPermission(listing, certResult, "listing.criteria.testFunctionalityNotFoundAndRemoved",
                        Util.formatCriteriaNumber(certResult.getCriterion()), testFunctionality.getName());
            }
        }
    }

    private void reviewTestFunctionalityFields(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultTestFunctionality testFunctionality) {
        if (StringUtils.isEmpty(testFunctionality.getName())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingTestFunctionalityName",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }
}
