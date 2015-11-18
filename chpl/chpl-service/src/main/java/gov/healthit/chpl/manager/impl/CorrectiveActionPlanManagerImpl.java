package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.CorrectiveActionPlanCertificationResultDAO;
import gov.healthit.chpl.dao.CorrectiveActionPlanDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.CorrectiveActionPlanDetails;
import gov.healthit.chpl.dto.CorrectiveActionPlanCertificationResultDTO;
import gov.healthit.chpl.dto.CorrectiveActionPlanDTO;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CorrectiveActionPlanManager;

@Service
public class CorrectiveActionPlanManagerImpl implements CorrectiveActionPlanManager {

	@Autowired CorrectiveActionPlanDAO capDao;
	@Autowired CorrectiveActionPlanCertificationResultDAO capCertDao;
	@Autowired public ActivityManager activityManager;
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public CorrectiveActionPlanDetails create(Long acbId, CorrectiveActionPlanDTO toCreate)
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		CorrectiveActionPlanDTO created = capDao.create(toCreate);
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, created.getId(), 
				"Corrective action plan with id "+ created.getId() +" was created.", null, created);

		return new CorrectiveActionPlanDetails(created, null);
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public CorrectiveActionPlanDetails addCertificationsToPlan(Long acbId,
			Long correctiveActionPlanId, List<CorrectiveActionPlanCertificationResultDTO> certs)
					throws EntityRetrievalException, EntityCreationException {
		
		for(CorrectiveActionPlanCertificationResultDTO toCreate : certs) {
			CorrectiveActionPlanCertificationResultDTO created = capCertDao.create(toCreate);
		}
		
		CorrectiveActionPlanDTO plan = capDao.getById(correctiveActionPlanId);
		List<CorrectiveActionPlanCertificationResultDTO> planCerts = capCertDao.getAllForCorrectiveActionPlan(correctiveActionPlanId);
		
		return new CorrectiveActionPlanDetails(plan, planCerts);
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public void removeCertificationsFromPlan(Long acbId, List<CorrectiveActionPlanCertificationResultDTO> certs)
					throws EntityRetrievalException {
		
		for(CorrectiveActionPlanCertificationResultDTO toDelete : certs) {
			capCertDao.delete(toDelete.getId());
		}
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

	public CorrectiveActionPlanDetails getPlanDetails(Long capId) throws EntityRetrievalException {
		CorrectiveActionPlanDTO plan = capDao.getById(capId);
		List<CorrectiveActionPlanCertificationResultDTO> planCerts = capCertDao.getAllForCorrectiveActionPlan(capId);
		
		return new CorrectiveActionPlanDetails(plan, planCerts);
	}
	
	public List<CorrectiveActionPlanDetails> getPlansForCertifiedProductDetails(Long certifiedProductId) 
		throws EntityRetrievalException {
		
		List<CorrectiveActionPlanDetails> result = new ArrayList<CorrectiveActionPlanDetails>();
		List<CorrectiveActionPlanDTO> plans = capDao.getAllForCertifiedProduct(certifiedProductId);
		for(CorrectiveActionPlanDTO plan : plans) {
			List<CorrectiveActionPlanCertificationResultDTO> planCerts = capCertDao.getAllForCorrectiveActionPlan(plan.getId());
			CorrectiveActionPlanDetails currDetails = new CorrectiveActionPlanDetails(plan, planCerts);
			result.add(currDetails);
		}
		
		return result;
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public CorrectiveActionPlanDTO update(Long acbId, CorrectiveActionPlanDTO toUpdate) throws EntityRetrievalException {
		return capDao.update(toUpdate);
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public void delete(Long acbId, Long capId) throws EntityRetrievalException {
		List<CorrectiveActionPlanCertificationResultDTO> planCerts = capCertDao.getAllForCorrectiveActionPlan(capId);
		if(planCerts != null && planCerts.size() > 0) {
			for(CorrectiveActionPlanCertificationResultDTO cert : planCerts) {
				capCertDao.delete(cert.getId());
			}
		}
		capDao.delete(capId);
	}

}
