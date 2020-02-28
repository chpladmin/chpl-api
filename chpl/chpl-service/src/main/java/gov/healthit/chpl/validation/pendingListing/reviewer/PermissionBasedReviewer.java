package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
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

    public void addListingWarningsByPermission(PendingCertifiedProductDTO listing, List<String> errorMessages) {
        for (String message : errorMessages) {
            addListingWarningByPermission(listing, message);
        }
    }

    public void addListingWarningByPermission(PendingCertifiedProductDTO listing, String errorMessageName,
            Object... errorMessageArgs) {
        String message = msgUtil.getMessage(errorMessageName, errorMessageArgs);
        addListingWarningByPermission(listing, message);
    }

    public void addListingWarningByPermission(PendingCertifiedProductDTO listing, String message) {
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
                listing.getWarningMessages().add(message);
                //ACBs do not get any error or warning about removed criteria validation issues
        }
    }

    public void addErrorOrWarningByPermission(
            PendingCertifiedProductDTO listing, PendingCertificationResultDTO certResult,
            String errorMessageName, Object... errorMessageArgs) {
        if (certResult.getCriterion() != null && certResult.getCriterion().getRemoved() != null
                && certResult.getCriterion().getRemoved().equals(Boolean.TRUE)
                && (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc())) {
                listing.getWarningMessages().add(msgUtil.getMessage(errorMessageName, errorMessageArgs));
                //ACBs do not get any error or warning about removed criteria validation issues
        } else if (certResult.getCriterion() != null && (certResult.getCriterion().getRemoved() == null
                || certResult.getCriterion().getRemoved().equals(Boolean.FALSE))) {
            listing.getErrorMessages().add(msgUtil.getMessage(errorMessageName, errorMessageArgs));
        }
    }

    @Override
    public abstract void review(PendingCertifiedProductDTO listing);
}
