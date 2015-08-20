package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertifiedProductSearchDetailsDTO;

import java.util.List;

public interface CertifiedProductSearchDetailsDAO {
	
	public CertifiedProductSearchDetailsDTO getById(Long productId) throws EntityRetrievalException;

	public List<CertifiedProductSearchDetailsDTO> getCertifiedProductSearchDetails(
			Integer pageNum, Integer pageSize);
	
}
