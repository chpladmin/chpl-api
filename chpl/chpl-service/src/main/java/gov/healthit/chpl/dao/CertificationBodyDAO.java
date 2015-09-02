package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertificationBodyDTO;

import java.util.List;

public interface CertificationBodyDAO {
	
	public CertificationBodyDTO create(CertificationBodyDTO acb) throws EntityRetrievalException, EntityCreationException;

	public void delete(Long acbId);

	public List<CertificationBodyDTO> findAll();

	public CertificationBodyDTO getById(Long id) throws EntityRetrievalException;

	public CertificationBodyDTO update(CertificationBodyDTO contact) throws EntityRetrievalException;
	
}
