package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

/**
 * Determines if a removed criteria is allowed with this pending listing.
 * @author kekey
 *
 */
@Component("removedCriteriaReviewer")
public class RemovedCriteriaReviewer implements Reviewer {
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public RemovedCriteriaReviewer(ResourcePermissions resourcePermissions, ErrorMessageUtil msgUtil) {
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
    }

    public void review(final PendingCertifiedProductDTO listing) {
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        } else if (resourcePermissions.isUserRoleAcbAdmin()) {
            for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
                if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria().booleanValue()
                        && (listing.getIcs() == null || !listing.getIcs().booleanValue())
                        && cert.getCriterion().getRemoved()) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.removedCriteriaAddNotAllowed",
                                    Util.formatCriteriaNumber(cert.getCriterion())));
                }
            }
        }
    }
}
