package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Makes sure the user has the correct privileges to change the status.
 *
 * @author kekey
 *
 */
@Component("developerBanComparisonReviewer")
public class DeveloperBanComparisonReviewer implements ComparisonReviewer {
    private ResourcePermissionsFactory resourcePermissionsFactory;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public DeveloperBanComparisonReviewer(ResourcePermissionsFactory resourcePermissionsFactory, ErrorMessageUtil msgUtil) {
        this.resourcePermissionsFactory = resourcePermissionsFactory;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(final CertifiedProductSearchDetails existingListing,
            final CertifiedProductSearchDetails updatedListing) {
        if (existingListing.getCurrentStatus() != null
                && updatedListing.getCurrentStatus() != null
                && !existingListing.getCurrentStatus().getStatus().getId()
                        .equals(updatedListing.getCurrentStatus().getStatus().getId())) {
            // if the status is to or from suspended by onc make sure the user
            // has admin
            if ((existingListing.getCurrentStatus().getStatus().getName()
                    .equals(CertificationStatusType.SuspendedByOnc.toString())
                    || updatedListing.getCurrentStatus().getStatus().getName()
                            .equals(CertificationStatusType.SuspendedByOnc.toString())
                    || existingListing.getCurrentStatus().getStatus().getName()
                            .equals(CertificationStatusType.TerminatedByOnc.toString())
                    || updatedListing.getCurrentStatus().getStatus().getName()
                            .equals(CertificationStatusType.TerminatedByOnc.toString()))
                    && !resourcePermissionsFactory.get().isUserRoleOnc()
                    && !resourcePermissionsFactory.get().isUserRoleAdmin()) {
                updatedListing.addBusinessErrorMessage(msgUtil.getMessage("listing.certStatusChange.notAllowed",
                        AuthUtil.getUsername(),
                        existingListing.getChplProductNumber(),
                        existingListing.getCurrentStatus().getStatus().getName(),
                        updatedListing.getCurrentStatus().getStatus().getName()));
            }
        }
    }
}
