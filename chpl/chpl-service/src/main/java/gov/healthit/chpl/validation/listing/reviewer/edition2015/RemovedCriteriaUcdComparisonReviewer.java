package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.HashSet;
import java.util.Set;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;

/**
 * This reviewer confirms that an ACB user does not attempt to associate
 * a removed criteria with a UCD Process.
 * @author kekey
 *
 */
@Component("removedCriteriaUcdComparisonReviewer")
public class RemovedCriteriaUcdComparisonReviewer implements ComparisonReviewer {
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;

    @Autowired
    public RemovedCriteriaUcdComparisonReviewer(final ResourcePermissions resourcePermissions,
            final ErrorMessageUtil msgUtil, final FF4j ff4j) {
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
    }

    @Override
    public void review(final CertifiedProductSearchDetails existingListing,
            final CertifiedProductSearchDetails updatedListing) {
        if (!ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
            return;
        }

        //this is only disallowed if the user is not ADMIN/ONC, so first check the permissions
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        }

        for (UcdProcess updatedUcd : updatedListing.getSed().getUcdProcesses()) {
            boolean existsInOriginal = false;
            for (UcdProcess existingUcd : existingListing.getSed().getUcdProcesses()) {
                if (updatedUcd.matches(existingUcd)) {
                    existsInOriginal = true;
                    //not a new UCD Process, but maybe there are new criteria?
                    Set<CertificationCriterion> addedCriteria = getAddedCriteria(existingUcd.getCriteria(),
                            updatedUcd.getCriteria());
                    for (CertificationCriterion addedCriterion : addedCriteria) {
                        if (addedCriterion.getRemoved() != null
                                && addedCriterion.getRemoved().booleanValue()) {
                            updatedListing.getErrorMessages().add(
                                    msgUtil.getMessage("listing.ucd.removedCriteriaNotAllowed",
                                    addedCriterion.getNumber(), updatedUcd.getName()));
                        }
                    }
                }
            }
            if (!existsInOriginal) {
                //check all the criteria for this newly added UCD process to see if any are removed
                for (CertificationCriterion criterion : updatedUcd.getCriteria()) {
                    if (criterion.getRemoved() != null && criterion.getRemoved().booleanValue()) {
                        updatedListing.getErrorMessages().add(
                                msgUtil.getMessage("listing.ucd.removedCriteriaNotAllowed", criterion.getNumber(), updatedUcd.getName()));
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
                if (updatedCriterion.getNumber().equals(originalCriterion.getNumber())) {
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
