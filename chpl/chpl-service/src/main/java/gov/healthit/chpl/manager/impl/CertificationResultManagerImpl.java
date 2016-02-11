package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.manager.CertificationResultManager;

@Service
public class CertificationResultManagerImpl implements
		CertificationResultManager {
	
	@Autowired private CertificationResultDAO certResultDAO;
	
	@Override
	public List<CertificationResultDTO> getAll() {
		return certResultDAO.findAll();
	}

	@Override
	public CertificationResultDTO getById(Long resultId) throws EntityRetrievalException {
		return certResultDAO.getById(resultId);
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public CertificationResultDTO create(Long acbId, CertificationResultDTO toCreate) throws EntityCreationException {
		CertificationResultDTO created = certResultDAO.create(toCreate);
		
		for (CertificationResultAdditionalSoftwareDTO asMapping : toCreate.getAdditionalSoftware()){
			CertificationResultAdditionalSoftwareDTO mappingToCreate = new CertificationResultAdditionalSoftwareDTO();
			mappingToCreate.setCertificationResultId(created.getId());
			mappingToCreate.setCertifiedProductId(asMapping.getCertifiedProductId());
			mappingToCreate.setName(asMapping.getName());
			mappingToCreate.setVersion(asMapping.getVersion());
			mappingToCreate.setJustification(asMapping.getJustification());
			CertificationResultAdditionalSoftwareDTO createdMapping = certResultDAO.addAdditionalSoftwareMapping(mappingToCreate);
			created.getAdditionalSoftware().add(createdMapping);
		}
		return created;
	}

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	public CertificationResultDTO update(Long acbId, CertificationResultDTO toUpdate) throws EntityRetrievalException, EntityCreationException {
		CertificationResultDTO updated = certResultDAO.update(toUpdate);
		List<CertificationResultAdditionalSoftwareDTO> existingMappings = certResultDAO.getAdditionalSoftwareForCertificationResult(toUpdate.getId());
		
		List<CertificationResultAdditionalSoftwareDTO> mappingsToAdd = new ArrayList<CertificationResultAdditionalSoftwareDTO>();
		List<CertificationResultAdditionalSoftwareDTO> mappingsToRemove = new ArrayList<CertificationResultAdditionalSoftwareDTO>();

		for (CertificationResultAdditionalSoftwareDTO toUpdateMapping : toUpdate.getAdditionalSoftware()){
			if(toUpdateMapping.getId() == null) {
				mappingsToAdd.add(toUpdateMapping);
			} 
		}
		
		for(CertificationResultAdditionalSoftwareDTO currMapping : existingMappings) {
			boolean isInUpdate = false;
			for (CertificationResultAdditionalSoftwareDTO toUpdateMapping : toUpdate.getAdditionalSoftware()){
				if(toUpdateMapping.getId() != null && 
						toUpdateMapping.getId().longValue() == currMapping.getId().longValue()) {
					isInUpdate = true;
				}
			}
			if(!isInUpdate) {
				mappingsToRemove.add(currMapping);
			}
		}
			
		for(CertificationResultAdditionalSoftwareDTO toAdd : mappingsToAdd) {
			certResultDAO.addAdditionalSoftwareMapping(toAdd);
		}
		for(CertificationResultAdditionalSoftwareDTO toRemove : mappingsToRemove) {
			certResultDAO.deleteAdditionalSoftwareMapping(toRemove.getId());
		}
		
		updated.setAdditionalSoftware(certResultDAO.getAdditionalSoftwareForCertificationResult(updated.getId()));
		return updated;
	}

	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	@Override
	public void delete(Long acbId, Long toDeleteId) {		
		//delete additional software mappings
		List<CertificationResultAdditionalSoftwareDTO> softwareMappings = 
				certResultDAO.getAdditionalSoftwareForCertificationResult(toDeleteId);
		for(CertificationResultAdditionalSoftwareDTO mapping : softwareMappings) {
			certResultDAO.deleteAdditionalSoftwareMapping(mapping.getId());
		}
		
		//delete the cert result
		certResultDAO.delete(toDeleteId);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Transactional(readOnly = false)
	@Override
	public void deleteByCertifiedProductId(Long acbId, Long certifiedProductId) {
		
		//delete additional software for all these products that are getting deleted
		List<CertificationResultDTO> resultsToDelete = certResultDAO.findByCertifiedProductId(certifiedProductId);
		for(CertificationResultDTO resultToDelete : resultsToDelete) {
			List<CertificationResultAdditionalSoftwareDTO> softwareMappings = 
					certResultDAO.getAdditionalSoftwareForCertificationResult(resultToDelete.getId());
			for(CertificationResultAdditionalSoftwareDTO mapping : softwareMappings) {
				certResultDAO.deleteAdditionalSoftwareMapping(mapping.getId());
			}
		}
		
		//delete the cert result
		certResultDAO.deleteByCertifiedProductId(certifiedProductId);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Override
	public CertificationResultAdditionalSoftwareDTO createAdditionalSoftwareMapping(Long acbId, CertificationResultAdditionalSoftwareDTO dto) throws EntityCreationException {
		return certResultDAO.addAdditionalSoftwareMapping(dto);
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Override
	public CertificationResultAdditionalSoftwareDTO updateAdditionalSoftwareMapping(Long acbId, 
			CertificationResultAdditionalSoftwareDTO dto) {
		return certResultDAO.updateAdditionalSoftwareMapping(dto);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Override
	public void deleteAdditionalSoftwareMapping(Long acbId, Long id) {
		certResultDAO.deleteAdditionalSoftwareMapping(id);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "( (hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN'))"
			+ "  and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)"
			+ ")")
	@Override
	@Transactional(readOnly = false)
	public void deleteAdditionalSoftwareForCertificationResult(Long acbId, Long certificationResultId) {
		List<CertificationResultAdditionalSoftwareDTO> softwareMappings = certResultDAO.getAdditionalSoftwareForCertificationResult(certificationResultId);
		for(CertificationResultAdditionalSoftwareDTO softwareMapping : softwareMappings) {
			certResultDAO.deleteAdditionalSoftwareMapping(softwareMapping.getId());
		}
	}
	
	@Override
	public List<CertificationResultAdditionalSoftwareDTO> getAdditionalSoftwareMappingsForCertificationResult(Long certificationResultId){
		return certResultDAO.getAdditionalSoftwareForCertificationResult(certificationResultId);
	}
}