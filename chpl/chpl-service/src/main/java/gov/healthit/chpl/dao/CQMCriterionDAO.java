package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CQMCriterionDTO;

import java.util.List;

public interface CQMCriterionDAO {
	
	public void create(CQMCriterionDTO criterion) throws EntityCreationException, EntityRetrievalException;
	
	public void update(CQMCriterionDTO criterion) throws EntityRetrievalException, EntityCreationException;
	
	public void delete(Long criterionId);
	
	public List<CQMCriterionDTO> findAll();
	
	public CQMCriterionDTO getById(Long criterionId) throws EntityRetrievalException;

}
