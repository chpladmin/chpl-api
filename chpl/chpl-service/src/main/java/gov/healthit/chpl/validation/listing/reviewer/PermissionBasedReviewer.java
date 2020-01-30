package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("pendingPermissionBasedReviewer")
public abstract class PermissionBasedReviewer implements Reviewer {
    protected ErrorMessageUtil msgUtil;
    protected  ResourcePermissions resourcePermissions;

    @Autowired
    public PermissionBasedReviewer(ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
    }

    public void addListingWarningByPermission(CertifiedProductSearchDetails listing, String errorMessageName,
            Object... errorMessageArgs) {
        String message = msgUtil.getMessage(errorMessageName, errorMessageArgs);
        addListingWarningByPermission(listing, message);
    }

    public void addListingWarningByPermission(CertifiedProductSearchDetails listing, String message) {
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
                listing.getWarningMessages().add(message);
                //ACBs do not get any error or warning about removed criteria validation issues
        }
    }

    public void addCriterionErrorOrWarningByPermission(
            CertifiedProductSearchDetails listing, CertificationResult certResult,
            String errorMessageName, Object... errorMessageArgs) {
        String message = msgUtil.getMessage(errorMessageName, errorMessageArgs);
        addCriterionErrorOrWarningByPermission(listing, certResult, message);
    }

    public void addCriterionErrorOrWarningByPermission(
            CertifiedProductSearchDetails listing, CertificationResult certResult,
            String message) {
        if (certResult.getCriterion() != null && certResult.getCriterion().getRemoved() != null
                && certResult.getCriterion().getRemoved().equals(Boolean.TRUE)
                && (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc())) {
                listing.getWarningMessages().add(message);
                //ACBs do not get any error or warning about removed criteria validation issues
        } else if (certResult.getCriterion() != null && (certResult.getCriterion().getRemoved() == null
                || certResult.getCriterion().getRemoved().equals(Boolean.FALSE))) {
            listing.getErrorMessages().add(message);
        }
    }

    @Override
    public abstract void review(CertifiedProductSearchDetails listing);
}
