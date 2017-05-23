package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.ClearBasicSearch;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.DecertifiedDeveloperResult;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DecertifiedDeveloperDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.entity.DeveloperStatusType;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.web.controller.results.DecertifiedDeveloperResults;

@Service
public class DeveloperManagerImpl implements DeveloperManager {
	private static final Logger logger = LogManager.getLogger(DeveloperManagerImpl.class);
	
	@Autowired private SendMailUtil sendMailService;
	@Autowired private Environment env;
	
	@Autowired DeveloperDAO developerDao;	
	@Autowired ProductManager productManager;
	@Autowired CertificationBodyManager acbManager;
	@Autowired CertificationBodyDAO certificationBodyDao;

	@Autowired
	ActivityManager activityManager;
	
	@Override
	@Transactional(readOnly = true)
	@Cacheable(CacheNames.ALL_DEVELOPERS)
	public List<DeveloperDTO> getAll() {
		List<DeveloperDTO> allDevelopers = developerDao.findAll();
		List<DeveloperDTO> allDevelopersWithTransparencies = addTransparencyMappings(allDevelopers);
		return allDevelopersWithTransparencies;
	}

	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	@Cacheable(CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED)
	public List<DeveloperDTO> getAllIncludingDeleted() {
		List<DeveloperDTO> allDevelopers = developerDao.findAllIncludingDeleted();
		List<DeveloperDTO> allDevelopersWithTransparencies = addTransparencyMappings(allDevelopers);
		return allDevelopersWithTransparencies;
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
	@CacheEvict(value = {CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.DEVELOPER_NAMES, 
			CacheNames.SEARCH, CacheNames.COUNT_MULTI_FILTER_SEARCH_RESULTS, CacheNames.GET_DECERTIFIED_DEVELOPERS}, allEntries=true)
	@ClearBasicSearch
	public DeveloperDTO update(DeveloperDTO developer) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		DeveloperDTO beforeDev = getById(developer.getId());
		DeveloperStatusEventDTO newDevStatus = developer.getStatus();
		DeveloperStatusEventDTO currDevStatus = beforeDev.getStatus();
		if(currDevStatus == null || currDevStatus.getStatus() == null) {
			String msg = "The developer " + beforeDev.getName()+ " cannot be updated since it's current status cannot be determined.";
			logger.error(msg);
			throw new EntityCreationException(msg);
		}
		
		//if the before status is not Active and the user is not ROLE_ADMIN
		//then nothing can be changed
		if(!currDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString()) && 
				!Util.isUserRoleAdmin()) {
			logger.error("User " + Util.getUsername() + " does not have ROLE_ADMIN and cannot change developer " + beforeDev.getName() + " because its status is not Active.");
			throw new EntityCreationException("User without ROLE_ADMIN is not authorized to update an inactive developer.");
		} 
		
