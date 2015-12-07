package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;


public interface CertifiedProductSearchManager {

	public SearchResponse search(
			SearchRequest searchRequest);
	
	public List<CertifiedProductDetailsDTO> searchForDtos(SearchRequest searchRequest);
}
