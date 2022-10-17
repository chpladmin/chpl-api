package gov.healthit.chpl.validation.listing;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.AmbulatoryCompleteRequiredCriteriaReviewer;

/**
 * Validation interface for any listing that is already uploaded and confirmed on the CHPL.
 * @author kekey
 *
 */
@Component("ambulatoryComplete2014LegacyListingValidator")
public class AmbulatoryComplete2014LegacyListingValidator extends AmbulatoryModular2014LegacyListingValidator {

    @Autowired
    @Qualifier("ambulatoryCompleteRequiredCriteriaReviewer")
    private AmbulatoryCompleteRequiredCriteriaReviewer criteriaReviewer;

    private List<Reviewer> reviewers;

    @Override
    public List<Reviewer> getReviewers() {
        if(reviewers == null) {
            reviewers = super.getReviewers();
            reviewers.add(criteriaReviewer);
        }
        return reviewers;
    }
}
