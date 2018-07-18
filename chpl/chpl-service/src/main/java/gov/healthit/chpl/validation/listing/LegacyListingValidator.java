package gov.healthit.chpl.validation.listing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.review.CertificationDateReviewer;
import gov.healthit.chpl.validation.listing.review.CertificationStatusReviewer;
import gov.healthit.chpl.validation.listing.review.ChplNumberReviewer;
import gov.healthit.chpl.validation.listing.review.DeveloperStatusReviewer;
import gov.healthit.chpl.validation.listing.review.FieldLengthReviewer;
import gov.healthit.chpl.validation.listing.review.RequiredFieldReviewer;
import gov.healthit.chpl.validation.listing.review.Reviewer;
import gov.healthit.chpl.validation.listing.review.UnsupportedCharacterReviewer;

/**
 * Validation interface for any listing with CHPL number beginning with CHP-
 * @author kekey
 *
 */
@Component("legacyListingValidator")
public class LegacyListingValidator {
    @Autowired ChplNumberReviewer chplNumberReviewer;
    @Autowired DeveloperStatusReviewer devStatusReviewer;
    @Autowired UnsupportedCharacterReviewer unsupportedCharacterReviewer;
    @Autowired FieldLengthReviewer fieldLengthReviewer;
    @Autowired RequiredFieldReviewer requiredFieldReviewer;
    @Autowired CertificationStatusReviewer certStatusReviewer;
    @Autowired CertificationDateReviewer certDateReviewer;
    
    private List<Reviewer> reviewers;

    public LegacyListingValidator() {
        reviewers = new ArrayList<Reviewer>();
        reviewers.add(chplNumberReviewer);
        reviewers.add(devStatusReviewer);
        reviewers.add(unsupportedCharacterReviewer);
        reviewers.add(fieldLengthReviewer);
        reviewers.add(requiredFieldReviewer);
        reviewers.add(certStatusReviewer);
        reviewers.add(certDateReviewer);
    }

    public List<Reviewer> getReviewers() {
        return reviewers;
    }
}
