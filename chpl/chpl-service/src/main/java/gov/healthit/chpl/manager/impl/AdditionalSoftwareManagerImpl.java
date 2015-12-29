package gov.healthit.chpl.manager.impl;

import gov.healthit.chpl.dao.AdditionalSoftwareDAO;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CQMResultAdditionalSoftwareMapDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareMapDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AdditionalSoftwareManagerImpl {
	
	@Autowired
	private CertificationResultDAO certificationResultDAO;
	
	@Autowired
	private CQMResultDAO cqmResultDAO;
	
	@Autowired
	private AdditionalSoftwareDAO additionalSoftwareDAO;
	
	
	public CertificationResultAdditionalSoftwareMapDTO addAdditionalSoftwareCertificationResultMapping(Long additionalSoftwareId, Long certificationResultId) throws EntityCreationException{
		
		CertificationResultAdditionalSoftwareMapDTO dto = new CertificationResultAdditionalSoftwareMapDTO();
		dto.setAdditionalSoftwareId(additionalSoftwareId);
		dto.setCertificationResultId(certificationResultId);
		
		// "CERTIFICATION"),
		CertificationResultAdditionalSoftwareMapDTO created = certificationResultDAO.createAdditionalSoftwareMapping(dto);
		return created;
	}
	
	public CQMResultAdditionalSoftwareMapDTO addAdditionalSoftwareCQMResultMapping(Long additionalSoftwareId, Long cqmResultId) throws EntityCreationException{
		
		CQMResultAdditionalSoftwareMapDTO dto = new CQMResultAdditionalSoftwareMapDTO();
		dto.setAdditionalSoftwareId(additionalSoftwareId);
		dto.setCqmResultId(cqmResultId);
		
		// "CQM"),
		CQMResultAdditionalSoftwareMapDTO created = cqmResultDAO.createAdditionalSoftwareMapping(dto);
		return created;
		
	}
	
	public void deleteAdditionalSoftwareCertificationResultMapping(Long additionalSoftwareId, Long certificationResultId){
		
		CertificationResultAdditionalSoftwareMapDTO dto = certificationResultDAO.getAdditionalSoftwareMapping(certificationResultId, additionalSoftwareId);
		dto.setDeleted(true);
		certificationResultDAO.updateAdditionalSoftwareMapping(dto);
		
	}
	
	public void deleteAdditionalSoftwareCQMResultMapping(Long additionalSoftwareId, Long cqmResultId){
		
		CQMResultAdditionalSoftwareMapDTO dto = cqmResultDAO.getAdditionalSoftwareMapping(cqmResultId, additionalSoftwareId);
		dto.setDeleted(true);
		cqmResultDAO.updateAdditionalSoftwareMapping(dto);
		
	}
	
	public void associateAdditionalSoftwareCerifiedProductSelf(Long additionalSoftwareId, Long certifiedProductId) throws EntityRetrievalException{
		
		AdditionalSoftwareDTO dto = additionalSoftwareDAO.getById(additionalSoftwareId);
		dto.setCertifiedProductSelfId(certifiedProductId);
		additionalSoftwareDAO.update(dto);
		
	}
	
}
