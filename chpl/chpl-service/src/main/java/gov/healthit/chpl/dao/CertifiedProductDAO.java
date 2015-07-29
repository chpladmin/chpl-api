package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertifiedProductDTO;

import java.util.List;

public interface CertifiedProductDAO {
	
	public void create(CertifiedProductDTO product);

	public void update(CertifiedProductDTO product);
	
	public void delete(Long productId);
	
	public List<CertifiedProductDTO> findAll();
	
	public CertifiedProductDTO getById(Long productId);
	
}
