package gov.healthit.chpl.validation.certifiedProduct;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
	
	@Override
	public boolean validateUniqueId(String uniqueId) {
		return true;
	}
	
	@Override
	public boolean validateProductCodeCharacters(String chplProductNumber) {
		String[] uniqueIdParts = chplProductNumber.split("\\.");
		if(uniqueIdParts != null && uniqueIdParts.length == CHPL_PRODUCT_ID_PARTS) {
			
			//validate that these pieces match up with data
			String productCode = uniqueIdParts[PRODUCT_CODE_INDEX];
			if(StringUtils.isEmpty(productCode) || productCode.length() > 16 || !productCode.matches("^\\w+$")) {
				return false;
			}
		}
		return true;
	}
}
