package gov.healthit.chpl.manager;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CorrectiveActionPlanDetails;
import gov.healthit.chpl.dto.CorrectiveActionPlanCertificationResultDTO;
import gov.healthit.chpl.dto.CorrectiveActionPlanDTO;
import gov.healthit.chpl.dto.CorrectiveActionPlanDocumentationDTO;

public interface CorrectiveActionPlanManager {

    CorrectiveActionPlanDetails create(Long acbId, CorrectiveActionPlanDTO toCreate)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    CorrectiveActionPlanDocumentationDTO addDocumentationToPlan(Long acbId,
            CorrectiveActionPlanDocumentationDTO doc)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    CorrectiveActionPlanDetails addCertificationsToPlan(Long acbId, Long correctiveActionPlanId,
            List<CorrectiveActionPlanCertificationResultDTO> certs)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    void removeCertificationsFromPlan(Long acbId, List<CorrectiveActionPlanCertificationResultDTO> certs)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    CorrectiveActionPlanCertificationResultDTO updateCertification(Long acbId,
            CorrectiveActionPlanCertificationResultDTO cert)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    void removeDocumentation(Long acbId, CorrectiveActionPlanDocumentationDTO toRemove)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    CorrectiveActionPlanDTO getPlanById(Long capId) throws EntityRetrievalException;

    List<CorrectiveActionPlanDTO> getPlansForCertifiedProduct(Long certifiedProductId)
            throws EntityRetrievalException;

    List<CorrectiveActionPlanCertificationResultDTO> getCertificationsForPlan(Long capId)
            throws EntityRetrievalException;

    List<CorrectiveActionPlanDetails> getPlansForCertifiedProductDetails(Long certifiedProductId)
            throws EntityRetrievalException;

    List<CorrectiveActionPlanDocumentationDTO> getDocumentationForPlan(Long capId)
            throws EntityRetrievalException;

    CorrectiveActionPlanDocumentationDTO getDocumentationById(Long docId) throws EntityRetrievalException;

    CorrectiveActionPlanDetails getPlanDetails(Long capId) throws EntityRetrievalException;

    CorrectiveActionPlanDTO update(Long acbId, CorrectiveActionPlanDTO toUpdate)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;

    void delete(Long acbId, Long capId)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
}
