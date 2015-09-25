package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationEventDTO;

public interface CertificationEventDAO {

	public CertificationEventDTO create(CertificationEventDTO dto) throws EntityCreationException, EntityRetrievalException;
	public CertificationEventDTO update(CertificationEventDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	public CertificationEventDTO getById(Long id) throws EntityRetrievalException;
	public List<CertificationEventDTO> findAll();	
	public List<CertificationEventDTO> findByCertifiedProductId(Long certifiedProductId);	
	public CertificationEventDTO findByEventTypeName(String eventTypeName);
	
}