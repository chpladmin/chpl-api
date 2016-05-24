package gov.healthit.chpl.manager;

import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;


public interface CertifiedProductSearchManager {

	public SearchResponse search(
			SearchRequest searchRequest);
	}
