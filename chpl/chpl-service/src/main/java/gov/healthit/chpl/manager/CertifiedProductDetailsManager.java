package gov.healthit.chpl.manager;


import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductDownloadDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public interface CertifiedProductDetailsManager {	
	public CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId) 
			throws EntityRetrievalException;
	public CertifiedProductDownloadDetails getCertifiedProductDownloadDetails(
			Long certifiedProductId) throws EntityRetrievalException;
}
