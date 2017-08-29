package gov.healthit.chpl.manager;


import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public interface CertifiedProductDetailsManager {	
	public CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId) 
			throws EntityRetrievalException;

	public List getIcsFamilyTree(Long certifiedProductId) throws EntityRetrievalException;
}
