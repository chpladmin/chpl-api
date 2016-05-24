package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertificationBodyDTO;

import java.util.List;

public interface CertificationBodyDAO {
	
	public CertificationBodyDTO create(CertificationBodyDTO acb) throws EntityRetrievalException, EntityCreationException;

	public void delete(Long acbId);

	public List<CertificationBodyDTO> findAll(boolean showDeleted);

	public CertificationBodyDTO getById(Long id) throws EntityRetrievalException;
	public CertificationBodyDTO getById(Long id, boolean includeDeleted) throws EntityRetrievalException;

	public CertificationBodyDTO getByName(String name);
	public String getMaxCode();
	public CertificationBodyDTO update(CertificationBodyDTO contact) throws EntityRetrievalException;
	
}
