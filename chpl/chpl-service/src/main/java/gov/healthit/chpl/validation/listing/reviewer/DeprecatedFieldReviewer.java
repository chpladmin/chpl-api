package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

@Component("deprecatedFieldReviewer")
public class DeprecatedFieldReviewer implements ComparisonReviewer {

    @Autowired
    public DeprecatedFieldReviewer() {
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing) {
        if (!existingListing.getTransparencyAttestationUrl().equals(updatedListing.getTransparencyAttestationUrl())) {
            updatedListing.getWarningMessages()
            .add("The deprecated field \"transparencyAttestationUrl\" will not be used when upating a Certified Product");
        }
    }
}
