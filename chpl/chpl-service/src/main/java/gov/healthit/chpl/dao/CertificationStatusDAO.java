package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationStatusDTO;

public interface CertificationStatusDAO {
	public List<CertificationStatusDTO> findAll();
	public CertificationStatusDTO getById(Long id) throws EntityRetrievalException;
	public CertificationStatusDTO getByStatusName(String statusName);
}
