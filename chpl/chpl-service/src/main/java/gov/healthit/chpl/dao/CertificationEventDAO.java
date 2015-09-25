package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationEventDTO;

public interface CertificationEventDAO {
	
	public CertificationEventDTO create(CertificationEventDTO dto) throws EntityCreationException;
	public List<CertificationEventDTO> findAll();
	public CertificationEventDTO findById(Long eventId);
	public CertificationEventDTO findByEventTypeName(String eventTypeName);
}
