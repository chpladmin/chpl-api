package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;

@Component("measureComparisonReviewer")
public class MeasureComparisonReviewer implements ComparisonReviewer {
    private ResourcePermissionsFactory resourcePermissionsFactory;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public MeasureComparisonReviewer(ResourcePermissionsFactory resourcePermissionsFactory,
            ErrorMessageUtil msgUtil) {
        this.resourcePermissionsFactory = resourcePermissionsFactory;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        // checking for the addition of a removed measure.
        // this is only disallowed if the user is not ADMIN/ONC, so first check the permissions
        if (resourcePermissionsFactory.get().isUserRoleAdmin() || resourcePermissionsFactory.get().isUserRoleOnc()) {
            return;
        }

        List<ListingMeasure> existingMeasuresForListing = existingListing.getMeasures();
        List<ListingMeasure> updatedMeasuresForListing = updatedListing.getMeasures();

        getNewlyAddedRemovedItems(updatedMeasuresForListing, existingMeasuresForListing).stream()
                .forEach(mm -> updatedListing.addBusinessErrorMessage(getErrorMessage("listing.removedMeasure", mm)));
    }

    private List<ListingMeasure> getNewlyAddedRemovedItems(List<ListingMeasure> listInUpdatedListing,
            List<ListingMeasure> listInOriginalListing) {

        Predicate<ListingMeasure> notInOriginalListing = updated -> !listInOriginalListing.stream()
                .anyMatch(original -> updated.getId() != null && updated.getId().equals(original.getId()));

        return listInUpdatedListing.stream()
                .filter(notInOriginalListing)
                .filter(mm -> mm.getMeasure().getRemoved())
                .collect(Collectors.toList());
    }

    private String getErrorMessage(String messageCode, ListingMeasure listingMeasure) {
        return msgUtil.getMessage(messageCode,
                listingMeasure.getMeasureType().getName(),
                listingMeasure.getMeasure().getName(),
                listingMeasure.getMeasure().getAbbreviation());
    }

}
