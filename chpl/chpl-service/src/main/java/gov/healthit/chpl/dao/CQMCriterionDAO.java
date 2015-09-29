package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.entity.CQMCriterionEntity;

import java.util.List;

public interface CQMCriterionDAO {
	
	public CQMCriterionDTO create(CQMCriterionDTO criterion) throws EntityCreationException, EntityRetrievalException;
	
	public void update(CQMCriterionDTO criterion) throws EntityRetrievalException, EntityCreationException;
	
	public void delete(Long criterionId);
	
	public List<CQMCriterionDTO> findAll();
	
	public CQMCriterionDTO getById(Long criterionId) throws EntityRetrievalException;
	public CQMCriterionDTO getByNumber(String number);
	public CQMCriterionDTO getByNumberAndVersion(String number, String version);
	public CQMCriterionEntity getEntityByNumberAndVersion(String number, String version);
	public CQMCriterionEntity getEntityByNumber(String number);
}
