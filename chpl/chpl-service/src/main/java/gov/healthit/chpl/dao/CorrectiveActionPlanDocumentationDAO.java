package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CorrectiveActionPlanDocumentationDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CorrectiveActionPlanDocumentationDAO {
    CorrectiveActionPlanDocumentationDTO create(CorrectiveActionPlanDocumentationDTO toCreate)
            throws EntityCreationException, EntityRetrievalException;

    CorrectiveActionPlanDocumentationDTO getById(Long id) throws EntityRetrievalException;

    List<CorrectiveActionPlanDocumentationDTO> getAllForCorrectiveActionPlan(Long capId);

    void delete(Long id) throws EntityRetrievalException;
}
