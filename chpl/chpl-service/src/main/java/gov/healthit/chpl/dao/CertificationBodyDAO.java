package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.CertificationBodyEntity;

import java.util.List;

public interface CertificationBodyDAO {
	
	public void create(CertificationBodyDTO acb) throws EntityCreationException;

	public void delete(Long acbId);

	public List<CertificationBodyDTO> findAll();

	public CertificationBodyDTO getById(Long id) throws EntityRetrievalException;

	public void update(CertificationBodyDTO contact) throws EntityRetrievalException;
	
}
