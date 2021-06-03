package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

@Component("listingUploadCriteriaReviewer")
public class CriteriaReviewer extends PermissionBasedReviewer {

    @Autowired
    public CriteriaReviewer(ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        super(msgUtil, resourcePermissions);
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> certResult.isSuccess() != null && certResult.isSuccess())
            .forEach(certResult -> review(listing, certResult));
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewRemovedCriteriaAllowedForRole(listing, certResult);
        //TODO: criteria relationship reviews may go here in the future
    }

    private void reviewRemovedCriteriaAllowedForRole(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        } else if (resourcePermissions.isUserRoleAcbAdmin()) {
            if ((listing.getIcs() == null || listing.getIcs().getInherits() == null
                    || !listing.getIcs().getInherits()) && certResult.getCriterion().getRemoved()) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.removedCriteriaAddNotAllowed",
                                Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

}
