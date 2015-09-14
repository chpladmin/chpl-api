package gov.healthit.chpl.manager;

import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;


public interface CertifiedProductSearchManager {
	
	public SearchResponse simpleSearch(String searchTerm,
			Integer pageNum, Integer pageSize);
	
	public SearchResponse simpleSearch(String searchTerm,
			Integer pageNum, Integer pageSize, String orderBy, Boolean orderDescending);

	public SearchResponse multiFilterSearch(
			SearchRequest searchRequest, Integer pageNum, Integer pageSize);
	
}
