package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("listingUploadRemovedCriteriaReviewer")
public class RemovedCriteriaReviewer {
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public RemovedCriteriaReviewer(ResourcePermissions resourcePermissions, ErrorMessageUtil msgUtil) {
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (doesNotInherit(listing) && listing.getCertificationResults() != null
                && resourcePermissions.isUserRoleAcbAdmin()) {
            listing.getCertificationResults().stream()
                .filter(certResult -> certificationResultIsRemovedAndAttested(certResult))
                .forEach(removedAttestedCertResult -> listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.removedCriteriaAddNotAllowed",
                                Util.formatCriteriaNumber(removedAttestedCertResult.getCriterion()))));
        }
    }
    private boolean doesNotInherit(CertifiedProductSearchDetails listing) {
        return listing.getIcs() == null
                || listing.getIcs().getInherits() == null
                || !listing.getIcs().getInherits();
    }

    private boolean certificationResultIsRemovedAndAttested(CertificationResult certResult) {
        return BooleanUtils.isTrue(certResult.isSuccess())
                && certResult.getCriterion() != null && certResult.getCriterion().getRemoved() != null
                && certResult.getCriterion().getRemoved();
    }
}
