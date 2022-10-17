package gov.healthit.chpl.validation.listing;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.InpatientG1G2RequiredData2014Reviewer;

@Component("inpatientModular2014LegacyListingValidator")
public class InpatientModular2014LegacyListingValidator extends Edition2014LegacyListingValidator {
    @Autowired
    @Qualifier("inpatientG1G2RequiredData2014Reviewer")
    private InpatientG1G2RequiredData2014Reviewer g1g2Reviewer;

    private List<Reviewer> reviewers;

    @Override
    public List<Reviewer> getReviewers() {
        if (reviewers == null) {
            reviewers = super.getReviewers();
            reviewers.add(g1g2Reviewer);
        }
        return reviewers;
    }
}
