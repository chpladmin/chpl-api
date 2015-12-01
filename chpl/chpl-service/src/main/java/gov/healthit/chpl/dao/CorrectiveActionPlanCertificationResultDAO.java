package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CorrectiveActionPlanCertificationResultDTO;

public interface CorrectiveActionPlanCertificationResultDAO {
	public CorrectiveActionPlanCertificationResultDTO create(CorrectiveActionPlanCertificationResultDTO toCreate) throws EntityCreationException,
		EntityRetrievalException;
	public CorrectiveActionPlanCertificationResultDTO update(CorrectiveActionPlanCertificationResultDTO toUpdate) throws EntityRetrievalException;
	public CorrectiveActionPlanCertificationResultDTO getById(Long id) throws EntityRetrievalException;
	public List<CorrectiveActionPlanCertificationResultDTO> getAllForCorrectiveActionPlan(Long capId);
	public void delete(Long id) throws EntityRetrievalException;
}
