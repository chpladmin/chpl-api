package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;

@Component("unavailableCriteriaTestTaskComparisonReviewer")
public class UnavailableCriteriaTestTaskComparisonReviewer implements ComparisonReviewer {
    private ResourcePermissionsFactory resourcePermissionsFactory;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public UnavailableCriteriaTestTaskComparisonReviewer(ResourcePermissionsFactory resourcePermissionsFactory, ErrorMessageUtil msgUtil) {
        this.resourcePermissionsFactory = resourcePermissionsFactory;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        // this is only disallowed if the user is not ADMIN/ONC, so first check the permissions
        if (resourcePermissionsFactory.get().isUserRoleAdmin() || resourcePermissionsFactory.get().isUserRoleOnc()) {
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
                        if (!isCriterionAvailable(updatedListing, addedCriterion)) {
                            updatedListing.addBusinessErrorMessage(
                                    msgUtil.getMessage("listing.testTask.unavailableCriteriaNotAllowed",
                                            Util.formatCriteriaNumber(addedCriterion),
                                            updatedTestTask.getDescription()));
                        }
                    }
                }
            }
            if (!existsInOriginal) {
                // check all the criteria for this newly added test task to see if any are not applicable to the listing
                for (CertificationCriterion criterion : updatedTestTask.getCriteria()) {
                    if (!isCriterionAvailable(updatedListing, criterion)) {
                        updatedListing.addBusinessErrorMessage(
                                msgUtil.getMessage("listing.testTask.unavailableCriteriaNotAllowed",
                                        Util.formatCriteriaNumber(criterion),
                                        updatedTestTask.getDescription()));
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

    private boolean isCriterionAvailable(CertifiedProductSearchDetails listing,
            CertificationCriterion criterion) {

        return DateUtil.datesOverlap(Pair.of(listing.getCertificationDay(), listing.getDecertificationDay()),
                        Pair.of(criterion.getStartDay(), criterion.getEndDay()));
    }
}
