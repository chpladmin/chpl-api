package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertifiedProductSearchDetailsDTO;

import java.util.List;

public interface CertifiedProductSearchDetailsDAO {
	
	public List<CertifiedProductSearchDetailsDTO> findAll();
	
	public CertifiedProductSearchDetailsDTO getById(Long productId) throws EntityRetrievalException;
	
}
