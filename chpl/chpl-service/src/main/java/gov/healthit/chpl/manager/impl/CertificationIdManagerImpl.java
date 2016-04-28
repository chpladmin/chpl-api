package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.CertificationIdDAO;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.entity.CertificationIdEntity;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationIdManager;

@Service
public class CertificationIdManagerImpl implements CertificationIdManager {

	@Autowired CertificationIdDAO CertificationIdDAO;
	
	@Autowired
	ActivityManager activityManager;

	@Override
	@Transactional(readOnly = true)
	public CertificationIdDTO getByProductIds(List<Long> productIds, String year) throws EntityRetrievalException {
		return CertificationIdDAO.getByProductIds(productIds, year);
	}
	
	@Override
	@Transactional(readOnly = true)
	public CertificationIdDTO getById(Long id) throws EntityRetrievalException {
		return CertificationIdDAO.getById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public CertificationIdDTO getByCertificationId(String certificationId) throws EntityRetrievalException {
		return CertificationIdDAO.getByCertificationId(certificationId);
	}

	@Override
	@Transactional(readOnly = true) 
	public List<CertificationIdDTO> getAll() {
		return CertificationIdDAO.findAll();
	}

	@Override
	@Transactional(readOnly = false)
	public CertificationIdDTO create(List<Long> productIds, String year) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		CertificationIdDTO result = CertificationIdDAO.create(productIds, year);

		String activityMsg = "CertificationId "+result.getCertificationId()+" was created.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATIONID, result.getId(), activityMsg, null, result);
		return result;
	}
	
	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public CertificationIdDTO create(CertificationIdDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		CertificationIdDTO result = CertificationIdDAO.create(dto);
		
		String activityMsg = "CertificationId "+dto.getCertificationId()+" was created.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATIONID, result.getId(), activityMsg, null, result);
		return result;
	}

}
