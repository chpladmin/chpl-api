package gov.healthit.chpl.validation.listing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.pendingListing.review.Reviewer;
import gov.healthit.chpl.validation.pendingListing.review.RequiredFieldReviewer;
import gov.healthit.chpl.validation.pendingListing.review.FieldLengthReviewer;
import gov.healthit.chpl.validation.pendingListing.review.UnsupportedCharacterReviewer;

/**
 * Validation interface for listings in the pending stage of upload to the CHPL.
 * @author kekey
 *
 */
@Component("pendingListingValidator")
public class PendingListingValidator {
    @Autowired UnsupportedCharacterReviewer unsupportedCharacterReviewer;
    @Autowired FieldLengthReviewer fieldLengthReviewer;
    @Autowired RequiredFieldReviewer requiredFieldReviewer;
    
    private List<Reviewer> reviewers;

    public PendingListingValidator() {
        reviewers = new ArrayList<Reviewer>();
        reviewers.add(unsupportedCharacterReviewer);
        reviewers.add(fieldLengthReviewer);
        reviewers.add(requiredFieldReviewer);
    }

    public List<Reviewer> getReviewers() {
        return reviewers;
    }}
