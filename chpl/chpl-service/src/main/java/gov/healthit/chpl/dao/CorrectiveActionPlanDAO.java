package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CorrectiveActionPlanDTO;
import gov.healthit.chpl.entity.CorrectiveActionPlanEntity;

public interface CorrectiveActionPlanDAO {
    CorrectiveActionPlanDTO create(CorrectiveActionPlanDTO toCreate)
            throws EntityCreationException, EntityRetrievalException;

    CorrectiveActionPlanDTO update(CorrectiveActionPlanDTO toUpdate) throws EntityRetrievalException;

    CorrectiveActionPlanDTO getById(Long id) throws EntityRetrievalException;

    CorrectiveActionPlanEntity getEntityById(Long id) throws EntityRetrievalException;

    List<CorrectiveActionPlanDTO> getAllForCertifiedProduct(Long certifiedProductId);

    void delete(Long id) throws EntityRetrievalException;
}
