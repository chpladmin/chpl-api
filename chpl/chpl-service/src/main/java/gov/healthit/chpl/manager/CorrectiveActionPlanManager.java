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
			
	public CorrectiveActionPlanDetails create(Long acbId, 
			CorrectiveActionPlanDTO toCreate)
		throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	
	public CorrectiveActionPlanDocumentationDTO addDocumentationToPlan(Long acbId, CorrectiveActionPlanDocumentationDTO doc)
			throws EntityRetrievalException, EntityCreationException,JsonProcessingException;
	public CorrectiveActionPlanDetails addCertificationsToPlan(Long acbId, Long correctiveActionPlanId,List<CorrectiveActionPlanCertificationResultDTO> certs)
		throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	public void removeCertificationsFromPlan(Long acbId, List<CorrectiveActionPlanCertificationResultDTO> certs)
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	public CorrectiveActionPlanCertificationResultDTO updateCertification(Long acbId, CorrectiveActionPlanCertificationResultDTO cert)
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	public void removeDocumentation(Long acbId, CorrectiveActionPlanDocumentationDTO toRemove) 
			throws EntityRetrievalException,EntityCreationException, JsonProcessingException;
	public CorrectiveActionPlanDTO getPlanById(Long capId) throws EntityRetrievalException;
	public List<CorrectiveActionPlanDTO> getPlansForCertifiedProduct(Long certifiedProductId) throws EntityRetrievalException;
	public List<CorrectiveActionPlanCertificationResultDTO> getCertificationsForPlan(Long capId) throws EntityRetrievalException; 
	public List<CorrectiveActionPlanDetails> getPlansForCertifiedProductDetails(Long certifiedProductId) 
			throws EntityRetrievalException;
	public List<CorrectiveActionPlanDocumentationDTO> getDocumentationForPlan(Long capId)
			throws EntityRetrievalException;
	public CorrectiveActionPlanDocumentationDTO getDocumentationById(Long docId) throws EntityRetrievalException;
	public CorrectiveActionPlanDetails getPlanDetails(Long capId) throws EntityRetrievalException;
	public CorrectiveActionPlanDTO update(Long acbId, CorrectiveActionPlanDTO toUpdate) 
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	
	public void delete(Long acbId, Long capId)  throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
}
