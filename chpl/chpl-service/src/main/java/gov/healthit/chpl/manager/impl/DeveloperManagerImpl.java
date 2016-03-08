package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.DeveloperEntity;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.DeveloperManager;

@Service
public class DeveloperManagerImpl implements DeveloperManager {

	@Autowired
	DeveloperDAO developerDao;
	
	@Autowired ProductDAO productDao;
	@Autowired CertificationBodyManager acbManager;

	@Autowired
	ActivityManager activityManager;
	
	@Override
	@Transactional(readOnly = true)
	public List<DeveloperDTO> getAll() {
		List<DeveloperDTO> allDevelopers = developerDao.findAll();
		List<CertificationBodyDTO> availableAcbs = acbManager.getAllForUser(false);
		if(availableAcbs != null && availableAcbs.size() == 1) {
			//if someone is a member of multiple acbs, they will not see the transparency
			CertificationBodyDTO acb = availableAcbs.get(0);
			for(DeveloperDTO developer : allDevelopers) {
				DeveloperACBMapDTO map = developerDao.getTransparencyMapping(developer.getId(), acb.getId());
				if(map == null) {
					developer.setTransparencyAttestation(null);
				} else {
					developer.setTransparencyAttestation(map.getTransparencyAttestation());
				}
			}
		}
		return allDevelopers;
	}

	@Override
	@Transactional(readOnly = true)
	public DeveloperDTO getById(Long id) throws EntityRetrievalException {
		DeveloperDTO developer = developerDao.getById(id);
		List<CertificationBodyDTO> availableAcbs = acbManager.getAllForUser(false);
		if(availableAcbs != null && availableAcbs.size() == 1) {
			//if someone is a member of multiple acbs, they will not see the transparency
			CertificationBodyDTO acb = availableAcbs.get(0);
			DeveloperACBMapDTO map = developerDao.getTransparencyMapping(developer.getId(), acb.getId());
			if(map == null) {
				developer.setTransparencyAttestation(null);
			} else {
				developer.setTransparencyAttestation(map.getTransparencyAttestation());
			}
		}
		return developer;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	@Transactional(readOnly = false)
	public DeveloperDTO update(DeveloperDTO developer) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		DeveloperDTO before = getById(developer.getId());
		DeveloperEntity result = developerDao.update(developer);
		
		//chplAdmin cannot update the transparency but any other role
		//allowed in this method can
		boolean isChplAdmin = false;
		Set<GrantedPermission> permissions = Util.getCurrentUser().getPermissions();
		for(GrantedPermission permission : permissions) {
			if(permission.getAuthority().equals("ROLE_ADMIN")) {
				isChplAdmin = true;
			}
		}
		
		if(!isChplAdmin) {
			List<CertificationBodyDTO> availableAcbs = acbManager.getAllForUser(false);
			if(availableAcbs != null && availableAcbs.size() > 0) {
				for(CertificationBodyDTO acb : availableAcbs) {
					DeveloperACBMapDTO existingMap = developerDao.getTransparencyMapping(developer.getId(), acb.getId());
					if(existingMap == null) {
						DeveloperACBMapDTO developerMappingToUpdate = new DeveloperACBMapDTO();
						developerMappingToUpdate.setAcbId(acb.getId());
						developerMappingToUpdate.setDeveloperId(before.getId());
						developerMappingToUpdate.setTransparencyAttestation(developer.getTransparencyAttestation());
						developerDao.createTransparencyMapping(developerMappingToUpdate);
					} else {
						existingMap.setTransparencyAttestation(developer.getTransparencyAttestation());
						developerDao.updateTransparencyMapping(existingMap);
					}
				}
			}
		}
		DeveloperDTO after = new DeveloperDTO(result);
		
		if(!isChplAdmin) {
			after.setTransparencyAttestation(developer.getTransparencyAttestation());
		}
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER, after.getId(), "Developer "+developer.getName()+" was updated.", before, after);
		
		return after;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	@Transactional(readOnly = false)
	public DeveloperDTO create(DeveloperDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		DeveloperDTO created = developerDao.create(dto);
		
		List<CertificationBodyDTO> availableAcbs = acbManager.getAllForUser(false);
		if(availableAcbs != null && availableAcbs.size() > 0) {
			for(CertificationBodyDTO acb : availableAcbs) {
				DeveloperACBMapDTO developerMappingToCreate = new DeveloperACBMapDTO();
				developerMappingToCreate.setAcbId(acb.getId());
				developerMappingToCreate.setDeveloperId(created.getId());
				developerMappingToCreate.setTransparencyAttestation(dto.getTransparencyAttestation());
				developerDao.createTransparencyMapping(developerMappingToCreate);
			}
		}
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER, created.getId(), "Developer "+created.getName()+" has been created.", null, created);
		return created;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	@Transactional(readOnly = false)
	public void delete(DeveloperDTO dto) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		DeveloperDTO toDelete = developerDao.getById(dto.getId());
		List<CertificationBodyDTO> availableAcbs = acbManager.getAllForUser(false);
		if(availableAcbs != null && availableAcbs.size() > 0) {
			for(CertificationBodyDTO acb : availableAcbs) {
				developerDao.deleteTransparencyMapping(dto.getId(), acb.getId());
			}
		}
		developerDao.delete(dto.getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER, toDelete.getId(), "Developer "+toDelete.getName()+" has been deleted.", toDelete, null);
		
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	@Transactional(readOnly = false)
	public void delete(Long developerId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		DeveloperDTO toDelete = developerDao.getById(developerId);
		List<CertificationBodyDTO> availableAcbs = acbManager.getAllForUser(false);
		if(availableAcbs != null && availableAcbs.size() > 0) {
			for(CertificationBodyDTO acb : availableAcbs) {
				developerDao.deleteTransparencyMapping(developerId, acb.getId());
			}
		}
		developerDao.delete(developerId);
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER, toDelete.getId(), "Developer "+toDelete.getName()+" has been deleted.", toDelete, null);
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = false)
	public DeveloperDTO merge(List<Long> developerIdsToMerge, DeveloperDTO developerToCreate) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		List<DeveloperDTO> beforeDevelopers = new ArrayList<DeveloperDTO>();
		for(Long developerId : developerIdsToMerge) {
			beforeDevelopers.add(developerDao.getById(developerId));
		}
		
		DeveloperDTO createdDeveloper = developerDao.create(developerToCreate);
		// - search for any products assigned to the list of developers passed in
		List<ProductDTO> developerProducts = productDao.getByDevelopers(developerIdsToMerge);
		// - reassign those products to the new developer
		for(ProductDTO product : developerProducts) {
			product.setDeveloperId(createdDeveloper.getId());
			productDao.update(product);
		}
		// - mark the passed in developers as deleted
		for(Long developerId : developerIdsToMerge) {
			List<CertificationBodyDTO> availableAcbs = acbManager.getAllForUser(false);
			if(availableAcbs != null && availableAcbs.size() > 0) {
				for(CertificationBodyDTO acb : availableAcbs) {
					developerDao.deleteTransparencyMapping(developerId, acb.getId());
				}
			}
			developerDao.delete(developerId);
		}
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER, createdDeveloper.getId(), "Merged "+ developerIdsToMerge.size() + " developers into new developer '" + createdDeveloper.getName() + "'.", beforeDevelopers, createdDeveloper);
		
		return createdDeveloper;
	}
}
