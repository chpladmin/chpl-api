package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;

import java.util.List;

public interface CertificationResultDAO {
	
	public void create(CertificationResultDTO result) throws EntityCreationException;
	public void update(CertificationResultDTO result) throws EntityRetrievalException;
	public void delete(Long resultId);
	public List<CertificationResultDTO> findAll();
	public List<CertificationResultDTO> findByCertifiedProductId(Long certifiedProductId);
	public CertificationResultDTO getById(Long resultId) throws EntityRetrievalException;
	
}
