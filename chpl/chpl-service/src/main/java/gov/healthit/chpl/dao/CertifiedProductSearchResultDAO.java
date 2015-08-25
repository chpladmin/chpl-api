package gov.healthit.chpl.dao;

import gov.healthit.chpl.domain.SearchFilters;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

import java.util.List;

public interface CertifiedProductSearchResultDAO {
	
	public CertifiedProductDetailsDTO getById(Long productId) throws EntityRetrievalException;

	public List<CertifiedProductDetailsDTO> getCertifiedProductSearchDetails(
			Integer pageNum, Integer pageSize);
	
	public List<CertifiedProductDetailsDTO> simpleSearch(String searchTerm,
			Integer pageNum, Integer pageSize);
	
	public List<CertifiedProductDetailsDTO> multiFilterSearch(
			SearchFilters searchFilters, Integer pageNum, Integer pageSize);
	
}
