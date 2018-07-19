package gov.healthit.chpl.validation.listing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.review.CertificationDateReviewer;
import gov.healthit.chpl.validation.listing.review.CertificationStatusReviewer;
import gov.healthit.chpl.validation.listing.review.DeveloperStatusReviewer;
import gov.healthit.chpl.validation.listing.review.FieldLengthReviewer;
import gov.healthit.chpl.validation.listing.review.Reviewer;
import gov.healthit.chpl.validation.listing.review.TestFunctionalityReviewer;
import gov.healthit.chpl.validation.listing.review.UnattestedCriteriaWithDataReviewer;
import gov.healthit.chpl.validation.listing.review.UnsupportedCharacterReviewer;
import gov.healthit.chpl.validation.listing.review.edition2014.RequiredData2014Reviewer;
import gov.healthit.chpl.validation.listing.review.legacy.LegacyTestToolReviewer;

/**
 * Validation interface for any 2014 listing with CHPL number beginning with CHP-
 * @author kekey
 *
 */
@Component
public class Edition2014LegacyListingValidator extends Validator {
    @Autowired protected DeveloperStatusReviewer devStatusReviewer;
    @Autowired protected UnsupportedCharacterReviewer unsupportedCharacterReviewer;
    @Autowired protected FieldLengthReviewer fieldLengthReviewer;
    @Autowired protected RequiredData2014Reviewer requiredFieldReviewer;
    @Autowired protected CertificationStatusReviewer certStatusReviewer;
    @Autowired protected CertificationDateReviewer certDateReviewer;
    @Autowired protected UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;
    @Autowired protected LegacyTestToolReviewer ttReviewer;
    @Autowired protected TestFunctionalityReviewer tfReviewer;

    private List<Reviewer> reviewers;

    public Edition2014LegacyListingValidator() {
        reviewers = new ArrayList<Reviewer>();
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

    @Override
    public List<Reviewer> getReviewers() {
        return reviewers;
    }
}
