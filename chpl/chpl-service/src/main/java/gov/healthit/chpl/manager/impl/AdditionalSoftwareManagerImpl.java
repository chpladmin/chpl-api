package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dao.AdditionalSoftwareDAO;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.AdditionalSoftware;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CQMResultAdditionalSoftwareMapDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareMapDTO;
import gov.healthit.chpl.manager.AdditionalSoftwareManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AdditionalSoftwareManagerImpl implements AdditionalSoftwareManager {
	
	@Autowired
	private CertificationResultDAO certificationResultDAO;
	
	@Autowired
	private CQMResultDAO cqmResultDAO;
	
	@Autowired
	private AdditionalSoftwareDAO additionalSoftwareDAO;
	
	
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
	public CQMResultAdditionalSoftwareMapDTO addAdditionalSoftwareCQMResultMapping(Long additionalSoftwareId, Long cqmResultId) throws EntityCreationException{
		
		CQMResultAdditionalSoftwareMapDTO dto = new CQMResultAdditionalSoftwareMapDTO();
		dto.setAdditionalSoftwareId(additionalSoftwareId);
		dto.setCqmResultId(cqmResultId);
		
		// "CQM"),
		CQMResultAdditionalSoftwareMapDTO created = cqmResultDAO.createAdditionalSoftwareMapping(dto);
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
	public void deleteAdditionalSoftwareCQMResultMapping(Long additionalSoftwareId, Long cqmResultId){
		
		cqmResultDAO.deleteAdditionalSoftwareMapping(cqmResultId, additionalSoftwareId);
	}
	
	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public void associateAdditionalSoftwareCerifiedProductSelf(Long additionalSoftwareId, Long certifiedProductId) throws EntityRetrievalException{
		
		AdditionalSoftwareDTO dto = additionalSoftwareDAO.getById(additionalSoftwareId);
		dto.setCertifiedProductSelfId(certifiedProductId);
		additionalSoftwareDAO.update(dto);
		
	}
	
	@Override
	public List<AdditionalSoftware> getAdditionalSoftwareByCertificationResultId(Long id){
		
		List<AdditionalSoftwareDTO> dtos = additionalSoftwareDAO.findByCertificationResultId(id);
		List<AdditionalSoftware> additionalSoftware = new ArrayList<>();
		
		for (AdditionalSoftwareDTO dto : dtos){
			additionalSoftware.add(new AdditionalSoftware(dto));
		}
		
		return additionalSoftware;
	}
	
	@Override
	public List<AdditionalSoftware> getAdditionalSoftwareByCQMResultId(Long id){
		
		List<AdditionalSoftwareDTO> dtos = additionalSoftwareDAO.findByCQMResultId(id);
		List<AdditionalSoftware> additionalSoftware = new ArrayList<>();
		
		for (AdditionalSoftwareDTO dto : dtos){
			additionalSoftware.add(new AdditionalSoftware(dto));
		}
		
		return additionalSoftware;
	}
	
}
