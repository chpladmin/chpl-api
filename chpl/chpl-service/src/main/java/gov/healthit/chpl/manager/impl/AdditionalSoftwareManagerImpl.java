package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dao.AdditionalSoftwareDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.AdditionalSoftware;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareMapDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.manager.AdditionalSoftwareManager;
import gov.healthit.chpl.manager.CertifiedProductManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AdditionalSoftwareManagerImpl implements AdditionalSoftwareManager {
	
	@Autowired
	private CertificationResultDAO certificationResultDAO;
	
	@Autowired
	private AdditionalSoftwareDAO additionalSoftwareDAO;
	
	@Autowired
	private CertifiedProductManager certifiedProductManager;
	
	
	@Override
	public AdditionalSoftwareDTO createAdditionalSoftware(AdditionalSoftwareDTO toCreate) throws EntityCreationException{
		return additionalSoftwareDAO.create(toCreate);
	}
	
	
	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public CertificationResultAdditionalSoftwareMapDTO addAdditionalSoftwareCertificationResultMapping(Long additionalSoftwareId, Long certificationResultId) throws EntityCreationException{
		
		CertificationResultAdditionalSoftwareMapDTO dto = new CertificationResultAdditionalSoftwareMapDTO();
		dto.setAdditionalSoftwareId(additionalSoftwareId);
		dto.setCertificationResultId(certificationResultId);
		
		// "CERTIFICATION"),
		CertificationResultAdditionalSoftwareMapDTO created = certificationResultDAO.createAdditionalSoftwareMapping(dto);
		return created;
	}
	
	
	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public void deleteAdditionalSoftwareCertificationResultMapping(Long additionalSoftwareId, Long certificationResultId){
		
		certificationResultDAO.deleteAdditionalSoftwareMapping(certificationResultId, additionalSoftwareId);
	}
	
	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public void associateAdditionalSoftwareCertifiedProductSelf(Long additionalSoftwareId, Long certifiedProductId) throws EntityRetrievalException{
		
		AdditionalSoftwareDTO dto = additionalSoftwareDAO.getById(additionalSoftwareId);
		dto.setCertifiedProductSelfId(certifiedProductId);
		additionalSoftwareDAO.update(dto);
		
	}
	
	@Override
	public List<AdditionalSoftware> getAdditionalSoftwareByCertificationResultId(Long id) throws EntityRetrievalException{
		
		List<AdditionalSoftwareDTO> dtos = additionalSoftwareDAO.findByCertificationResultId(id);
		List<AdditionalSoftware> additionalSoftware = new ArrayList<>();
		
		for (AdditionalSoftwareDTO dto : dtos){	
			
			AdditionalSoftware sw = new AdditionalSoftware();
			sw.setAdditionalSoftwareId(dto.getId());
			
			
			sw.setCertifiedProductId(dto.getCertifiedProductId());
			
			CertifiedProductDTO cp = certifiedProductManager.getById(dto.getCertifiedProductId());
			sw.setCertifiedProductCHPLId(cp.getChplProductNumber());
			
			
			sw.setCertifiedProductSelf(dto.getCertifiedProductSelfId());
			
			CertifiedProductDTO selfCp = certifiedProductManager.getById(dto.getCertifiedProductSelfId());
			sw.setCertifiedProductSelfCHPLId(selfCp.getChplProductNumber());
			

			sw.setJustification(dto.getJustification());
			sw.setName(dto.getName());
			sw.setVersion(dto.getVersion());
			
			
			additionalSoftware.add(sw);
		}
		
		return additionalSoftware;
	}
	
}
