package gov.healthit.chpl.certifiedProduct.validation;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public interface CertifiedProductValidatorFactory {
	public CertifiedProductValidator getValidator(PendingCertifiedProductDTO product);
	public CertifiedProductValidator getValidator(CertifiedProductSearchDetails product);
}
