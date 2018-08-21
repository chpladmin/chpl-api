package gov.healthit.chpl.validation.listing;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public interface ListingValidatorFactory {
    public PendingValidator getValidator(PendingCertifiedProductDTO product);
    public Validator getValidator(CertifiedProductSearchDetails product);
}
