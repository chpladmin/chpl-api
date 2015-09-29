package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertificationStatusDTO;

import java.util.List;

public interface CertificationStatusDAO {
	public List<CertificationStatusDTO> findAll();
	public CertificationStatusDTO getById(Long id) throws EntityRetrievalException;
	public CertificationStatusDTO getByStatusName(String statusName);
}
