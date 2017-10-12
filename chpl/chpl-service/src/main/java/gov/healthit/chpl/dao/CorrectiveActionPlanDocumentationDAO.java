package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CorrectiveActionPlanDocumentationDTO;

public interface CorrectiveActionPlanDocumentationDAO {
    CorrectiveActionPlanDocumentationDTO create(CorrectiveActionPlanDocumentationDTO toCreate)
            throws EntityCreationException, EntityRetrievalException;

    CorrectiveActionPlanDocumentationDTO getById(Long id) throws EntityRetrievalException;

    List<CorrectiveActionPlanDocumentationDTO> getAllForCorrectiveActionPlan(Long capId);

    void delete(Long id) throws EntityRetrievalException;
}
