package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.entity.CQMCriterionEntity;

public interface CQMCriterionDAO {

	public CQMCriterionDTO create(CQMCriterionDTO criterion) throws EntityCreationException, EntityRetrievalException;

	public void update(CQMCriterionDTO criterion) throws EntityRetrievalException, EntityCreationException;

	public void delete(Long criterionId);

	public List<CQMCriterionDTO> findAll();

	public CQMCriterionDTO getById(Long criterionId) throws EntityRetrievalException;
	public CQMCriterionDTO getCMSByNumber(String number);
	public CQMCriterionDTO getNQFByNumber(String number);
	public CQMCriterionDTO getCMSByNumberAndVersion(String number, String version);

	public CQMCriterionEntity getCMSEntityByNumberAndVersion(String number, String version);
	public CQMCriterionEntity getCMSEntityByNumber(String number);
	public CQMCriterionEntity getNQFEntityByNumber(String number);
}
