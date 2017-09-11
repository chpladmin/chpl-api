package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.entity.CertificationCriterionEntity;

public interface CertificationCriterionDAO {
	
	public CertificationCriterionDTO create(CertificationCriterionDTO result) throws EntityCreationException, EntityRetrievalException;

	public CertificationCriterionDTO update(CertificationCriterionDTO result) throws EntityRetrievalException, EntityCreationException;
	
	public void delete(Long criterionId);
	
	public List<CertificationCriterionDTO> findAll();
	public List<CertificationCriterionDTO> findByCertificationEditionYear(String year);
	public CertificationCriterionDTO getById(Long criterionId) throws EntityRetrievalException;
	public CertificationCriterionDTO getByName(String criterionName);
	public CertificationCriterionDTO getByNameAndYear(String criterionName, String year);
	public CertificationCriterionEntity getEntityByName(String name);
	public CertificationCriterionEntity getEntityById(Long id) throws EntityRetrievalException;
	
}
