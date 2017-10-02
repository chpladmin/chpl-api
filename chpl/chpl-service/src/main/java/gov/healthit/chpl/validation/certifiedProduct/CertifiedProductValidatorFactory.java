package gov.healthit.chpl.validation.certifiedProduct;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public interface CertifiedProductValidatorFactory {
    public CertifiedProductValidator getValidator(PendingCertifiedProductDTO product);

    public CertifiedProductValidator getValidator(CertifiedProductSearchDetails product);
}
