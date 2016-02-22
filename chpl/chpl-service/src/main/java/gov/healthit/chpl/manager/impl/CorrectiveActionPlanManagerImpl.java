package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CorrectiveActionPlanCertificationResultDAO;
import gov.healthit.chpl.dao.CorrectiveActionPlanDAO;
import gov.healthit.chpl.dao.CorrectiveActionPlanDocumentationDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.CorrectiveActionPlanDetails;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CorrectiveActionPlanCertificationResultDTO;
import gov.healthit.chpl.dto.CorrectiveActionPlanDTO;
import gov.healthit.chpl.dto.CorrectiveActionPlanDocumentationDTO;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CorrectiveActionPlanManager;

@Service
public class CorrectiveActionPlanManagerImpl implements CorrectiveActionPlanManager {

	@Autowired CertifiedProductDAO cpDao;
	@Autowired CorrectiveActionPlanDAO capDao;
	@Autowired CorrectiveActionPlanCertificationResultDAO capCertDao;
	@Autowired CorrectiveActionPlanDocumentationDAO capDocDao;
	@Autowired public ActivityManager activityManager;
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public CorrectiveActionPlanDetails create(Long acbId, CorrectiveActionPlanDTO toCreate)
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		CorrectiveActionPlanDTO created = capDao.create(toCreate);
		
		CertifiedProductDTO cpDto = cpDao.getById(created.getCertifiedProductId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, created.getId(), 
				"Corrective action plan for "+ cpDto.getChplProductNumberForActivity() +" was created.", null, created);

