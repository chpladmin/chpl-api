package gov.healthit.chpl.validation.listing;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.review.Reviewer;
import gov.healthit.chpl.validation.listing.review.edition2014.InpatientG1G2RequiredData2014Reviewer;

@Component
public class InpatientModular2014LegacyListingValidator extends Edition2014ListingValidator {
    @Autowired protected InpatientG1G2RequiredData2014Reviewer g1g2Reviewer;
    
    public InpatientModular2014LegacyListingValidator() {
        super();
        reviewers.add(g1g2Reviewer);
    }
    
    @Override
    public List<Reviewer> getReviewers() {
        return reviewers;
    }
}
