package gov.healthit.chpl.validation.pendingListing.reviewer;

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

    public void addRemovedCriteriaWarningByPermission(
            PendingCertifiedProductDTO listing, PendingCertificationResultDTO certResult,
            String errorMessageName, Object... errorMessageArgs) {
        if (certResult.getCriterion() != null && certResult.getCriterion().getRemoved() != null
                && certResult.getCriterion().getRemoved().equals(Boolean.TRUE)) {
            if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
                listing.getWarningMessages().add(msgUtil.getMessage(errorMessageName, errorMessageArgs));
            }
            //ACBs do not get any error or warning about removed criteria issues
        }

    }

    @Override
    public abstract void review(final PendingCertifiedProductDTO listing);
}
