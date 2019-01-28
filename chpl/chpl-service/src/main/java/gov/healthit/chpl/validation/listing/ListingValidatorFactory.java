package gov.healthit.chpl.validation.listing;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public interface ListingValidatorFactory {
    PendingValidator getValidator(PendingCertifiedProductDTO product);
    Validator getValidator(CertifiedProductSearchDetails product);
}
