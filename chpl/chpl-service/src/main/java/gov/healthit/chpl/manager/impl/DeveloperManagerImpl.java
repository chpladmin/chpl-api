package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.TransparencyAttestationMap;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.entity.DeveloperEntity;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.DeveloperManager;

@Service
public class DeveloperManagerImpl implements DeveloperManager {
	private static final Logger logger = LogManager.getLogger(DeveloperManagerImpl.class);
	
	@Autowired private SendMailUtil sendMailService;
	@Autowired private Environment env;
	
	@Autowired DeveloperDAO developerDao;	
	@Autowired ProductDAO productDao;
	@Autowired CertificationBodyManager acbManager;

	@Autowired
	ActivityManager activityManager;
	
	@Override
	@Transactional(readOnly = true)
	public List<DeveloperDTO> getAll() {
		List<DeveloperDTO> allDevelopers = developerDao.findAll();
		List<CertificationBodyDTO> availableAcbs = acbManager.getAllForUser(false);
		if(availableAcbs == null || availableAcbs.size() == 0) {
			availableAcbs = acbManager.getAll(true);
		}
		//someone will see either the transparencies that apply to the ACBs to which they have access
		//or they will see the transparencies for all ACBs if they are an admin or not logged in
		for(CertificationBodyDTO acb : availableAcbs) {
			for(DeveloperDTO developer : allDevelopers) {
				DeveloperACBMapDTO map = developerDao.getTransparencyMapping(developer.getId(), acb.getId());
				if(map == null) {
					DeveloperACBMapDTO mapToAdd = new DeveloperACBMapDTO();
					mapToAdd.setAcbId(acb.getId());
					mapToAdd.setAcbName(acb.getName());
					mapToAdd.setDeveloperId(developer.getId());
					mapToAdd.setTransparencyAttestation(null);
					developer.getTransparencyAttestationMappings().add(mapToAdd);
				} else {
					developer.getTransparencyAttestationMappings().add(map);
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
		if(availableAcbs == null || availableAcbs.size() == 0) {
			availableAcbs = acbManager.getAll(true);
		}
		//someone will see either the transparencies that apply to the ACBs to which they have access
		//or they will see the transparencies for all ACBs if they are an admin or not logged in
		for(CertificationBodyDTO acb : availableAcbs) {
			DeveloperACBMapDTO map = developerDao.getTransparencyMapping(developer.getId(), acb.getId());
			if(map == null) {
				DeveloperACBMapDTO mapToAdd = new DeveloperACBMapDTO();
				mapToAdd.setAcbId(acb.getId());
				mapToAdd.setAcbName(acb.getName());
				mapToAdd.setDeveloperId(developer.getId());
				mapToAdd.setTransparencyAttestation(null);
				developer.getTransparencyAttestationMappings().add(mapToAdd);
			} else {
				map.setAcbName(acb.getName());
				developer.getTransparencyAttestationMappings().add(map);
			}
		}
		return developer;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	@Transactional(readOnly = false)
	public DeveloperDTO update(DeveloperDTO developer) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		DeveloperDTO before = getById(developer.getId());
		DeveloperEntity result = developerDao.update(developer);
		
		List<CertificationBodyDTO> availableAcbs = acbManager.getAllForUser(false);
		if(availableAcbs != null && availableAcbs.size() > 0) {
			for(CertificationBodyDTO acb : availableAcbs) {
				DeveloperACBMapDTO existingMap = developerDao.getTransparencyMapping(developer.getId(), acb.getId());
				if(existingMap == null) {
					DeveloperACBMapDTO developerMappingToCreate = new DeveloperACBMapDTO();
					developerMappingToCreate.setAcbId(acb.getId());
					developerMappingToCreate.setDeveloperId(before.getId());
					for(DeveloperACBMapDTO attMap : developer.getTransparencyAttestationMappings()) {
						if(attMap.getAcbId().longValue() == acb.getId().longValue()) {
							developerMappingToCreate.setTransparencyAttestation(attMap.getTransparencyAttestation());;
							developerDao.createTransparencyMapping(developerMappingToCreate);
						}
					}
				} else {
					for(DeveloperACBMapDTO attMap : developer.getTransparencyAttestationMappings()) {
						if(attMap.getAcbId().longValue() == acb.getId().longValue()) {
							existingMap.setTransparencyAttestation(attMap.getTransparencyAttestation());
							developerDao.updateTransparencyMapping(existingMap);
						}
					}
				}
			}
		}
		
		DeveloperDTO after = getById(result.getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER, after.getId(), "Developer "+developer.getName()+" was updated.", before, after);
		checkSuspiciousActivity(before, after);
		return after;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	@Transactional(readOnly = false)
	public DeveloperDTO create(DeveloperDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		DeveloperDTO created = developerDao.create(dto);
		
		List<CertificationBodyDTO> availableAcbs = acbManager.getAllForUser(false);
		if(availableAcbs != null && availableAcbs.size() > 0) {
			for(CertificationBodyDTO acb : availableAcbs) {
				for(DeveloperACBMapDTO attMap : dto.getTransparencyAttestationMappings()) {
					if(acb.getId().longValue() == attMap.getAcbId().longValue() && 
							!StringUtils.isEmpty(attMap.getTransparencyAttestation())) {
						attMap.setDeveloperId(created.getId());
						developerDao.createTransparencyMapping(attMap);
					}
				}
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
		
		
		//check if the transparency attestation for each developer is conflicting
		List<CertificationBodyDTO> allAcbs = acbManager.getAll(false);
		for(CertificationBodyDTO acb : allAcbs) {
			AttestationType transparencyAttestation = null;
			for(DeveloperDTO dev : beforeDevelopers) {
				DeveloperACBMapDTO taMap = developerDao.getTransparencyMapping(dev.getId(), acb.getId());
				if(taMap != null && !StringUtils.isEmpty(taMap.getTransparencyAttestation())) {
					AttestationType currAtt = AttestationType.valueOf(taMap.getTransparencyAttestation());
					if(transparencyAttestation == null) {
						transparencyAttestation = currAtt;
					} else if(currAtt != transparencyAttestation) {
						throw new EntityCreationException("Cannot complete merge because " + acb.getName() + " has a conflicting transparency attestation for these developers.");
					}
				}
			}
			
			if(transparencyAttestation != null) {
				DeveloperACBMapDTO devMap = new DeveloperACBMapDTO();
				devMap.setAcbId(acb.getId());
				devMap.setAcbName(acb.getName());
				devMap.setTransparencyAttestation(transparencyAttestation.name());
				developerToCreate.getTransparencyAttestationMappings().add(devMap);
			}	
		}
		
		DeveloperDTO createdDeveloper = create(developerToCreate);
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
	
	@Override
	public void checkSuspiciousActivity(DeveloperDTO original, DeveloperDTO changed) {
		String subject = "CHPL Questionable Activity";
		String htmlMessage = "<p>Activity was detected on developer " + original.getName() + ".</p>" 
				+ "<p>To view the details of this activity go to: " + 
				env.getProperty("chplUrlBegin") + "/#/admin/reports</p>";
		
		boolean sendMsg = false;
		
		if( (original.getName() != null && changed.getName() == null) ||
			(original.getName() == null && changed.getName() != null) ||
			!original.getName().equals(changed.getName()) ) {
			sendMsg = true;
		}
		
		if(sendMsg) {
			String emailAddr = env.getProperty("questionableActivityEmail");
			String[] emailAddrs = emailAddr.split(";");
			try {
				sendMailService.sendEmail(emailAddrs, subject, htmlMessage);
			} catch(MessagingException me) {
				logger.error("Could not send questionable activity email", me);
			}
		}	
	}
}
