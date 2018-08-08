package gov.healthit.chpl.validation.listing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.edition2014.AmbulatoryG1G2RequiredData2014Reviewer;

/**
 * Validation interface for a 2014 Edition Ambulatory Modular EHR listing
 * already uploaded to the CHPL.
 * @author kekey
 *
 */
@Component
public class AmbulatoryModular2014LegacyListingValidator extends Edition2014LegacyListingValidator {

    @Autowired AmbulatoryG1G2RequiredData2014Reviewer g1g2Reviewer;
    
    public AmbulatoryModular2014LegacyListingValidator() {
        super();
        getReviewers().add(g1g2Reviewer);
    }
}
