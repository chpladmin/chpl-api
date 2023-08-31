package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;

/**
 * This reviewer confirms that an ACB user does not attempt to associate a removed criteria with a UCD Process.
 *
 * @author kekey
 *
 */
@Component("removedCriteriaUcdComparisonReviewer")
public class RemovedCriteriaUcdComparisonReviewer implements ComparisonReviewer {
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public RemovedCriteriaUcdComparisonReviewer(ResourcePermissions resourcePermissions,
            ErrorMessageUtil msgUtil) {
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(final CertifiedProductSearchDetails existingListing,
            final CertifiedProductSearchDetails updatedListing) {
        // this is only disallowed if the user is not ADMIN/ONC, so first check the permissions
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        }

        for (CertifiedProductUcdProcess updatedUcd : updatedListing.getSed().getUcdProcesses()) {
            boolean existsInOriginal = false;
            for (CertifiedProductUcdProcess existingUcd : existingListing.getSed().getUcdProcesses()) {
                if (updatedUcd.matches(existingUcd)) {
                    existsInOriginal = true;
                    // not a new UCD Process, but maybe there are new criteria?
                    Set<CertificationCriterion> addedCriteria = getAddedCriteria(existingUcd.getCriteria(),
                            updatedUcd.getCriteria());
                    for (CertificationCriterion addedCriterion : addedCriteria) {
                        if (addedCriterion.getRemoved() != null
                                && addedCriterion.getRemoved().booleanValue()) {
                            updatedListing.addBusinessErrorMessage(
                                    msgUtil.getMessage("listing.ucd.removedCriteriaNotAllowed",
                                            Util.formatCriteriaNumber(addedCriterion), updatedUcd.getName()));
                        }
                    }
                }
            }
            if (!existsInOriginal) {
                // check all the criteria for this newly added UCD process to see if any are removed
                for (CertificationCriterion criterion : updatedUcd.getCriteria()) {
                    if (criterion.getRemoved() != null && criterion.getRemoved().booleanValue()) {
                        updatedListing.addBusinessErrorMessage(
                                msgUtil.getMessage("listing.ucd.removedCriteriaNotAllowed",
                                        Util.formatCriteriaNumber(criterion), updatedUcd.getName()));
                    }
                }
            }
        }
    }

    private Set<CertificationCriterion> getAddedCriteria(Set<CertificationCriterion> originalCriteria,
            Set<CertificationCriterion> updatedCriteria) {
        if (originalCriteria == null || originalCriteria.size() == 0) {
            return updatedCriteria;
        }

        Set<CertificationCriterion> newCriteria = new HashSet<CertificationCriterion>();
        for (CertificationCriterion updatedCriterion : updatedCriteria) {
            boolean existsInOriginal = false;
            for (CertificationCriterion originalCriterion : originalCriteria) {
                if (updatedCriterion.getId().equals(originalCriterion.getId())) {
                    existsInOriginal = true;
                }
            }
            if (!existsInOriginal) {
                newCriteria.add(updatedCriterion);
            }
        }
        return newCriteria;
    }
}
