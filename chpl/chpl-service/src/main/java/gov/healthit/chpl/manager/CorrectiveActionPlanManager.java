package gov.healthit.chpl.manager;


import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CorrectiveActionPlanDetails;
import gov.healthit.chpl.dto.CorrectiveActionPlanCertificationResultDTO;
import gov.healthit.chpl.dto.CorrectiveActionPlanDTO;

public interface CorrectiveActionPlanManager {
			
	public CorrectiveActionPlanDetails create(Long acbId, 
			CorrectiveActionPlanDTO toCreate)
		throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	
	public CorrectiveActionPlanDetails addCertificationsToPlan(Long acbId, Long correctiveActionPlanId,List<CorrectiveActionPlanCertificationResultDTO> certs)
		throws EntityRetrievalException, EntityCreationException;
	public void removeCertificationsFromPlan(Long acbId, List<CorrectiveActionPlanCertificationResultDTO> certs)
			throws EntityRetrievalException;
			
	public CorrectiveActionPlanDTO getPlanById(Long capId) throws EntityRetrievalException;
	public List<CorrectiveActionPlanDTO> getPlansForCertifiedProduct(Long certifiedProductId) throws EntityRetrievalException;
	public List<CorrectiveActionPlanCertificationResultDTO> getCertificationsForPlan(Long capId) throws EntityRetrievalException; 
	public List<CorrectiveActionPlanDetails> getPlansForCertifiedProductDetails(Long certifiedProductId) 
			throws EntityRetrievalException;
	public CorrectiveActionPlanDetails getPlanDetails(Long capId) throws EntityRetrievalException;
	public CorrectiveActionPlanDTO update(Long acbId, CorrectiveActionPlanDTO toUpdate) throws EntityRetrievalException;
	
	public void delete(Long acbId, Long capId)  throws EntityRetrievalException;
}
