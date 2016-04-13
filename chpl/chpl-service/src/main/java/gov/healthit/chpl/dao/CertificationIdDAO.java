package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.entity.CertificationIdEntity;

import java.util.List;

public interface CertificationIdDAO {
	
	public CertificationIdDTO create(CertificationIdDTO dto) throws EntityCreationException;
	
	public CertificationIdEntity update(CertificationIdDTO dto) throws EntityRetrievalException;
	
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<CertificationIdDTO> findAll();
	
	public CertificationIdDTO getById(Long id) throws EntityRetrievalException;
	public CertificationIdDTO getByCertificationId(String certificationId) throws EntityRetrievalException;
	public CertificationIdDTO getByProductIds(List<Long> productIds) throws EntityRetrievalException;
}
