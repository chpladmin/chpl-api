package gov.healthit.chpl.certifiedProduct.validation;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public interface CertifiedProductValidator {
	static final long AMBULATORY_CQM_TYPE_ID = 1;
	static final long INPATIENT_CQM_TYPE_ID = 2;
	
	public void validate(PendingCertifiedProductDTO product);
	public void validate(CertifiedProductSearchDetails product);
}
