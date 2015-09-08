package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertifiedProductDTO;

import java.util.List;

public interface CertifiedProductDAO {
	
	public void create(CertifiedProductDTO product) throws EntityCreationException;

	public void update(CertifiedProductDTO product) throws EntityRetrievalException;
	
	public void delete(Long productId);
	
	public List<CertifiedProductDTO> findAll();
	
	public CertifiedProductDTO getById(Long productId) throws EntityRetrievalException;

	public List<CertifiedProductDTO> getByVersionId(Long versionId);
	
}
