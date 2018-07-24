package gov.healthit.chpl.validation.listing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.pendingListing.review.Reviewer;
import gov.healthit.chpl.validation.pendingListing.review.UnattestedCriteriaWithDataReviewer;
import gov.healthit.chpl.validation.pendingListing.review.RequiredDataReviewer;
import gov.healthit.chpl.validation.pendingListing.review.CertificationDateReviewer;
import gov.healthit.chpl.validation.pendingListing.review.ChplNumberReviewer;
import gov.healthit.chpl.validation.pendingListing.review.FieldLengthReviewer;
import gov.healthit.chpl.validation.pendingListing.review.FuzzyMatchReviewer;
import gov.healthit.chpl.validation.pendingListing.review.InheritedCertificationStatusReviewer;
import gov.healthit.chpl.validation.pendingListing.review.UnsupportedCharacterReviewer;
import gov.healthit.chpl.validation.pendingListing.review.ValidDataReviewer;

/**
 * Validation interface for 2015 listings in the pending stage of upload to the CHPL.
 * @author kekey
 *
 */
@Component
public class Edition2015PendingListingValidator {
    @Autowired ChplNumberReviewer chplNumberReviewer;
    @Autowired CertificationDateReviewer certDateReviewer;
    @Autowired FuzzyMatchReviewer fuzzyMatchReviewer;
    @Autowired UnsupportedCharacterReviewer unsupportedCharacterReviewer;
    @Autowired UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;
    @Autowired ValidDataReviewer validDataReviewer;
    @Autowired FieldLengthReviewer fieldLengthReviewer;
    @Autowired RequiredDataReviewer requiredDataReviewer;
    @Autowired InheritedCertificationStatusReviewer icsReviewer;
    
    private List<Reviewer> reviewers;

    public Edition2015PendingListingValidator() {
        reviewers = new ArrayList<Reviewer>();
        reviewers.add(chplNumberReviewer);
        reviewers.add(certDateReviewer);
        reviewers.add(fuzzyMatchReviewer);
        reviewers.add(unsupportedCharacterReviewer);
        reviewers.add(unattestedCriteriaWithDataReviewer);
        reviewers.add(validDataReviewer);
        reviewers.add(fieldLengthReviewer);
        reviewers.add(requiredDataReviewer);
        reviewers.add(icsReviewer);
    }

    public List<Reviewer> getReviewers() {
        return reviewers;
    }}
