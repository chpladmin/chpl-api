package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMipsMeasure;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;

@Component("mipsMeasureComparisonReviewer")
public class MipsMeasureComparisonReviewer implements ComparisonReviewer {
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public MipsMeasureComparisonReviewer(ResourcePermissions resourcePermissions,
            ErrorMessageUtil msgUtil) {
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing,
            final CertifiedProductSearchDetails updatedListing) {
        // checking for the addition of a removed mips measure.
        // this is only disallowed if the user is not ADMIN/ONC, so first check the permissions
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        }

        List<ListingMipsMeasure> existingMacraMeasuresForListing = existingListing.getMipsMeasures();
        List<ListingMipsMeasure> updatedMacraMeasuresForCriterion = updatedListing.getMipsMeasures();

        getNewlyAddedRemovedItems(updatedMacraMeasuresForCriterion, existingMacraMeasuresForListing).stream()
                .forEach(mm -> updatedListing.getErrorMessages()
                        .add(getErrorMessage("listing.removedMipsMeasure", mm)));
    }

    private List<ListingMipsMeasure> getNewlyAddedRemovedItems(List<ListingMipsMeasure> listInUpdatedListing,
            List<ListingMipsMeasure> listInOriginalListing) {

        Predicate<ListingMipsMeasure> notInOriginalListing = updated -> !listInOriginalListing.stream()
                .anyMatch(original -> updated.getId().equals(original.getId()));

        return listInUpdatedListing.stream()
                .filter(notInOriginalListing)
                .filter(mm -> mm.getMeasure().getRemoved())
                .collect(Collectors.toList());
    }

    private String getErrorMessage(String messageCode, ListingMipsMeasure listingMipsMeasure) {
        return msgUtil.getMessage(messageCode,
                listingMipsMeasure.getMeasure().getAbbreviation());
    }

}
