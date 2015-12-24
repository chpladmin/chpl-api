package gov.healthit.chpl.manager.impl;

import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.EntityCreationException;
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
	
	public CertificationResultAdditionalSoftwareMapDTO addAdditionalSoftwareCertificationResultMapping(Long additionalSoftwareId, Long certificationResultId) throws EntityCreationException{
		
		CertificationResultAdditionalSoftwareMapDTO dto = new CertificationResultAdditionalSoftwareMapDTO();
		dto.setAdditionalSoftwareId(additionalSoftwareId);
		dto.setCertificationResultId(certificationResultId);
		
		CertificationResultAdditionalSoftwareMapDTO created = certificationResultDAO.createAdditionalSoftwareMapping(dto);
		return created;
	}
	
	public CQMResultAdditionalSoftwareMapDTO addAdditionalSoftwareCQMResultMapping(Long additionalSoftwareId, Long cqmResultId) throws EntityCreationException{
		
		CQMResultAdditionalSoftwareMapDTO dto = new CQMResultAdditionalSoftwareMapDTO();
		dto.setAdditionalSoftwareId(additionalSoftwareId);
		dto.setCqmResultId(cqmResultId);
		
		CQMResultAdditionalSoftwareMapDTO created = cqmResultDAO.createAdditionalSoftwareMapping(dto);
		return created;
		
	}
	public void associateAdditionalSoftwareCerifiedProductSelf(Long additionalSoftwareId, Long certifiedProductId){
		
	}
	
}
