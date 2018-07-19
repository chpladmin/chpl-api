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
import gov.healthit.chpl.validation.listing.review.LegacyTestToolReviewer;
import gov.healthit.chpl.validation.listing.review.RequiredField2014Reviewer;
import gov.healthit.chpl.validation.listing.review.Reviewer;
import gov.healthit.chpl.validation.listing.review.TestFunctionalityReviewer;
import gov.healthit.chpl.validation.listing.review.UnattestedCriteriaWithDataReviewer;
import gov.healthit.chpl.validation.listing.review.UnsupportedCharacterReviewer;

/**
 * Validation interface for any 2014 listing with CHPL number beginning with CHP-
 * @author kekey
 *
 */
@Component
public class Edition2014LegacyListingValidator {
    @Autowired ChplNumberReviewer chplNumberReviewer;
    @Autowired DeveloperStatusReviewer devStatusReviewer;
    @Autowired UnsupportedCharacterReviewer unsupportedCharacterReviewer;
    @Autowired FieldLengthReviewer fieldLengthReviewer;
    @Autowired RequiredField2014Reviewer requiredFieldReviewer;
    @Autowired CertificationStatusReviewer certStatusReviewer;
    @Autowired CertificationDateReviewer certDateReviewer;
    @Autowired UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;
    @Autowired LegacyTestToolReviewer ttReviewer;
    @Autowired TestFunctionalityReviewer tfReviewer;

    private List<Reviewer> reviewers;

    public Edition2014LegacyListingValidator() {
        reviewers = new ArrayList<Reviewer>();
        reviewers.add(chplNumberReviewer);
        reviewers.add(devStatusReviewer);
        reviewers.add(unsupportedCharacterReviewer);
        reviewers.add(fieldLengthReviewer);
        reviewers.add(requiredFieldReviewer);
        reviewers.add(certStatusReviewer);
        reviewers.add(certDateReviewer);
        reviewers.add(unattestedCriteriaWithDataReviewer);
        reviewers.add(ttReviewer);
        reviewers.add(tfReviewer);
    }

    public List<Reviewer> getReviewers() {
        return reviewers;
    }
}
