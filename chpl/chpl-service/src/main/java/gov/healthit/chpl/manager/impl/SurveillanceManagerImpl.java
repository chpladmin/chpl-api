package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.SurveillanceCertificationResultDAO;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.SurveillanceDetails;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.SurveillanceCertificationResultDTO;
import gov.healthit.chpl.dto.SurveillanceDTO;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.SurveillanceManager;

@Service
public class SurveillanceManagerImpl implements SurveillanceManager {

	@Autowired CertifiedProductDAO cpDao;
	@Autowired SurveillanceDAO surveillanceDao;
	@Autowired SurveillanceCertificationResultDAO surveillanceCertDao;
	@Autowired public ActivityManager activityManager;
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public SurveillanceDetails create(Long acbId, SurveillanceDTO toCreate)
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		SurveillanceDTO created = surveillanceDao.create(toCreate);
		
		CertifiedProductDTO cpDto = cpDao.getById(created.getCertifiedProductId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, created.getId(), 
				"Surveillance for "+ cpDto.getChplProductNumberForActivity() +" was created.", null, created);

		return new SurveillanceDetails(created, null);
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public SurveillanceDetails addCertificationsToSurveillance(Long acbId,
			Long surveillanceId, List<SurveillanceCertificationResultDTO> certs)
					throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		SurveillanceDTO surveillance = surveillanceDao.getById(surveillanceId);
		CertifiedProductDTO cpDto = cpDao.getById(surveillance.getCertifiedProductId());

		for(SurveillanceCertificationResultDTO toCreate : certs) {
			SurveillanceCertificationResultDTO created = surveillanceCertDao.create(toCreate);
			activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, created.getId(), 
					"Added certification " + created.getCertCriterion().getNumber() + 
					" to a surveillance for certified product "+ cpDto.getChplProductNumberForActivity(), 
					null, created);
		}
		
		List<SurveillanceCertificationResultDTO> surveillanceCerts = surveillanceCertDao.getAllForSurveillance(surveillanceId);
		return new SurveillanceDetails(surveillance, surveillanceCerts);
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public void removeCertificationsFromSurveillance(Long acbId, List<SurveillanceCertificationResultDTO> certs)
					throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		for(SurveillanceCertificationResultDTO toDelete : certs) {
			SurveillanceCertificationResultDTO toDeleteWithAllInfo = surveillanceCertDao.getById(toDelete.getId());
			SurveillanceDTO surveillance = surveillanceDao.getById(toDeleteWithAllInfo.getSurveillanceId());
			CertifiedProductDTO cpDto = cpDao.getById(surveillance.getCertifiedProductId());
			
			surveillanceCertDao.delete(toDelete.getId());
			
			activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, toDeleteWithAllInfo.getId(), 
					"Deleted certification " + toDeleteWithAllInfo.getCertCriterion().getNumber() + 
					" from a surveillance for certified product "+ cpDto.getChplProductNumberForActivity(), 
					toDeleteWithAllInfo, null);
		}
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public SurveillanceCertificationResultDTO updateCertification(Long acbId, SurveillanceCertificationResultDTO cert)
					throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		SurveillanceCertificationResultDTO originalCert = surveillanceCertDao.getById(cert.getId());		
		SurveillanceDTO surveillance = surveillanceDao.getById(originalCert.getSurveillanceId());
		CertifiedProductDTO cpDto = cpDao.getById(surveillance.getCertifiedProductId());
		
		SurveillanceCertificationResultDTO updatedCert = surveillanceCertDao.update(cert);

		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, cert.getId(), 
				"Updated information for certification " + originalCert.getCertCriterion().getNumber() + 
				" in a surveillance for certified product "+ cpDto.getChplProductNumberForActivity(), 
				originalCert, updatedCert);
		
		return updatedCert;
	}
	
	@Override
	public SurveillanceDTO getSurveillanceById(Long surveillanceId) throws EntityRetrievalException {
		return surveillanceDao.getById(surveillanceId);
	}

	@Override
	public List<SurveillanceDTO> getSurveillanceForCertifiedProduct(Long certifiedProductId)
			throws EntityRetrievalException {
		return surveillanceDao.getAllForCertifiedProduct(certifiedProductId);
	}

	@Override
	public List<SurveillanceCertificationResultDTO> getCertificationsForSurveillance(Long surveillanceId)
			throws EntityRetrievalException {
		return surveillanceCertDao.getAllForSurveillance(surveillanceId);
	}
	
	public SurveillanceDetails getSurveillanceDetails(Long surveillanceId) throws EntityRetrievalException {
		SurveillanceDTO plan = surveillanceDao.getById(surveillanceId);
		List<SurveillanceCertificationResultDTO> surveillanceCerts = surveillanceCertDao.getAllForSurveillance(surveillanceId);
		
		return new SurveillanceDetails(plan, surveillanceCerts);
	}
	
	public List<SurveillanceDetails> getSurveillanceForCertifiedProductDetails(Long certifiedProductId) 
		throws EntityRetrievalException {
		
		List<SurveillanceDetails> result = new ArrayList<SurveillanceDetails>();
		List<SurveillanceDTO> surveillances = surveillanceDao.getAllForCertifiedProduct(certifiedProductId);
		for(SurveillanceDTO surveillance : surveillances) {
			List<SurveillanceCertificationResultDTO> surveillanceCerts = surveillanceCertDao.getAllForSurveillance(surveillance.getId());
			SurveillanceDetails currDetails = new SurveillanceDetails(surveillance, surveillanceCerts);
			result.add(currDetails);
		}

		return result;
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public SurveillanceDTO update(Long acbId, SurveillanceDTO toUpdate) 
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		SurveillanceDTO orig = surveillanceDao.getById(toUpdate.getId());
		SurveillanceDTO updated = surveillanceDao.update(toUpdate);
		
		CertifiedProductDTO cpDto = cpDao.getById(updated.getCertifiedProductId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, updated.getId(), 
				"Surveillance for "+ cpDto.getChplProductNumberForActivity() +" was updated.", 
				orig, updated);
		
		return updated;
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public void delete(Long acbId, Long surveillanceId) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		List<SurveillanceCertificationResultDTO> surveillanceCerts = surveillanceCertDao.getAllForSurveillance(surveillanceId);
		if(surveillanceCerts != null && surveillanceCerts.size() > 0) {
			for(SurveillanceCertificationResultDTO cert : surveillanceCerts) {
				surveillanceCertDao.delete(cert.getId());
			}
		}
		
		SurveillanceDTO toDelete = surveillanceDao.getById(surveillanceId);
		surveillanceDao.delete(surveillanceId);
		
		CertifiedProductDTO cpDto = cpDao.getById(toDelete.getCertifiedProductId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, toDelete.getId(), 
				"Surveillance for "+ cpDto.getChplProductNumberForActivity() +" was deleted.", 
				toDelete, null);

	}

}
