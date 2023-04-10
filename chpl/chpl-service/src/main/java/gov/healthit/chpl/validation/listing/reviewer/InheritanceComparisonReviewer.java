package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("inheritanceComparisonReviewer")
public class InheritanceComparisonReviewer implements ComparisonReviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public InheritanceComparisonReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        reviewNoIcsChildrenRemoved(existingListing, updatedListing);
    }

    private void reviewNoIcsChildrenRemoved(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing) {
        if (existingListing.getIcs() != null && existingListing.getIcs().getChildren() != null) {
            existingListing.getIcs().getChildren().stream()
                    .forEach(icsChild -> reviewIcsChildIsInUpdatedListing(icsChild, updatedListing));
        }
    }

    private void reviewIcsChildIsInUpdatedListing(CertifiedProduct icsChild, CertifiedProductSearchDetails updatedListing) {
        if (updatedListing.getIcs() != null && updatedListing.getIcs().getChildren() != null) {
            boolean isChildPresent = updatedListing.getIcs().getChildren().stream()
                    .filter(updatedIcsChild -> updatedIcsChild.getId().equals(icsChild.getId()))
                    .findAny().isPresent();
            if (!isChildPresent) {
                updatedListing.addDataErrorMessage(
                        msgUtil.getMessage("listing.icsChildRemoved", icsChild.getChplProductNumber()));
            }
        } else {
            updatedListing.addDataErrorMessage(
                    msgUtil.getMessage("listing.icsChildRemoved", icsChild.getChplProductNumber()));
        }
    }
}
