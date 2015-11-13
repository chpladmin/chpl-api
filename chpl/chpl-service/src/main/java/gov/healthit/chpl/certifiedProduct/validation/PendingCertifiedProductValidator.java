package gov.healthit.chpl.certifiedProduct.validation;

import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public interface PendingCertifiedProductValidator {
	static final long AMBULATORY_CQM_TYPE_ID = 1;
	static final long INPATIENT_CQM_TYPE_ID = 2;
	
	public void validate(PendingCertifiedProductDTO product);
}
