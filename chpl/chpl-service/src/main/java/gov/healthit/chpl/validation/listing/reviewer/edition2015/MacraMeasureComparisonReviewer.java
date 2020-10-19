package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.MipsMeasure;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;

@Component("macraMeasureComparisonReviewer")
public class MacraMeasureComparisonReviewer implements ComparisonReviewer {
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public MacraMeasureComparisonReviewer(ResourcePermissions resourcePermissions,
            ErrorMessageUtil msgUtil) {
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing,
            final CertifiedProductSearchDetails updatedListing) {
        // checking for the addition of a removed macra measure.
        // this is only disallowed if the user is not ADMIN/ONC, so first check the permissions
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        }

        List<MipsMeasure> existingMacraMeasuresForListing = existingListing.getG1MacraMeasures();
        List<MipsMeasure> updatedMacraMeasuresForCriterion = updatedListing.getG1MacraMeasures();

        // Was a G1 item added?
        getNewlyAddedRemovedItems(updatedMacraMeasuresForCriterion, existingMacraMeasuresForListing).stream()
                .forEach(mm -> updatedListing.getErrorMessages()
                        .add(getErrorMessage("listing.criteria.removedG1MacraMeasure", mm)));

        // Was a G2 item added?
        existingMacraMeasuresForListing = existingListing.getG2MacraMeasures();
        updatedMacraMeasuresForCriterion = updatedListing.getG2MacraMeasures();
        getNewlyAddedRemovedItems(updatedMacraMeasuresForCriterion, existingMacraMeasuresForListing).stream()
                .forEach(mm -> updatedListing.getErrorMessages()
                        .add(getErrorMessage("listing.criteria.removedG2MacraMeasure", mm)));
    }

    private List<MipsMeasure> getNewlyAddedRemovedItems(List<MipsMeasure> listInUpdatedListing,
            List<MipsMeasure> listInOriginalListing) {

        Predicate<MipsMeasure> notInOriginalListing = updated -> !listInOriginalListing.stream()
                .anyMatch(original -> updated.getId().equals(original.getId()));

        return listInUpdatedListing.stream()
                .filter(notInOriginalListing)
                .filter(mm -> mm.getRemoved())
                .collect(Collectors.toList());
    }

    private String getErrorMessage(String messageCode, MipsMeasure macraMeasure) {
        return msgUtil.getMessage(messageCode,
                macraMeasure.getAbbreviation());
    }

}
