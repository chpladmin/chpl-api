package gov.healthit.chpl.dao;

import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

import java.util.List;

public interface CertifiedProductSearchResultDAO {
	
	public CertifiedProductDetailsDTO getById(Long productId) throws EntityRetrievalException;

	public List<CertifiedProductDetailsDTO> getCertifiedProductSearchDetails(
			Integer pageNum, Integer pageSize);
	
	public List<CertifiedProductDetailsDTO> simpleSearch(String searchTerm,
			Integer pageNum, Integer pageSize, String orderBy);
	
	public List<CertifiedProductDetailsDTO> multiFilterSearch(
			SearchRequest searchRequest, Integer pageNum, Integer pageSize);
	
}
