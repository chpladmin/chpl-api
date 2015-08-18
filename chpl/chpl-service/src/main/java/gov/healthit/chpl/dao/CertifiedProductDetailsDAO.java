package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

import java.util.List;

public interface CertifiedProductDetailsDAO {
	
	public List<CertifiedProductDetailsDTO> findAll();
	
	public CertifiedProductDetailsDTO getById(Long productId) throws EntityRetrievalException;
	
}
