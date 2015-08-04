package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;

import java.util.List;

public interface CertificationResultDAO {
	
	public void create(CertificationResultDTO result);

	public void update(CertificationResultDTO result);
	
	public void delete(Long productId);
	
	public List<CertificationResultDTO> findAll();
	
	public CertificationResultDTO getById(Long productId);
	
}
