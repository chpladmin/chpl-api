package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CorrectiveActionPlanDocumentationDTO;

public interface CorrectiveActionPlanDocumentationDAO {
	public CorrectiveActionPlanDocumentationDTO create(CorrectiveActionPlanDocumentationDTO toCreate) throws EntityCreationException,
		EntityRetrievalException;
	public CorrectiveActionPlanDocumentationDTO getById(Long id) throws EntityRetrievalException;
	public List<CorrectiveActionPlanDocumentationDTO> getAllForCorrectiveActionPlan(Long capId);
	public void delete(Long id) throws EntityRetrievalException;
}
