package gov.healthit.chpl.validation.certifiedProduct;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

@Component("allowedValidator")
public class CertifiedProductAllowedValidator implements CertifiedProductValidator {

	@Override
	public void validate(PendingCertifiedProductDTO product) {
		//does nothing, everything is valid
	}
	
	@Override
	public void validate(CertifiedProductSearchDetails product) {
		//does nothing, everything is valid
	}
}