		//if the status history has been modified, the user must be role admin
		//except that an acb admin can change to UnderCertificationBanByOnc 
		//triggered by listing status update
		boolean devStatusHistoryUpdated = isStatusHistoryUpdated(beforeDev, developer);
		if(devStatusHistoryUpdated && newDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.UnderCertificationBanByOnc.toString()) && 
			!(Util.isUserRoleAdmin() || Util.isUserRoleAcbAdmin())) {
			logger.error("User " + Util.getUsername() + " does not have ROLE_ADMIN or ROLE_ACB_ADMIN but may "
					+ "have tried to change status history for the developer " + beforeDev.getName() + " to include " + DeveloperStatusType.UnderCertificationBanByOnc.toString());
			throw new EntityCreationException("User cannot change developer status to " + DeveloperStatusType.UnderCertificationBanByOnc.toString() + " without ROLE_ADMIN or ROLE_ACB_ADMIN.");
		} else if(devStatusHistoryUpdated && !newDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.UnderCertificationBanByOnc.toString()) && 
				!Util.isUserRoleAdmin()) {
			logger.error("User " + Util.getUsername() + " does not have ROLE_ADMIN but may have tried to change history for the developer " + beforeDev.getName());
			throw new EntityCreationException("User without ROLE_ADMIN is not authorized to change developer status history.");
		}
				
		//determine if the status has been changed
		//in most cases only allowed by ROLE_ADMIN but ROLE_ACB_ADMIN
		//can change it to UnderCertificationBanByOnc
		boolean currentStatusChanged = !currDevStatus.getStatus().getStatusName().equals(newDevStatus.getStatus().getStatusName());
		if(currentStatusChanged && newDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.UnderCertificationBanByOnc.toString()) && 
				!(Util.isUserRoleAdmin() || Util.isUserRoleAcbAdmin())) {
				logger.error("User " + Util.getUsername() + " does not have ROLE_ADMIN or ROLE_ACB_ADMIN but may "
						+ "have tried to change status for the developer " + beforeDev.getName() + " to include " + DeveloperStatusType.UnderCertificationBanByOnc.toString());
				throw new EntityCreationException("User cannot change developer status to " + DeveloperStatusType.UnderCertificationBanByOnc.toString() + " without ROLE_ADMIN or ROLE_ACB_ADMIN.");
		} else if(currentStatusChanged &&  !newDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.UnderCertificationBanByOnc.toString()) && 
				!Util.isUserRoleAdmin()) {
			logger.error("User " + Util.getUsername() + " does not have ROLE_ADMIN and cannot change developer " + beforeDev.getName() + " status from " + currDevStatus.getStatus().getStatusName() + " to " + currDevStatus.getStatus().getStatusName());
			throw new EntityCreationException("User without ROLE_ADMIN is not authorized to change developer status.");
		} else if(!currDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString()) && 
				  !newDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())) {
			//if the developer is not active and not going to be active
			//only its status can be updated
			developerDao.updateStatus(newDevStatus);
			return getById(developer.getId());
		}
		
		//if either the before or updated statuses are active and the user is ROLE_ADMIN
		//OR if before status is active and user is not ROLE_ADMIN - proceed
		if( ((currDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString()) 
				|| newDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())) 
			  && Util.isUserRoleAdmin()) 
			||
			(currDevStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString()) && 
					!Util.isUserRoleAdmin())) {
			
			developerDao.update(developer);
			List<CertificationBodyDTO> availableAcbs = acbManager.getAllForUser(false);
			if(availableAcbs != null && availableAcbs.size() > 0) {
				for(CertificationBodyDTO acb : availableAcbs) {
					DeveloperACBMapDTO existingMap = developerDao.getTransparencyMapping(developer.getId(), acb.getId());
					if(existingMap == null) {
						DeveloperACBMapDTO developerMappingToCreate = new DeveloperACBMapDTO();
						developerMappingToCreate.setAcbId(acb.getId());
						developerMappingToCreate.setDeveloperId(beforeDev.getId());
						for(DeveloperACBMapDTO attMap : developer.getTransparencyAttestationMappings()) {
							if(attMap.getAcbName().equals(acb.getName())) {
								developerMappingToCreate.setTransparencyAttestation(attMap.getTransparencyAttestation());;
								developerDao.createTransparencyMapping(developerMappingToCreate);
							}
						}
					} else {
						for(DeveloperACBMapDTO attMap : developer.getTransparencyAttestationMappings()) {
							if(attMap.getAcbName().equals(acb.getName())) {
								existingMap.setTransparencyAttestation(attMap.getTransparencyAttestation());
								developerDao.updateTransparencyMapping(existingMap);
							}
						}
					}
				}
			}
		}
		
		DeveloperDTO after = getById(developer.getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_DEVELOPER, after.getId(), "Developer "+developer.getName()+" was updated.", beforeDev, after);
		if(!Boolean.parseBoolean(env.getProperty("disableSuspiciousActivityEmails"))){
			checkSuspiciousActivity(beforeDev, after);
		}
		return after;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	@Transactional(readOnly = false)
	@CacheEvict(value = {CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.DEVELOPER_NAMES, 
			CacheNames.SEARCH, CacheNames.COUNT_MULTI_FILTER_SEARCH_RESULTS}, allEntries=true)
	@ClearBasicSearch
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
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = false)
	@CacheEvict(value = {CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.DEVELOPER_NAMES, 
			CacheNames.SEARCH, CacheNames.COUNT_MULTI_FILTER_SEARCH_RESULTS, CacheNames.GET_DECERTIFIED_DEVELOPERS}, allEntries=true)
	@ClearBasicSearch
	public DeveloperDTO merge(List<Long> developerIdsToMerge, DeveloperDTO developerToCreate) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		List<DeveloperDTO> beforeDevelopers = new ArrayList<DeveloperDTO>();
		for(Long developerId : developerIdsToMerge) {
			beforeDevelopers.add(developerDao.getById(developerId));
		}
		
		//check for any non-active developers and throw an error if any are found
		for(DeveloperDTO beforeDeveloper : beforeDevelopers) {
			DeveloperStatusEventDTO currDeveloperStatus = beforeDeveloper.getStatus();
			if(currDeveloperStatus == null || currDeveloperStatus.getStatus() == null) {
				String msg = "Cannot merge developer " + beforeDeveloper.getName() + " because their current status cannot be determined.";
				logger.error(msg);
				throw new EntityCreationException(msg);
			} else if(!currDeveloperStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())) {
				String msg = "Cannot merge developer " + beforeDeveloper.getName() + " with a status of " + currDeveloperStatus.getStatus().getStatusName();
				logger.error(msg);
				throw new EntityCreationException(msg);
			}
		}
		
		//check if the transparency attestation for each developer is conflicting
		List<CertificationBodyDTO> allAcbs = acbManager.getAll(false);
		for(CertificationBodyDTO acb : allAcbs) {
			AttestationType transparencyAttestation = null;
			for(DeveloperDTO dev : beforeDevelopers) {
				DeveloperACBMapDTO taMap = developerDao.getTransparencyMapping(dev.getId(), acb.getId());
				if(taMap != null && !StringUtils.isEmpty(taMap.getTransparencyAttestation())) {
					AttestationType currAtt = AttestationType.getValue(taMap.getTransparencyAttestation());
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
		//search for any products assigned to the list of developers passed in
		List<ProductDTO> developerProducts = productManager.getByDevelopers(developerIdsToMerge);
		for(ProductDTO product : developerProducts) {
			//add an item to the ownership history of each product
			ProductOwnerDTO historyToAdd= new ProductOwnerDTO();
			historyToAdd.setProductId(product.getId());
			DeveloperDTO prevOwner = new DeveloperDTO();
			prevOwner.setId(product.getDeveloperId());
			historyToAdd.setDeveloper(prevOwner);
			historyToAdd.setTransferDate(System.currentTimeMillis());
			product.getOwnerHistory().add(historyToAdd);
			//reassign those products to the new developer
			product.setDeveloperId(createdDeveloper.getId());
			productManager.update(product, false);
			
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
	
	@Transactional(readOnly = true)
	@Cacheable(CacheNames.GET_DECERTIFIED_DEVELOPERS)
	public DecertifiedDeveloperResults getDecertifiedDevelopers() throws EntityRetrievalException{
		DecertifiedDeveloperResults ddr = new DecertifiedDeveloperResults();
		List<DecertifiedDeveloperDTO> dtoList = new ArrayList<DecertifiedDeveloperDTO>();
		List<DecertifiedDeveloperResult> decertifiedDeveloperResults = new ArrayList<DecertifiedDeveloperResult>();
		
		dtoList = developerDao.getDecertifiedDevelopers();
		
		for(DecertifiedDeveloperDTO dto : dtoList){
			List<CertificationBody> certifyingBody = new ArrayList<CertificationBody>();
			for(Long oncacbId : dto.getAcbIdList()){
				CertificationBody cb = new CertificationBody(certificationBodyDao.getById(oncacbId));
				certifyingBody.add(cb);
			}
			
			DecertifiedDeveloperResult decertifiedDeveloper = new DecertifiedDeveloperResult(developerDao.getById(dto.getDeveloperId()), certifyingBody, dto.getDecertificationDate(), dto.getNumMeaningfulUse());
			decertifiedDeveloperResults.add(decertifiedDeveloper);
		}
		
		ddr.setDecertifiedDeveloperResults(decertifiedDeveloperResults);
		return ddr;
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
		
		sendMsg = sendMsg || isStatusHistoryUpdated(original, changed);
		
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
	
	private boolean isStatusHistoryUpdated(DeveloperDTO original, DeveloperDTO changed) {
		boolean hasChanged = false;
		if( (original.getStatusEvents() != null && changed.getStatusEvents() == null) || 
				(original.getStatusEvents() == null && changed.getStatusEvents() != null) || 
				(original.getStatusEvents().size() != changed.getStatusEvents().size())) {
			hasChanged = true;
			} else {
				//neither status history is null and they have the same size history arrays
				//so now check for any differences in the values of each
				for(DeveloperStatusEventDTO origStatusHistory : original.getStatusEvents()) {
					boolean foundMatchInChanged = false;
					for(DeveloperStatusEventDTO changedStatusHistory : changed.getStatusEvents()) {
						if(origStatusHistory.getStatus().getId().longValue() == changedStatusHistory.getStatus().getId().longValue() && 
							origStatusHistory.getStatusDate().getTime() == changedStatusHistory.getStatusDate().getTime()) {
							foundMatchInChanged = true;
						}
					}
					hasChanged = hasChanged || !foundMatchInChanged;
				}
			}
		return hasChanged;
	}
	
	private List<DeveloperDTO> addTransparencyMappings(List<DeveloperDTO> developers) {
		List<DeveloperACBMapDTO> transparencyMaps = developerDao.getAllTransparencyMappings();
        Map<Long,DeveloperDTO> mappedDevelopers = new HashMap<Long,DeveloperDTO>();
        for(DeveloperDTO dev : developers) {
            mappedDevelopers.put(dev.getId(), dev);
        }
        for(DeveloperACBMapDTO map : transparencyMaps) {
            if (map.getAcbId() != null) {
                mappedDevelopers.get(map.getDeveloperId()).getTransparencyAttestationMappings().add(map);
            }
        }
        List<DeveloperDTO> ret = new ArrayList<DeveloperDTO>();
        for (DeveloperDTO dev : mappedDevelopers.values()) {
            ret.add(dev);
        }
        return ret;
	}
}
