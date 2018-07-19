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
import gov.healthit.chpl.validation.listing.review.RequiredField2014Reviewer;
import gov.healthit.chpl.validation.listing.review.Reviewer;
import gov.healthit.chpl.validation.listing.review.SedG3Reviewer;
import gov.healthit.chpl.validation.listing.review.UnattestedCriteriaWithDataReviewer;
import gov.healthit.chpl.validation.listing.review.UnsupportedCharacterReviewer;

/**
 * Validation interface for any listing that is already uploaded and confirmed on the CHPL.
 * @author kekey
 *
 */
@Component
public class Edition2015ListingValidator extends Validator {
    @Autowired ChplNumberReviewer chplNumberReviewer;
    @Autowired DeveloperStatusReviewer devStatusReviewer;
    @Autowired UnsupportedCharacterReviewer unsupportedCharacterReviewer;
    @Autowired FieldLengthReviewer fieldLengthReviewer;
    @Autowired RequiredField2015Reviewer requiredFieldReviewer;
    @Autowired SedG3Reviewer sedG3Reviewer;
    @Autowired CertificationStatusReviewer certStatusReviewer;
    @Autowired CertificationDateReviewer certDateReviewer;
    @Autowired UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;
    
    private List<Reviewer> reviewers;

    public Edition2015ListingValidator() {
        reviewers = new ArrayList<Reviewer>();
        reviewers.add(chplNumberReviewer);
        reviewers.add(devStatusReviewer);
        reviewers.add(unsupportedCharacterReviewer);
        reviewers.add(fieldLengthReviewer);
        reviewers.add(requiredFieldReviewer);
        reviewers.add(sedG3Reviewer);
        reviewers.add(certStatusReviewer);
        reviewers.add(certDateReviewer);
        reviewers.add(unattestedCriteriaWithDataReviewer);
    }

    public List<Reviewer> getReviewers() {
        return reviewers;
    }
}
