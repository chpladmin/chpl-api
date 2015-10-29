package gov.healthit.chpl.certifiedProduct.validation;

import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public interface PendingCertifiedProductValidatorFactory {
	public PendingCertifiedProductValidator getValidator(PendingCertifiedProductDTO product);
}
