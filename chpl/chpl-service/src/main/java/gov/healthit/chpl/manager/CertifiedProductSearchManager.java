package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;


public interface CertifiedProductSearchManager {

	public List<CertifiedProductFlatSearchResult> search();
	public void addSearchResultToListingCache(Long id);
	public SearchResponse search(SearchRequest searchRequest);
}
