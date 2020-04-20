package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * This reviewer confirms that an ACB user does not attempt to add a 'removed' macra measure to any certification
 * result.
 * 
 * @author kekey
 *
 */
@Component("macraMeasureComparisonReviewer")
public class MacraMeasureComparisonReviewer implements ComparisonReviewer {
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public MacraMeasureComparisonReviewer(final ResourcePermissions resourcePermissions,
            final ErrorMessageUtil msgUtil) {
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(final CertifiedProductSearchDetails existingListing,
            final CertifiedProductSearchDetails updatedListing) {
        // checking for the addition of a removed macra measure.
        // this is only disallowed if the user is not ADMIN/ONC, so first check the permissions
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        }

        List<FlatMacraMeasure> existingListingMacraMeasures = getFlattenedG1MacraMeasures(
                existingListing.getCertificationResults());
        List<FlatMacraMeasure> updatedListingMacraMeasures = getFlattenedG1MacraMeasures(
                updatedListing.getCertificationResults());

        // Was a G1 item added?
        subtractLists(updatedListingMacraMeasures, existingListingMacraMeasures).stream()
                .forEach(mm -> getErrorMessage("listing.criteria.removedG1MacraMeasure", mm));

        existingListingMacraMeasures = getFlattenedG2MacraMeasures(existingListing.getCertificationResults());
        updatedListingMacraMeasures = getFlattenedG2MacraMeasures(updatedListing.getCertificationResults());

        // Was a G2 item added?
        subtractLists(updatedListingMacraMeasures, existingListingMacraMeasures).stream()
                .forEach(mm -> getErrorMessage("listing.criteria.removedG2MacraMeasure", mm));
    }

    private List<FlatMacraMeasure> subtractLists(List<FlatMacraMeasure> listA,
            List<FlatMacraMeasure> listB) {

        Predicate<FlatMacraMeasure> notInListB = fromA -> !listB.stream()
                .anyMatch(fromB -> fromA.getCriterion().getId().equals(fromB.getCriterion().getId())
                        && fromA.getMacraMeasure().getId().equals(fromB.getMacraMeasure().getId()));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private List<FlatMacraMeasure> getFlattenedG1MacraMeasures(List<CertificationResult> results) {
        return results.stream()
                .filter(result -> Objects.nonNull(result.getG1MacraMeasures()))
                .flatMap(result -> result.getG1MacraMeasures().stream()
                        .map(mm -> new FlatMacraMeasure(result.getCriterion(), mm)))
                .collect(Collectors.toList());
    }

    private List<FlatMacraMeasure> getFlattenedG2MacraMeasures(List<CertificationResult> results) {
        return results.stream()
                .filter(result -> Objects.nonNull(result.getG2MacraMeasures()))
                .flatMap(result -> result.getG2MacraMeasures().stream()
                        .map(mm -> new FlatMacraMeasure(result.getCriterion(), mm)))
                .collect(Collectors.toList());
    }

    private String getErrorMessage(String messageCode, FlatMacraMeasure flatMacraMeasure) {
        return msgUtil.getMessage(messageCode,
                CertificationCriterionService.formatCriteriaNumber(flatMacraMeasure.getCriterion()),
                flatMacraMeasure.getMacraMeasure().getAbbreviation());
    }

    @Data
    @AllArgsConstructor
    private class FlatMacraMeasure {
        private CertificationCriterion criterion;
        private MacraMeasure macraMeasure;
    }

}
