package gov.healthit.chpl.manager.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareMapDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.manager.AdditionalSoftwareManager;
import gov.healthit.chpl.manager.CertificationResultManager;

public class CertificationResultManagerImpl implements
		CertificationResultManager {
	
	@Autowired
	private CertificationResultDAO certResultDAO;
	
	@Autowired
	private AdditionalSoftwareManager additionalSoftwareManager;

	@Override
	public void create(CertificationResultDTO result) throws EntityCreationException {
		
		CertificationResultDTO created = certResultDAO.create(result);
		
		for (AdditionalSoftwareDTO additionalSoftware : result.getAdditionalSoftware()){
			
			AdditionalSoftwareDTO createdAddlSoftware = additionalSoftwareManager.createAdditionalSoftware(additionalSoftware);
			
			CertificationResultAdditionalSoftwareMapDTO additionalSoftwareMapping
				= new CertificationResultAdditionalSoftwareMapDTO();
			
			additionalSoftwareMapping.setCertificationResultId(created.getId());
			additionalSoftwareMapping.setAdditionalSoftwareId(createdAddlSoftware.getId());
			additionalSoftwareMapping.setDeleted(false);
			additionalSoftwareMapping.setCreationDate(new Date());
			additionalSoftwareMapping.setLastModifiedDate(new Date());
			additionalSoftwareMapping.setLastModifiedUser(Util.getCurrentUser().getId());;
			
			createAdditionalSoftwareMapping(additionalSoftwareMapping);	
		}
	}

	@Override
	public void update(CertificationResultDTO result) throws EntityRetrievalException, EntityCreationException {
		
		CertificationResultDTO updated = certResultDAO.update(result);
		
		for (AdditionalSoftwareDTO additionalSoftware : result.getAdditionalSoftware()){
			
			AdditionalSoftwareDTO createdAddlSoftware = additionalSoftwareManager.createAdditionalSoftware(additionalSoftware);
			
			CertificationResultAdditionalSoftwareMapDTO additionalSoftwareMapping
				= new CertificationResultAdditionalSoftwareMapDTO();
			
			additionalSoftwareMapping.setCertificationResultId(updated.getId());
			additionalSoftwareMapping.setAdditionalSoftwareId(createdAddlSoftware.getId());
			additionalSoftwareMapping.setDeleted(false);
			additionalSoftwareMapping.setCreationDate(new Date());
			additionalSoftwareMapping.setLastModifiedDate(new Date());
			additionalSoftwareMapping.setLastModifiedUser(Util.getCurrentUser().getId());
			
			createAdditionalSoftwareMapping(additionalSoftwareMapping);	
		}
		
	}

	@Override
	public void delete(Long resultId) {
		certResultDAO.delete(resultId);
	}

	@Override
	public void deleteByCertifiedProductId(Long certifiedProductId) {
		certResultDAO.deleteByCertifiedProductId(certifiedProductId);
	}

	@Override
	public List<CertificationResultDTO> getAll() {
		return certResultDAO.findAll();
	}

	@Override
	public CertificationResultDTO getById(Long resultId) throws EntityRetrievalException {
		return certResultDAO.getById(resultId);
	}

	@Override
	public CertificationResultAdditionalSoftwareMapDTO createAdditionalSoftwareMapping(
			CertificationResultAdditionalSoftwareMapDTO dto)
			throws EntityCreationException {
		return certResultDAO.createAdditionalSoftwareMapping(dto);
	}

	@Override
	public CertificationResultAdditionalSoftwareMapDTO updateAdditionalSoftwareMapping(
			CertificationResultAdditionalSoftwareMapDTO dto) {
		return certResultDAO.updateAdditionalSoftwareMapping(dto);
	}

	@Override
	public void deleteAdditionalSoftwareMapping(Long certificationResultId,
			Long additionalSoftwareId) {
		certResultDAO.deleteAdditionalSoftwareMapping(certificationResultId, additionalSoftwareId);
	}

	@Override
	public CertificationResultAdditionalSoftwareMapDTO getAdditionalSoftwareMapping(
			Long certificationResultId, Long additionalSoftwareId) {
		return certResultDAO.getAdditionalSoftwareMapping(certificationResultId, additionalSoftwareId);
	}
	
	@Override
	public List<CertificationResultAdditionalSoftwareMapDTO> getAdditionalSoftwareMappingsForCertificationResult(Long certificationResultId){
		return certResultDAO.getCertificationResultAdditionalSoftwareMappings(certificationResultId);
	}
	
	
	
}