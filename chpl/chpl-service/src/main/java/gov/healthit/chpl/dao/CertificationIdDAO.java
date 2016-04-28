package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.entity.CertificationIdEntity;

import java.util.List;

public interface CertificationIdDAO {
	
	public CertificationIdDTO create(List<Long> productIds, String year) throws EntityCreationException;
	public CertificationIdDTO create(CertificationIdDTO dto) throws EntityCreationException;
	
	public List<CertificationIdDTO> findAll();
	
	public CertificationIdDTO getById(Long id) throws EntityRetrievalException;
	public CertificationIdDTO getByCertificationId(String certificationId) throws EntityRetrievalException;
	public CertificationIdDTO getByProductIds(List<Long> productIds, String year) throws EntityRetrievalException;
}
