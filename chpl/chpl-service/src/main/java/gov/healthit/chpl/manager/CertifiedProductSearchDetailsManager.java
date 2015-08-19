package gov.healthit.chpl.manager;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

import java.util.List;



public interface CertifiedProductSearchDetailsManager {
	
	public List<CertifiedProductSearchDetails> getAllCertifiedProducts() throws EntityRetrievalException;
	public CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId);

}
