package gov.healthit.chpl.manager;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.domain.SearchFilters;

import java.util.List;
import java.util.Map;



public interface CertifiedProductSearchDetailsManager {
	
	
	public CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId) 
			throws EntityRetrievalException;
	
	public List<CertifiedProductSearchResult> getCertifiedProducts(Integer pageNum,
			Integer pageSize) throws EntityRetrievalException;
	
	public List<CertifiedProductSearchResult> simpleSearch(String searchTerm,
			Integer pageNum, Integer pageSize);

	public List<CertifiedProductSearchResult> multiFilterSearch(
			SearchFilters searchFilters, Integer pageNum, Integer pageSize);
	
	
}
