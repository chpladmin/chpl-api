package gov.healthit.chpl.validation.listing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.CertificationDateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.CertificationStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.DeveloperStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.FieldLengthReviewer;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.TestFunctionalityReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UnattestedCriteriaWithDataReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UnsupportedCharacterReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.RequiredData2014Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.legacy.LegacyTestToolReviewer;

/**
 * Validation interface for any 2014 listing with CHPL number beginning with CHP-
 * @author kekey
 *
 */
@Component
public class Edition2014LegacyListingValidator extends Validator {
    @Autowired 
    @Qualifier("developerStatusReviewer")
    private DeveloperStatusReviewer devStatusReviewer;

    @Autowired 
    @Qualifier("unsupportedCharacterReviewer")
    private UnsupportedCharacterReviewer unsupportedCharacterReviewer;

    @Autowired 
    @Qualifier("fieldLengthReviewer")
    private FieldLengthReviewer fieldLengthReviewer;

    @Autowired 
    @Qualifier("requiredData2014Reviewer")
    private RequiredData2014Reviewer requiredFieldReviewer;

    @Autowired 
    @Qualifier("certificationStatusReviewer")
    private CertificationStatusReviewer certStatusReviewer;

    @Autowired 
    @Qualifier("certificationDateReviewer")
    private CertificationDateReviewer certDateReviewer;

    @Autowired 
    @Qualifier("unattestedCriteriaWithDataReviewer")
    private UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;

    @Autowired 
    @Qualifier("legacyTestToolReviewer")
    private LegacyTestToolReviewer ttReviewer;
    
    @Autowired 
    @Qualifier("testFunctionalityReviewer")
    private TestFunctionalityReviewer tfReviewer;

    private List<Reviewer> reviewers;

    @Override
    public List<Reviewer> getReviewers() {
        if(reviewers == null) {
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
        return reviewers;
    }
}
