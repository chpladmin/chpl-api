package gov.healthit.chpl.manager;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

import java.util.List;



public interface CertifiedProductSearchDetailsManager {
	
	public CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId);
	public List<CertifiedProductSearchDetails> getCertifiedProducts(Integer pageNum,
			Integer pageSize) throws EntityRetrievalException;

}
