package gov.healthit.chpl.web.controller;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CorrectiveActionPlanCertificationResult;
import gov.healthit.chpl.domain.CorrectiveActionPlanDetails;
import gov.healthit.chpl.domain.CorrectiveActionPlanResults;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CorrectiveActionPlanCertificationResultDTO;
import gov.healthit.chpl.dto.CorrectiveActionPlanDTO;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.CorrectiveActionPlanManager;

@RestController
@RequestMapping("/corrective_action_plan")
public class CorrectiveActionPlanController {
	
	private static final Logger logger = LogManager.getLogger(CorrectiveActionPlanController.class);

	@Autowired CorrectiveActionPlanManager capManager;
	@Autowired CertifiedProductManager productManager;
	
	@RequestMapping(value="/", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CorrectiveActionPlanResults getCorrectiveActionPlansForCertifiedProduct(
			@RequestParam(value = "certifiedProductId", required=true) Long cpId) throws EntityRetrievalException {
		List<CorrectiveActionPlanDetails> plans = capManager.getPlansForCertifiedProductDetails(cpId);
		
		CorrectiveActionPlanResults results = new CorrectiveActionPlanResults();
		results.getPlans().addAll(plans);
		return results;
	}
	
	@RequestMapping(value="/{capId}", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody CorrectiveActionPlanDetails getCorrectiveActionPlanById(@PathVariable("capId") Long capId) throws EntityRetrievalException {
		return capManager.getPlanDetails(capId);
	}
	
	@RequestMapping(value="/update", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody CorrectiveActionPlanDetails update(@RequestBody(required=true) CorrectiveActionPlanDetails updateRequest) 
		throws EntityCreationException, EntityRetrievalException, JsonProcessingException,
		InvalidArgumentsException {
		
		CorrectiveActionPlanDTO toUpdate = new CorrectiveActionPlanDTO();
		toUpdate.setId(updateRequest.getId());
		toUpdate.setAcbSummary(updateRequest.getAcbSummary());
		toUpdate.setActualCompletionDate(updateRequest.getActualCompletionDate());
		toUpdate.setApprovalDate(updateRequest.getApprovalDate());
		toUpdate.setCertifiedProductId(updateRequest.getCertifiedProductId());
		toUpdate.setDeveloperSummary(updateRequest.getDeveloperSummary());
		toUpdate.setEffectiveDate(updateRequest.getEffectiveDate());
		toUpdate.setEstimatedCompleteionDate(updateRequest.getEstimatedCompleteionDate());
		toUpdate.setResolution(updateRequest.getResolution());
		
		//update the plan info
		Long owningAcbId = null;
		CorrectiveActionPlanDTO existingPlan = capManager.getPlanById(updateRequest.getId());
		if(existingPlan.getCertifiedProductId() != null) {
			CertifiedProductDTO certifiedProduct = productManager.getById(existingPlan.getCertifiedProductId());
			if(certifiedProduct != null) {
				owningAcbId = certifiedProduct.getCertificationBodyId();
				capManager.update(owningAcbId, toUpdate);
			} else {
				throw new InvalidArgumentsException("Could not find the certified product for this plan.");
			}
		} else {
			throw new InvalidArgumentsException("No certified product id was found for this plan.");
		}
		
		//remove certifications that aren't there anymore
		List<CorrectiveActionPlanCertificationResultDTO> certsToDelete = new ArrayList<CorrectiveActionPlanCertificationResultDTO>();
		List<CorrectiveActionPlanCertificationResultDTO> existingCerts = capManager.getCertificationsForPlan(existingPlan.getId());
		for(int i = 0; i < existingCerts.size(); i++) {
			CorrectiveActionPlanCertificationResultDTO existingCert = existingCerts.get(i);
			boolean foundCert = false;
			for(int j = 0; j < updateRequest.getCertifications().size(); j++) {
				CorrectiveActionPlanCertificationResult updateCert = updateRequest.getCertifications().get(j);
				if(existingCert.getId().longValue() == updateCert.getId().longValue()) {
					foundCert = true;
				}
			}
			
			if(!foundCert) {
				CorrectiveActionPlanCertificationResultDTO certToDelete = new CorrectiveActionPlanCertificationResultDTO();
				certToDelete.setId(existingCert.getId());
				certsToDelete.add(certToDelete);
			}
		}
		if(certsToDelete.size() > 0) {
			capManager.removeCertificationsFromPlan(owningAcbId, certsToDelete);
		}
		
		//add certifications that weren't there before
		List<CorrectiveActionPlanCertificationResultDTO> certsToAdd = new ArrayList<CorrectiveActionPlanCertificationResultDTO>();
		existingCerts = capManager.getCertificationsForPlan(existingPlan.getId());
		for(int i = 0; i < updateRequest.getCertifications().size(); i++) {
			CorrectiveActionPlanCertificationResult updateCert = updateRequest.getCertifications().get(i);
			boolean foundCert = false;
			for(int j = 0; j < existingCerts.size(); j++) {
				CorrectiveActionPlanCertificationResultDTO existingCert = existingCerts.get(j);
				if(existingCert.getId().longValue() == updateCert.getId().longValue()) {
					foundCert = true;
				}
			}
			
			if(!foundCert) {
				CorrectiveActionPlanCertificationResultDTO certToAdd = new CorrectiveActionPlanCertificationResultDTO();
				certToAdd.setAcbSummary(updateCert.getAcbSummary());
				certToAdd.setCorrectiveActionPlanId(updateRequest.getId());
				certToAdd.setDeveloperSummary(updateCert.getDeveloperSummary());
				certToAdd.setResolution(updateCert.getResolution());
				
				CertificationCriterionDTO criterion = new CertificationCriterionDTO();
				criterion.setId(updateCert.getCertificationCriterionId());
				certToAdd.setCertCriterion(criterion);
				certsToAdd.add(certToAdd);
			}
		}
		if(certsToAdd.size() > 0) {
			capManager.addCertificationsToPlan(owningAcbId, updateRequest.getId(), certsToAdd);
		}
		//END 
		
		return capManager.getPlanDetails(toUpdate.getId());
	}
	
	@RequestMapping(value="/create", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody CorrectiveActionPlanDetails create(@RequestBody(required=true) CorrectiveActionPlanDetails createRequest) 
			throws EntityCreationException, EntityRetrievalException, JsonProcessingException,
			InvalidArgumentsException {
		CorrectiveActionPlanDTO toCreate = new CorrectiveActionPlanDTO();
		toCreate.setAcbSummary(createRequest.getAcbSummary());
		toCreate.setActualCompletionDate(createRequest.getActualCompletionDate());
		toCreate.setApprovalDate(createRequest.getApprovalDate());
		toCreate.setCertifiedProductId(createRequest.getCertifiedProductId());
		toCreate.setDeveloperSummary(createRequest.getDeveloperSummary());
		toCreate.setEffectiveDate(createRequest.getEffectiveDate());
		toCreate.setEstimatedCompleteionDate(createRequest.getEstimatedCompleteionDate());
		toCreate.setResolution(createRequest.getResolution());
		
		Long createdPlanId = null;
		Long acbId = null;
		
		//get the acb that owns the product to make sure we have permissions to create it		
		CertifiedProductDTO certifiedProduct = productManager.getById(toCreate.getCertifiedProductId());
		if(certifiedProduct != null) {
			acbId = certifiedProduct.getCertificationBodyId();
			CorrectiveActionPlanDetails createdPlan = capManager.create(acbId, toCreate);
			createdPlanId = createdPlan.getId();
		} else {
			throw new InvalidArgumentsException("Could not find the certified product for this plan.");
		}
		
		List<CorrectiveActionPlanCertificationResultDTO> certsToCreate = new ArrayList<CorrectiveActionPlanCertificationResultDTO>();
		if(createRequest.getCertifications() != null && createRequest.getCertifications().size() > 0) {
			for(CorrectiveActionPlanCertificationResult cert : createRequest.getCertifications()) {
				CorrectiveActionPlanCertificationResultDTO currCertToCreate = new CorrectiveActionPlanCertificationResultDTO();
				currCertToCreate.setAcbSummary(cert.getAcbSummary());
				currCertToCreate.setCorrectiveActionPlanId(createdPlanId);
				currCertToCreate.setDeveloperSummary(cert.getDeveloperSummary());
				currCertToCreate.setResolution(cert.getResolution());
				
				CertificationCriterionDTO criterion = new CertificationCriterionDTO();
				criterion.setId(cert.getCertificationCriterionId());
				currCertToCreate.setCertCriterion(criterion);
				certsToCreate.add(currCertToCreate);
			}
		}
		
		CorrectiveActionPlanDetails result = capManager.addCertificationsToPlan(acbId, createdPlanId, certsToCreate);
		return result;
	}
	
	@RequestMapping(value="/{planId}/delete", method= RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public String deleteAcb(@PathVariable("planId") Long planId) 
			throws JsonProcessingException, EntityCreationException, EntityRetrievalException,
				InvalidArgumentsException {
		
		//get the acb that owns the product to make sure we have permissions to update it
		CorrectiveActionPlanDTO existingPlan = capManager.getPlanById(planId);
		if(existingPlan.getCertifiedProductId() != null) {
			CertifiedProductDTO certifiedProduct = productManager.getById(existingPlan.getCertifiedProductId());
			if(certifiedProduct != null) {
				Long acbId = certifiedProduct.getCertificationBodyId();
				capManager.delete(acbId, planId);
			} else {
				throw new InvalidArgumentsException("Could not find the certified product for this plan.");
			}
		} else {
			throw new InvalidArgumentsException("No certified product id was found for this plan.");
		}
		return "{\"deleted\" : true }";
	}
}