package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("deprecatedFieldReviewer")
public class DeprecatedFieldReviewer implements ComparisonReviewer {

    private ErrorMessageUtil msgUtil;

    @Autowired
    public DeprecatedFieldReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing) {
        //If there are deprecated fields that have been updated between existingListing and updatedListing
        //add a warning to the updatedListing here using the error message "deprecated.field.update"

    }
}
