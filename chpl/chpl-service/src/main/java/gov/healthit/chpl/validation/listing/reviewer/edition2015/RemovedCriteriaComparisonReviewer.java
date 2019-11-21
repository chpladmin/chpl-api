package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;

/**
 * This reviewer confirms that an ACB user does not attempt to add a 'removed' criteria
 * to a listing.
 * @author kekey
 *
 */
@Component("removedCriteriaComparisonReviewer")
public class RemovedCriteriaComparisonReviewer implements ComparisonReviewer {
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public RemovedCriteriaComparisonReviewer(final ResourcePermissions resourcePermissions,
            final ErrorMessageUtil msgUtil) {
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(final CertifiedProductSearchDetails existingListing,
            final CertifiedProductSearchDetails updatedListing) {
        //checking for the addition of a removed criteria
        //this is only disallowed if the user is not ADMIN/ONC, so first check the permissions
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        }

        for (CertificationResult updatedCert : updatedListing.getCertificationResults()) {
            for (CertificationResult existingCert : existingListing.getCertificationResults()) {
                //find matching criteria in existing/updated listings
                if (!StringUtils.isEmpty(updatedCert.getNumber()) && !StringUtils.isEmpty(existingCert.getNumber())
                        && updatedCert.getNumber().equals(existingCert.getNumber()) && updatedCert.isSuccess()) {
                    if (isCertAdded(existingCert, updatedCert)) {
                        updatedListing.getErrorMessages().add(
                                msgUtil.getMessage("listing.removedCriteriaNotAllowed", updatedCert.getNumber()));
                    }
                }
            }
        }
    }

    private boolean isCertAdded(final CertificationResult existingCert,
            final CertificationResult updatedCert) {
        return (existingCert.isSuccess() == null || !existingCert.isSuccess())
                && (updatedCert.isSuccess() != null && updatedCert.isSuccess());
    }
}
