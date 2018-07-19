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
import gov.healthit.chpl.validation.listing.review.Reviewer;
import gov.healthit.chpl.validation.listing.review.SedG3Reviewer;
import gov.healthit.chpl.validation.listing.review.TestFunctionalityReviewer;
import gov.healthit.chpl.validation.listing.review.TestToolReviewer;
import gov.healthit.chpl.validation.listing.review.UnattestedCriteriaWithDataReviewer;
import gov.healthit.chpl.validation.listing.review.UnsupportedCharacterReviewer;
import gov.healthit.chpl.validation.listing.review.edition2015.InheritedCertificationStatus2015Reviewer;
import gov.healthit.chpl.validation.listing.review.edition2015.RequiredData2015Reviewer;

/**
 * Validation interface for any listing that is already uploaded and confirmed on the CHPL.
 * @author kekey
 *
 */
@Component
public class Edition2015ListingValidator extends Validator {
    @Autowired protected ChplNumberReviewer chplNumberReviewer;
    @Autowired protected DeveloperStatusReviewer devStatusReviewer;
    @Autowired protected UnsupportedCharacterReviewer unsupportedCharacterReviewer;
    @Autowired protected FieldLengthReviewer fieldLengthReviewer;
    @Autowired protected RequiredData2015Reviewer requiredDataReviewer;
    @Autowired protected SedG3Reviewer sedG3Reviewer;
    @Autowired protected CertificationStatusReviewer certStatusReviewer;
    @Autowired protected CertificationDateReviewer certDateReviewer;
    @Autowired protected UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;
    @Autowired protected TestToolReviewer ttReviewer;
    @Autowired protected TestFunctionalityReviewer tfReviewer;
    @Autowired protected InheritedCertificationStatus2015Reviewer icsReviewer;
    
    private List<Reviewer> reviewers;

    public Edition2015ListingValidator() {
        reviewers = new ArrayList<Reviewer>();
        reviewers.add(chplNumberReviewer);
        reviewers.add(devStatusReviewer);
        reviewers.add(unsupportedCharacterReviewer);
        reviewers.add(fieldLengthReviewer);
        reviewers.add(requiredDataReviewer);
        reviewers.add(sedG3Reviewer);
        reviewers.add(certStatusReviewer);
        reviewers.add(certDateReviewer);
        reviewers.add(unattestedCriteriaWithDataReviewer);
        reviewers.add(icsReviewer);
        reviewers.add(ttReviewer);
        reviewers.add(tfReviewer);
    }

    public List<Reviewer> getReviewers() {
        return reviewers;
    }
}
