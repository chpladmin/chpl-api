package gov.healthit.chpl.manager;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;

import java.util.List;
import java.util.Map;



public interface CertifiedProductSearchManager {
	
	
	public CertifiedProductSearchDetails getCertifiedProductDetails(Long certifiedProductId) 
			throws EntityRetrievalException;
	
	public List<CertifiedProductSearchResult> getCertifiedProducts(Integer pageNum,
			Integer pageSize) throws EntityRetrievalException;
	
	public SearchResponse simpleSearch(String searchTerm,
			Integer pageNum, Integer pageSize);
	
	public SearchResponse simpleSearch(String searchTerm,
			Integer pageNum, Integer pageSize, String orderBy);

	public SearchResponse multiFilterSearch(
			SearchRequest searchFilters, Integer pageNum, Integer pageSize);	
	
}
