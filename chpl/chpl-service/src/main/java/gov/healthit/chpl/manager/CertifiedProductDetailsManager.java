package gov.healthit.chpl.manager;


import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public interface CertifiedProductDetailsManager {	
	public CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId) 
			throws EntityRetrievalException;
}
