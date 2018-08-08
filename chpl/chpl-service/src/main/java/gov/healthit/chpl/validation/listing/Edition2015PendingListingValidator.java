package gov.healthit.chpl.validation.listing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.pendingListing.reviewer.CertificationDateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.ChplNumberReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.FieldLengthReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.FuzzyMatchReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.InheritedCertificationStatusReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.TestFunctionalityReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.TestToolReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.UnattestedCriteriaWithDataReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.UnsupportedCharacterReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.ValidDataReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.RequiredData2015Reviewer;

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
    @Autowired RequiredData2015Reviewer requiredDataReviewer;
    @Autowired TestToolReviewer ttReviewer;
    @Autowired TestFunctionalityReviewer tfReviewer;
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
        reviewers.add(ttReviewer);
        reviewers.add(tfReviewer);
        reviewers.add(icsReviewer);
    }

    public List<Reviewer> getReviewers() {
        return reviewers;
    }}
