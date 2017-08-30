package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

public interface CertifiedProductSearchResultDAO {
	
	public CertifiedProductDetailsDTO getById(Long productId) throws EntityRetrievalException;
	//public CertifiedProductDetailsDTO getAllDetailsById(Long productId) throws EntityRetrievalException;
	public List<CertifiedProductDetailsDTO> search(
			SearchRequest searchRequest);
	
	public Long countMultiFilterSearchResults(SearchRequest searchRequest);
	
}
