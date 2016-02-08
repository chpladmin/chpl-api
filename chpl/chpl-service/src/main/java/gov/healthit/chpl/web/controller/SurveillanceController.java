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
import gov.healthit.chpl.domain.SurveillanceCertificationResult;
import gov.healthit.chpl.domain.SurveillanceDetails;
import gov.healthit.chpl.domain.SurveillanceResults;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.SurveillanceCertificationResultDTO;
import gov.healthit.chpl.dto.SurveillanceDTO;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="surveillance")
@RestController
@RequestMapping("/surveillance")
public class SurveillanceController {
	
	private static final Logger logger = LogManager.getLogger(SurveillanceController.class);

	@Autowired SurveillanceManager surveillanceManager;
	@Autowired CertifiedProductManager productManager;
	
	@ApiOperation(value="List surveillance events for a certified product.", 
			notes="List all surveillance events, both open and resolved, for a certified product.")
	@RequestMapping(value="/", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SurveillanceResults getSurveillancesForCertifiedProduct(
			@RequestParam(value = "certifiedProductId", required=true) Long cpId) throws EntityRetrievalException {
		List<SurveillanceDetails> surveillances = surveillanceManager.getSurveillanceForCertifiedProductDetails(cpId);
		
		SurveillanceResults results = new SurveillanceResults();
		results.getSurveillances().addAll(surveillances);
		return results;
	}
	
	@ApiOperation(value="Get surveillance event details.", 
			notes="Get all of the information about a specific surveillance event.")
	@RequestMapping(value="/{surId}", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody SurveillanceDetails getSurveillanceByid(@PathVariable("surId") Long surId) throws EntityRetrievalException {
		return surveillanceManager.getSurveillanceDetails(surId);
	}
	
	@ApiOperation(value="Update a surveillance event.", 
			notes="The logged in user must have ROLE_ADMIN or ROLE_ACB_ADMIN and administrative "
					+ "authority on the ACB associated with the surveillance event.")
	@RequestMapping(value="/update", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody SurveillanceDetails update(@RequestBody(required=true) SurveillanceDetails updateRequest) 
		throws EntityCreationException, EntityRetrievalException, JsonProcessingException,
		InvalidArgumentsException {
		
		SurveillanceDTO toUpdate = new SurveillanceDTO();
		toUpdate.setId(updateRequest.getId());
		toUpdate.setCertifiedProductId(updateRequest.getCertifiedProductId());
		toUpdate.setStartDate(updateRequest.getStartDate());
		toUpdate.setEndDate(updateRequest.getEndDate());
		
		//update the plan info
		Long owningAcbId = null;
		SurveillanceDTO existingSur = surveillanceManager.getSurveillanceById(updateRequest.getId());
		if(existingSur.getCertifiedProductId() != null) {
			CertifiedProductDTO certifiedProduct = productManager.getById(existingSur.getCertifiedProductId());
			if(certifiedProduct != null) {
				owningAcbId = certifiedProduct.getCertificationBodyId();
				surveillanceManager.update(owningAcbId, toUpdate);
			} else {
				throw new InvalidArgumentsException("Could not find the certified product for this plan.");
			}
		} else {
			throw new InvalidArgumentsException("No certified product id was found for this plan.");
		}
		
		//update data for any certifications that already exist
		List<SurveillanceCertificationResultDTO> existingCerts = surveillanceManager.getCertificationsForSurveillance(existingSur.getId());
		for(int i = 0; i < existingCerts.size(); i++) {
			SurveillanceCertificationResultDTO existingCert = existingCerts.get(i);
			for(int j = 0; j < updateRequest.getCertifications().size(); j++) {
				SurveillanceCertificationResult updateCert = updateRequest.getCertifications().get(j);
				if(existingCert.getCertCriterion().getNumber().equals(updateCert.getCertificationCriterionNumber())) {
					existingCert.setNumSites(updateCert.getNumSites());
					existingCert.setPassRate(updateCert.getPassRate());
					existingCert.setResults(updateCert.getResult());
					existingCert.setSurveillanceId(updateRequest.getId());
					surveillanceManager.updateCertification(owningAcbId, existingCert);
				}
			}
		}
		
		//remove certifications that aren't there anymore
		List<SurveillanceCertificationResultDTO> certsToDelete = new ArrayList<SurveillanceCertificationResultDTO>();
		existingCerts = surveillanceManager.getCertificationsForSurveillance(existingSur.getId());
		for(int i = 0; i < existingCerts.size(); i++) {
			SurveillanceCertificationResultDTO existingCert = existingCerts.get(i);
			boolean foundCert = false;
			for(int j = 0; j < updateRequest.getCertifications().size(); j++) {
				SurveillanceCertificationResult updateCert = updateRequest.getCertifications().get(j);
				if(existingCert.getCertCriterion().getNumber().equals(updateCert.getCertificationCriterionNumber())) {
					foundCert = true;
				}
			}
			
			if(!foundCert) {
				SurveillanceCertificationResultDTO certToDelete = new SurveillanceCertificationResultDTO();
				certToDelete.setId(existingCert.getId());
				certToDelete.setSurveillanceId(updateRequest.getId());
				certsToDelete.add(certToDelete);
			}
		}
		if(certsToDelete.size() > 0) {
			surveillanceManager.removeCertificationsFromSurveillance(owningAcbId, certsToDelete);
		}
		
		//add certifications that weren't there before
		List<SurveillanceCertificationResultDTO> certsToAdd = new ArrayList<SurveillanceCertificationResultDTO>();
		existingCerts = surveillanceManager.getCertificationsForSurveillance(existingSur.getId());
		for(int i = 0; i < updateRequest.getCertifications().size(); i++) {
			SurveillanceCertificationResult updateCert = updateRequest.getCertifications().get(i);
			boolean foundCert = false;
			for(int j = 0; j < existingCerts.size(); j++) {
				SurveillanceCertificationResultDTO existingCert = existingCerts.get(j);
				if(existingCert.getCertCriterion().getNumber().equals(updateCert.getCertificationCriterionNumber())) {
					foundCert = true;
				}
			}
			
			if(!foundCert) {
				SurveillanceCertificationResultDTO certToAdd = new SurveillanceCertificationResultDTO();
				certToAdd.setNumSites(updateCert.getNumSites());
				certToAdd.setPassRate(updateCert.getPassRate());
				certToAdd.setResults(updateCert.getResult());
				certToAdd.setSurveillanceId(updateRequest.getId());
				
				CertificationCriterionDTO criterion = new CertificationCriterionDTO();
				criterion.setNumber(updateCert.getCertificationCriterionNumber());
				certToAdd.setCertCriterion(criterion);
				certsToAdd.add(certToAdd);
			}
		}
		if(certsToAdd.size() > 0) {
			surveillanceManager.addCertificationsToSurveillance(owningAcbId, updateRequest.getId(), certsToAdd);
		}
		//END 
		
		return surveillanceManager.getSurveillanceDetails(toUpdate.getId());
	}
	
	@ApiOperation(value="Create a new surveillance event.", 
			notes="The logged in user"
					+ " must have either ROLE_ADMIN or ROLE_ACB_ADMIN and administrative "
					+ " authority on the associated ACB.")
	@RequestMapping(value="/create", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public @ResponseBody SurveillanceDetails create(@RequestBody(required=true) SurveillanceDetails createRequest) 
			throws EntityCreationException, EntityRetrievalException, JsonProcessingException,
			InvalidArgumentsException {
		SurveillanceDTO toCreate = new SurveillanceDTO();
		toCreate.setEndDate(createRequest.getEndDate());
		toCreate.setStartDate(createRequest.getStartDate());
		toCreate.setCertifiedProductId(createRequest.getCertifiedProductId());
		
		Long createdSurId = null;
		Long acbId = null;
		
		//get the acb that owns the product to make sure we have permissions to create it		
		CertifiedProductDTO certifiedProduct = productManager.getById(toCreate.getCertifiedProductId());
		if(certifiedProduct != null) {
			acbId = certifiedProduct.getCertificationBodyId();
			SurveillanceDetails createdPlan = surveillanceManager.create(acbId, toCreate);
			createdSurId = createdPlan.getId();
		} else {
			throw new InvalidArgumentsException("Could not find the certified product for this surveilllance.");
		}
		
		List<SurveillanceCertificationResultDTO> certsToCreate = new ArrayList<SurveillanceCertificationResultDTO>();
		if(createRequest.getCertifications() != null && createRequest.getCertifications().size() > 0) {
			for(SurveillanceCertificationResult cert : createRequest.getCertifications()) {
				SurveillanceCertificationResultDTO currCertToCreate = new SurveillanceCertificationResultDTO();
				currCertToCreate.setNumSites(cert.getNumSites());
				currCertToCreate.setPassRate(cert.getPassRate());
				currCertToCreate.setResults(cert.getResult());
				currCertToCreate.setSurveillanceId(createdSurId);
				
				CertificationCriterionDTO criterion = new CertificationCriterionDTO();
				criterion.setId(cert.getCertificationCriterionId());
				criterion.setNumber(cert.getCertificationCriterionNumber());
				currCertToCreate.setCertCriterion(criterion);
				certsToCreate.add(currCertToCreate);
			}
		}
		
		SurveillanceDetails result = surveillanceManager.addCertificationsToSurveillance(acbId, createdSurId, certsToCreate);
		return result;
	}
	
	@ApiOperation(value="Delete a surveillance event.", 
			notes="The logged in user"
					+ " must have either ROLE_ADMIN or ROLE_ACB_ADMIN and administrative "
					+ " authority on the associated ACB.")
	@RequestMapping(value="/{surId}/delete", method= RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public String delete(@PathVariable("surId") Long surId) 
			throws JsonProcessingException, EntityCreationException, EntityRetrievalException,
				InvalidArgumentsException {
		
		//get the acb that owns the product to make sure we have permissions to update it
		SurveillanceDTO existingPlan = surveillanceManager.getSurveillanceById(surId);
		if(existingPlan.getCertifiedProductId() != null) {
			CertifiedProductDTO certifiedProduct = productManager.getById(existingPlan.getCertifiedProductId());
			if(certifiedProduct != null) {
				Long acbId = certifiedProduct.getCertificationBodyId();
				surveillanceManager.delete(acbId, surId);
			} else {
				throw new InvalidArgumentsException("Could not find the certified product for this plan.");
			}
		} else {
			throw new InvalidArgumentsException("No certified product id was found for this plan.");
		}
		return "{\"deleted\" : true }";
	}
}