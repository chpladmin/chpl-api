package gov.healthit.chpl.validation.listing;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.InpatientG1G2RequiredData2014Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.InpatientRequiredTestToolReviewer;

@Component("inpatientModular2014ListingValidator")
public class InpatientModular2014ListingValidator extends Edition2014ListingValidator {
    @Autowired
    @Qualifier("inpatientG1G2RequiredData2014Reviewer")
    private InpatientG1G2RequiredData2014Reviewer g1g2Reviewer;

    @Autowired
    @Qualifier("inpatientRequiredTestToolReviewer")
    private InpatientRequiredTestToolReviewer ttReviewer;

    private List<Reviewer> reviewers;

    @Override
    public List<Reviewer> getReviewers() {
        if (reviewers == null) {
            reviewers = super.getReviewers();
            reviewers.add(g1g2Reviewer);
            reviewers.add(ttReviewer);
        }
        return reviewers;
    }
}
