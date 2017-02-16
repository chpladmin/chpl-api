package gov.healthit.chpl.manager;

import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.domain.search.BasicSearchResponse;


public interface CertifiedProductSearchManager {

	public BasicSearchResponse search();
	public SearchResponse search(SearchRequest searchRequest);
}
