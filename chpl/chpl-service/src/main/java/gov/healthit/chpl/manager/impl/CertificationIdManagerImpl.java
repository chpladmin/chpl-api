package gov.healthit.chpl.manager.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.CertificationIdDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.dto.CQMMetDTO;
import gov.healthit.chpl.dto.CertificationIdAndCertifiedProductDTO;
import gov.healthit.chpl.dto.CertificationIdDTO;
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
	
	public List<Long> getProductIdsById(Long id) throws EntityRetrievalException {
		return CertificationIdDAO.getProductIdsById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> getCriteriaNumbersMetByCertifiedProductIds(List<Long> productIds) {
		return CertificationIdDAO.getCriteriaNumbersMetByCertifiedProductIds(productIds);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CQMMetDTO> getCqmsMetByCertifiedProductIds(List<Long> productIds) {
		return 	CertificationIdDAO.getCqmsMetByCertifiedProductIds(productIds);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<String, Boolean> verifyByCertificationId(List<String> certificationIds) throws EntityRetrievalException {
		return CertificationIdDAO.verifyByCertificationId(certificationIds);
	}
	
	@Override
	@Transactional(readOnly = true) 
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CMS_STAFF') or hasRole('ROLE_ONC_STAFF')")
	public List<CertificationIdDTO> getAll() {
		return CertificationIdDAO.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ONC_STAFF')")
	public List<CertificationIdAndCertifiedProductDTO> getAllWithProducts() {
		return CertificationIdDAO.getAllCertificationIdsWithProducts();
	}
	
	@Override
	@Transactional(readOnly = false)
	@CacheEvict("certificationIds")
	public CertificationIdDTO create(List<Long> productIds, String year) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		CertificationIdDTO result = CertificationIdDAO.create(productIds, year);

		String activityMsg = "CertificationId "+result.getCertificationId()+" was created.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATIONID, result.getId(), activityMsg, null, result);
		return result;
	}
	
	@Override
	@Transactional(readOnly = false)
	@CacheEvict("certificationIds")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public CertificationIdDTO create(CertificationIdDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		CertificationIdDTO result = CertificationIdDAO.create(dto);
		
		String activityMsg = "CertificationId "+dto.getCertificationId()+" was created.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFICATIONID, result.getId(), activityMsg, null, result);
		return result;
	}
}
