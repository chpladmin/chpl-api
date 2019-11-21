package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Determines if a removed criteria is allowed with this pending listing.
 * @author kekey
 *
 */
@Component("removedCriteriaReviewer")
public class RemovedCriteriaReviewer implements Reviewer {
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;

    @Autowired
    public RemovedCriteriaReviewer(final ResourcePermissions resourcePermissions,
            final ErrorMessageUtil msgUtil,
            final FF4j ff4j) {
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
    }

    public void review(final PendingCertifiedProductDTO listing) {
        if (!ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
            return;
        }

        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        } else if (resourcePermissions.isUserRoleAcbAdmin()) {
            for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
                if (cert.getMeetsCriteria() != null && cert.getMeetsCriteria().booleanValue()
                        && (listing.getIcs() == null || !listing.getIcs().booleanValue())
                        && cert.getCriterion().getRemoved()) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.removedCriteriaNotAllowed", cert.getCriterion().getNumber()));
                }
            }
        }
    }
}
