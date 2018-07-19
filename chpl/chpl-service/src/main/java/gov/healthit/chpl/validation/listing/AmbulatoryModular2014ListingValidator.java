package gov.healthit.chpl.validation.listing;

import org.springframework.stereotype.Component;

/**
 * Validation interface for any listing that is already uploaded and confirmed on the CHPL.
 * @author kekey
 *
 */
@Component
public class AmbulatoryModular2014ListingValidator extends Edition2014ListingValidator {

    public AmbulatoryModular2014ListingValidator() {
        super();
        //TODO: add other reviewers
    }
}
