package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationEventDTO;
import gov.healthit.chpl.entity.CertificationEventEntity;


public interface CertificationEventDAO {

	public CertificationEventEntity create(CertificationEventDTO dto) throws EntityCreationException, EntityRetrievalException;
	public CertificationEventEntity update(CertificationEventDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	public CertificationEventDTO getById(Long id) throws EntityRetrievalException;
	public List<CertificationEventDTO> findAll();	
	public List<CertificationEventDTO> findByCertifiedProductId(Long certifiedProductId);	
	
}
