package gov.healthit.chpl.validation.listing;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public interface ListingValidatorFactory {
    Validator getValidator(CertifiedProductSearchDetails product);
}
