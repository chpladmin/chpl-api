package gov.healthit.chpl.manager;


import java.util.ArrayList;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

public interface CertifiedProductDetailsManager {	
	public CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId) 
			throws EntityRetrievalException;

	ArrayList getIcsFamilyTree(Long certifiedProductId) throws EntityRetrievalException;
}
