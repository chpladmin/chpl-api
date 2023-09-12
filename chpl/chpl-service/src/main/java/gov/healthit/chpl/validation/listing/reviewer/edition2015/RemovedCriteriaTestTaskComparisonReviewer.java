package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;

/**
 * This reviewer confirms that an ACB user does not attempt to associate a removed criteria with a Test Task.
 *
 * @author kekey
 *
 */
@Component("removedCriteriaTestTaskComparisonReviewer")
public class RemovedCriteriaTestTaskComparisonReviewer implements ComparisonReviewer {
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public RemovedCriteriaTestTaskComparisonReviewer(ResourcePermissions resourcePermissions,
            ErrorMessageUtil msgUtil) {
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        // this is only disallowed if the user is not ADMIN/ONC, so first check the permissions
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        }

        for (TestTask updatedTestTask : updatedListing.getSed().getTestTasks()) {
            boolean existsInOriginal = false;
            for (TestTask existingTestTask : existingListing.getSed().getTestTasks()) {
                if (updatedTestTask.matches(existingTestTask)) {
                    existsInOriginal = true;
                    // not a new Test Task, but maybe there are new criteria?
                    Set<CertificationCriterion> addedCriteria = getAddedCriteria(existingTestTask.getCriteria(),
                            updatedTestTask.getCriteria());
                    for (CertificationCriterion addedCriterion : addedCriteria) {
                        if (addedCriterion.isRemoved() != null
                                && addedCriterion.isRemoved().booleanValue()) {
                            updatedListing.addBusinessErrorMessage(
                                    msgUtil.getMessage("listing.testTask.removedCriteriaNotAllowed",
                                            Util.formatCriteriaNumber(addedCriterion), updatedTestTask.getDescription()));
                        }
                    }
                }
            }
            if (!existsInOriginal) {
                // check all the criteria for this newly added test task to see if any are removed
                for (CertificationCriterion criterion : updatedTestTask.getCriteria()) {
                    if (criterion.isRemoved() != null && criterion.isRemoved().booleanValue()) {
                        updatedListing.addBusinessErrorMessage(
                                msgUtil.getMessage("listing.testTask.removedCriteriaNotAllowed",
                                        Util.formatCriteriaNumber(criterion), updatedTestTask.getDescription()));
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