		return new CorrectiveActionPlanDetails(created, null);
	}


	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public CorrectiveActionPlanDocumentationDTO addDocumentationToPlan(Long acbId, CorrectiveActionPlanDocumentationDTO doc)
		throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		CorrectiveActionPlanDTO plan = capDao.getById(doc.getCorrectiveActionPlanId());
		CertifiedProductDTO cpDto = cpDao.getById(plan.getCertifiedProductId());
		
		CorrectiveActionPlanDocumentationDTO created = capDocDao.create(doc);
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, created.getId(), 
				"A file " + doc.getFileName() + " was added to a corrective action plan for certified product "+ cpDto.getChplProductNumberForActivity(), null, created);
		return created;
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public CorrectiveActionPlanDetails addCertificationsToPlan(Long acbId,
			Long correctiveActionPlanId, List<CorrectiveActionPlanCertificationResultDTO> certs)
					throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		CorrectiveActionPlanDTO plan = capDao.getById(correctiveActionPlanId);
		CertifiedProductDTO cpDto = cpDao.getById(plan.getCertifiedProductId());

		for(CorrectiveActionPlanCertificationResultDTO toCreate : certs) {
			CorrectiveActionPlanCertificationResultDTO created = capCertDao.create(toCreate);
			activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, created.getId(), 
					"Added certification " + created.getCertCriterion().getNumber() + 
					" to a corrective action plan for certified product "+ cpDto.getChplProductNumberForActivity(), 
					null, created);
		}
		
		List<CorrectiveActionPlanCertificationResultDTO> planCerts = capCertDao.getAllForCorrectiveActionPlan(correctiveActionPlanId);
		return new CorrectiveActionPlanDetails(plan, planCerts);
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public void removeCertificationsFromPlan(Long acbId, List<CorrectiveActionPlanCertificationResultDTO> certs)
					throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		for(CorrectiveActionPlanCertificationResultDTO toDelete : certs) {
			CorrectiveActionPlanCertificationResultDTO toDeleteWithAllInfo = capCertDao.getById(toDelete.getId());
			CorrectiveActionPlanDTO plan = capDao.getById(toDeleteWithAllInfo.getCorrectiveActionPlanId());
			CertifiedProductDTO cpDto = cpDao.getById(plan.getCertifiedProductId());
			
			capCertDao.delete(toDelete.getId());
			
			activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, toDeleteWithAllInfo.getId(), 
					"Deleted certification " + toDeleteWithAllInfo.getCertCriterion().getNumber() + 
					" from a corrective action plan for certified product "+ cpDto.getChplProductNumberForActivity(), 
					toDeleteWithAllInfo, null);
		}
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public CorrectiveActionPlanCertificationResultDTO updateCertification(Long acbId, CorrectiveActionPlanCertificationResultDTO cert)
					throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		CorrectiveActionPlanCertificationResultDTO originalCert = capCertDao.getById(cert.getId());		
		CorrectiveActionPlanDTO plan = capDao.getById(originalCert.getCorrectiveActionPlanId());
		CertifiedProductDTO cpDto = cpDao.getById(plan.getCertifiedProductId());
		
		CorrectiveActionPlanCertificationResultDTO updatedCert = capCertDao.update(cert);

		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, cert.getId(), 
				"Updated information for certification " + originalCert.getCertCriterion().getNumber() + 
				" in a corrective action plan for certified product "+ cpDto.getChplProductNumberForActivity(), 
				originalCert, updatedCert);
		
		return updatedCert;
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public void removeDocumentation(Long acbId, CorrectiveActionPlanDocumentationDTO toRemove) 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		CorrectiveActionPlanDTO plan = capDao.getById(toRemove.getCorrectiveActionPlanId());
		CertifiedProductDTO cpDto = cpDao.getById(plan.getCertifiedProductId());
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, toRemove.getId(), 
				"Removed uploaded file " + toRemove.getFileName() + 
				" from a corrective action plan for certified product "+ cpDto.getChplProductNumberForActivity(), 
				toRemove, null);
		
		capDocDao.delete(toRemove.getId());
	}
	
	@Override
	public CorrectiveActionPlanDTO getPlanById(Long capId) throws EntityRetrievalException {
		return capDao.getById(capId);
	}

	@Override
	public List<CorrectiveActionPlanDTO> getPlansForCertifiedProduct(Long certifiedProductId)
			throws EntityRetrievalException {
		return capDao.getAllForCertifiedProduct(certifiedProductId);
	}

	@Override
	public List<CorrectiveActionPlanCertificationResultDTO> getCertificationsForPlan(Long capId)
			throws EntityRetrievalException {
		return capCertDao.getAllForCorrectiveActionPlan(capId);
	}

	@Override
	public List<CorrectiveActionPlanDocumentationDTO> getDocumentationForPlan(Long capId)
			throws EntityRetrievalException {
		return capDocDao.getAllForCorrectiveActionPlan(capId);
	}
	
	@Override
	public CorrectiveActionPlanDocumentationDTO getDocumentationById(Long docId) throws EntityRetrievalException {
		return capDocDao.getById(docId);
	}
	
	public CorrectiveActionPlanDetails getPlanDetails(Long capId) throws EntityRetrievalException {
		CorrectiveActionPlanDTO plan = capDao.getById(capId);
		List<CorrectiveActionPlanCertificationResultDTO> planCerts = capCertDao.getAllForCorrectiveActionPlan(capId);
		List<CorrectiveActionPlanDocumentationDTO> planDocs = capDocDao.getAllForCorrectiveActionPlan(capId);
		
		return new CorrectiveActionPlanDetails(plan, planCerts, planDocs);
	}
	
	public List<CorrectiveActionPlanDetails> getPlansForCertifiedProductDetails(Long certifiedProductId) 
		throws EntityRetrievalException {
		
		List<CorrectiveActionPlanDetails> result = new ArrayList<CorrectiveActionPlanDetails>();
		List<CorrectiveActionPlanDTO> plans = capDao.getAllForCertifiedProduct(certifiedProductId);
		for(CorrectiveActionPlanDTO plan : plans) {
			List<CorrectiveActionPlanCertificationResultDTO> planCerts = capCertDao.getAllForCorrectiveActionPlan(plan.getId());
			List<CorrectiveActionPlanDocumentationDTO> planDocs = capDocDao.getAllForCorrectiveActionPlan(plan.getId());
			CorrectiveActionPlanDetails currDetails = new CorrectiveActionPlanDetails(plan, planCerts, planDocs);
			result.add(currDetails);
		}

		return result;
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public CorrectiveActionPlanDTO update(Long acbId, CorrectiveActionPlanDTO toUpdate) 
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		CorrectiveActionPlanDTO origPlan = capDao.getById(toUpdate.getId());
		CorrectiveActionPlanDTO updatedPlan = capDao.update(toUpdate);
		
		CertifiedProductDTO cpDto = cpDao.getById(updatedPlan.getCertifiedProductId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, updatedPlan.getId(), 
				"Corrective action plan for "+ cpDto.getChplProductNumberForActivity() +" was updated.", 
				origPlan, updatedPlan);
		
		return updatedPlan;
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public void delete(Long acbId, Long capId) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		List<CorrectiveActionPlanCertificationResultDTO> planCerts = capCertDao.getAllForCorrectiveActionPlan(capId);
		if(planCerts != null && planCerts.size() > 0) {
			for(CorrectiveActionPlanCertificationResultDTO cert : planCerts) {
				capCertDao.delete(cert.getId());
			}
		}
		
		List<CorrectiveActionPlanDocumentationDTO> planDocs = capDocDao.getAllForCorrectiveActionPlan(capId); 
		if(planDocs != null && planDocs.size() > 0) {
			for(CorrectiveActionPlanDocumentationDTO doc : planDocs) {
				capDocDao.delete(doc.getId());
			}
		}
		
		CorrectiveActionPlanDTO planToDelete = capDao.getById(capId);
		capDao.delete(capId);
		
		CertifiedProductDTO cpDto = cpDao.getById(planToDelete.getCertifiedProductId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, planToDelete.getId(), 
				"Corrective action plan for "+ cpDto.getChplProductNumberForActivity() +" was deleted.", 
				planToDelete, null);

	}

}
