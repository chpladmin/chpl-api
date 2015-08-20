package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertificationCriterionDTO;

import java.util.List;

public interface CertificationCriterionDAO {
	
	public void create(CertificationCriterionDTO result) throws EntityCreationException, EntityRetrievalException;

	public void update(CertificationCriterionDTO result) throws EntityRetrievalException, EntityCreationException;
	
	public void delete(Long criterionId);
	
	public List<CertificationCriterionDTO> findAll();
	
	public CertificationCriterionDTO getById(Long criterionId) throws EntityRetrievalException;
	
}
