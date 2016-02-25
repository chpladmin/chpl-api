package gov.healthit.chpl.certifiedProduct.validation;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

public class CertifiedProductValidatorImpl implements CertifiedProductValidator {
	
	@Override
	public void validate(PendingCertifiedProductDTO product) {
		//check that the codes match the values
		if(product.getUniqueId() == null) {
			product.getErrorMessages().add("No unique ID was provided.");
		} else {
			String[] idParts = product.getUniqueId().split(".");
			if(idParts.length != 9) {
				product.getErrorMessages().add("Unique id was not in the correct format.");
			} else {
				String edition = idParts[0];
				String atl = idParts[1];
				String acb = idParts[2];
				String developer = idParts[3];
				
				if(("2014".equals(product.getCertificationEdition()) && !"14".equals(edition)) ||
					("2015".equals(product.getCertificationEdition()) && !"15".equals(edition))) {
					product.getErrorMessages().add("Certification edition specified does not match the 'edition' part of the unique ID.");
				}
				
				//TODO: add more validation
			}
		}
	}
	@Override
	public void validate(CertifiedProductSearchDetails product) {
		//TODO: not sure if we should do the same validation here or not
	}
	

}
