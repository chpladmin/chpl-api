package gov.healthit.chpl.certifiedProduct.validation;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public interface CertifiedProductValidator {
	static final long AMBULATORY_CQM_TYPE_ID = 1;
	static final long INPATIENT_CQM_TYPE_ID = 2;
	static final String URL_PATTERN = "^https?://([\\da-z\\.-]+)\\.([a-z\\.]{2,6})(:[0-9]+)?([\\/\\w \\.\\-\\,=&%#]*)*(\\?([\\/\\w \\.\\-\\,=&%#]*)*)?";

		
	
	public void validate(PendingCertifiedProductDTO product);
	public void validate(CertifiedProductSearchDetails product);
}
