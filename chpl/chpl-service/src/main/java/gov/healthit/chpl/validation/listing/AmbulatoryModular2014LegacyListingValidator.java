package gov.healthit.chpl.validation.listing;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.AmbulatoryG1G2RequiredData2014Reviewer;

/**
 * Validation interface for a 2014 Edition Ambulatory Modular EHR listing
 * already uploaded to the CHPL.
 * @author kekey
 *
 */
@Component("ambulatoryModular2014LegacyListingValidator")
public class AmbulatoryModular2014LegacyListingValidator extends Edition2014LegacyListingValidator {

    @Autowired
    @Qualifier("ambulatoryG1G2RequiredData2014Reviewer")
    AmbulatoryG1G2RequiredData2014Reviewer g1g2Reviewer;

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
