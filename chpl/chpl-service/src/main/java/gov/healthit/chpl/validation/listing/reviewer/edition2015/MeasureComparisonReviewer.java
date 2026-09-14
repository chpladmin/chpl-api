package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;

@Component("measureComparisonReviewer")
public class MeasureComparisonReviewer implements ComparisonReviewer {
    private ResourcePermissionsFactory resourcePermissionsFactory;
    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;

    @Autowired
    public MeasureComparisonReviewer(ResourcePermissionsFactory resourcePermissionsFactory,
            ErrorMessageUtil msgUtil,
            FF4j ff4j) {
        this.resourcePermissionsFactory = resourcePermissionsFactory;
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        if (ff4j.check(FeatureList.HTI_5_2027_01_01)) {
            return;
        }

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
                .filter(mm -> mm.getMeasure() != null && BooleanUtils.isTrue(mm.getMeasure().getRemoved()))
                .collect(Collectors.toList());
    }

    private String getErrorMessage(String messageCode, ListingMeasure listingMeasure) {
        return msgUtil.getMessage(messageCode,
                listingMeasure.getMeasureType().getName(),
                listingMeasure.getMeasure().getName(),
                listingMeasure.getMeasure().getAbbreviation());
    }

}
