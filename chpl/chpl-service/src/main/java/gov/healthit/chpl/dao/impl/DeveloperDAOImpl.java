package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.ContactDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.DeveloperTransparency;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DecertifiedDeveloperDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.entity.ContactEntity;
import gov.healthit.chpl.entity.developer.DeveloperACBMapEntity;
import gov.healthit.chpl.entity.developer.DeveloperACBTransparencyMapEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.entity.developer.DeveloperStatusEntity;
import gov.healthit.chpl.entity.developer.DeveloperStatusEventEntity;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.entity.developer.DeveloperTransparencyEntity;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.entity.search.CertifiedProductBasicSearchResultEntity;

@Repository("developerDAO")
public class DeveloperDAOImpl extends BaseDAOImpl implements DeveloperDAO {

	private static final Logger logger = LogManager.getLogger(DeveloperDAOImpl.class);
	private static final DeveloperStatusType DEFAULT_STATUS = DeveloperStatusType.Active;
	@Autowired AddressDAO addressDao;
	@Autowired ContactDAO contactDao;
	@Autowired DeveloperStatusDAO statusDao;

	@Override
	public DeveloperDTO create(DeveloperDTO dto) throws EntityCreationException, EntityRetrievalException {

		DeveloperEntity entity = null;
		try {
			if (dto.getId() != null){
				entity = this.getEntityById(dto.getId());
			}
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}

		if (entity != null) {
			throw new EntityCreationException("An entity with this ID already exists.");
		} else {
			entity = new DeveloperEntity();

			if(dto.getAddress() != null)
			{
				entity.setAddress(addressDao.mergeAddress(dto.getAddress()));
			}
			if(dto.getContact() != null) {
				if(dto.getContact().getId() != null) {
					ContactDTO contact = contactDao.getById(dto.getContact().getId());
					if(contact != null && contact.getId() != null) {
						entity.setContactId(contact.getId());
					}
				} else {
					ContactEntity contact = contactDao.create(dto.getContact());
					if(contact != null) {
						entity.setContactId(contact.getId());
					}				
				}
			}

			entity.setName(dto.getName());
			entity.setWebsite(dto.getWebsite());
			
			if(dto.getDeleted() != null) {
				entity.setDeleted(dto.getDeleted());
			} else {
				entity.setDeleted(false);
			}

			if(dto.getLastModifiedUser() != null) {
				entity.setLastModifiedUser(dto.getLastModifiedUser());
			} else {
				entity.setLastModifiedUser(Util.getCurrentUser().getId());
			}

			if(dto.getLastModifiedDate() != null) {
				entity.setLastModifiedDate(dto.getLastModifiedDate());
			} else {
				entity.setLastModifiedDate(new Date());
			}

			if(dto.getCreationDate() != null) {
				entity.setCreationDate(dto.getCreationDate());
			} else {
				entity.setCreationDate(new Date());
			}

			create(entity);
			
			//create a status history entry - will be Active by default
			if(dto.getStatusEvents() == null || dto.getStatusEvents().size() == 0) {
				DeveloperStatusEventEntity initialDeveloperStatus = new DeveloperStatusEventEntity();
				initialDeveloperStatus.setDeveloperId(entity.getId());
				DeveloperStatusEntity defaultStatus = getStatusByName(DEFAULT_STATUS.toString());
				initialDeveloperStatus.setDeveloperStatusId(defaultStatus.getId());
				initialDeveloperStatus.setStatusDate(entity.getCreationDate());
				initialDeveloperStatus.setDeleted(false);
				initialDeveloperStatus.setLastModifiedUser(entity.getLastModifiedUser());
				entityManager.persist(initialDeveloperStatus);
				entityManager.flush();
			} else {
				for(DeveloperStatusEventDTO providedDeveloperStatusEvent : dto.getStatusEvents()) {
					if(providedDeveloperStatusEvent.getStatus() != null && 
						!StringUtils.isEmpty(providedDeveloperStatusEvent.getStatus().getStatusName()) && 
						providedDeveloperStatusEvent.getStatusDate() != null) {
						DeveloperStatusEventEntity currDevStatus = new DeveloperStatusEventEntity();
						currDevStatus.setDeveloperId(entity.getId());
						DeveloperStatusEntity defaultStatus = getStatusByName(providedDeveloperStatusEvent.getStatus().getStatusName());
						if(defaultStatus != null) {
							currDevStatus.setDeveloperStatusId(defaultStatus.getId());
							currDevStatus.setStatusDate(providedDeveloperStatusEvent.getStatusDate());
							currDevStatus.setDeleted(false);
							currDevStatus.setLastModifiedUser(entity.getLastModifiedUser());
							entityManager.persist(currDevStatus);
							entityManager.flush();
						} else {
							String msg = "Could not find status with name " + providedDeveloperStatusEvent.getStatus().getStatusName() + "; cannot insert this status history entry for developer " + entity.getName();
							logger.error(msg);
							throw new EntityCreationException(msg);
						}
					} else {
						String msg = "Developer Status name and date must be provided but at least one was not found; cannot insert this status history for developer " + entity.getName();
						logger.error(msg);
						throw new EntityCreationException(msg);
					}
				}
			}
			
			Long id = entity.getId();
			entityManager.clear();
			return getById(id);
		}
	}

	@Override
	public DeveloperACBMapDTO createTransparencyMapping(DeveloperACBMapDTO dto) {
		DeveloperACBMapEntity mapping = new DeveloperACBMapEntity();
		mapping.getDeveloperId(dto.getDeveloperId());
		mapping.setCertificationBodyId(dto.getAcbId());
		mapping.setTransparencyAttestation(AttestationType.getValue(dto.getTransparencyAttestation()));
		mapping.setCreationDate(new Date());
		mapping.setDeleted(false);
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		return new DeveloperACBMapDTO(mapping);
	}
	
	@Override
	public DeveloperDTO update(DeveloperDTO dto) throws EntityRetrievalException, EntityCreationException {
		DeveloperEntity entity = this.getEntityById(dto.getId());
		if(entity == null) {
			throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
		}

		if(dto.getAddress() != null)
		{
			try {
				entity.setAddress(addressDao.mergeAddress(dto.getAddress()));
			} catch(EntityCreationException ex) {
				logger.error("Could not create new address in the database.", ex);
				entity.setAddress(null);
			}
		} else {
			entity.setAddress(null);
		}

		if(dto.getContact() != null) {
			if(dto.getContact().getId() == null) {
				//if there is not contact id then it must not exist - create it
				ContactEntity contact = contactDao.create(dto.getContact());
				if(contact != null && contact.getId() != null) {
					entity.setContactId(contact.getId());
					entity.setContact(contact);
				}
			} else {
				//if there is a contact id then set that on the object
				ContactEntity contact = contactDao.update(dto.getContact());
				if(contact != null) {
					entity.setContactId(dto.getContact().getId());
					entity.setContact(contact);
				}			
			}
		} else {
			//if there's no contact at all, set the id to null
			entity.setContactId(null);
			entity.setContact(null);
		}

		entity.setWebsite(dto.getWebsite());
		if(dto.getName() != null) {
			entity.setName(dto.getName());
		}

		if(dto.getDeleted() != null) {
			entity.setDeleted(dto.getDeleted());
		}

		if(dto.getLastModifiedUser() != null) {
			entity.setLastModifiedUser(dto.getLastModifiedUser());
		} else {
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
		}

		if(dto.getLastModifiedDate() != null) {
			entity.setLastModifiedDate(dto.getLastModifiedDate());
		} else {
			entity.setLastModifiedDate(new Date());
		}

		update(entity);
		
		//update the status history 
		//check to make sure at least 1 status event was passed in, we can't delete them all
		if(dto.getStatusEvents() == null || dto.getStatusEvents().size() == 0) {
			String msg = "Developer Status name and date must be provided but at least one was not found; cannot insert this status history for developer " + entity.getName();
			logger.error(msg);
			throw new EntityCreationException(msg);
		}
		
		//delete existing developer status history
		for(DeveloperStatusEventEntity existingDeveloperStatusEvent : entity.getStatusEvents()) {
			DeveloperStatusEventDTO newDeveloperStatusEvent = null;
			for(DeveloperStatusEventDTO providedDeveloperStatusEvent : dto.getStatusEvents()) {
				if(providedDeveloperStatusEvent.getId() != null && providedDeveloperStatusEvent.getId().longValue() == existingDeveloperStatusEvent.getId().longValue()) {
					newDeveloperStatusEvent = providedDeveloperStatusEvent;
				}
			}
			if(newDeveloperStatusEvent != null) {
				//update with new values
				if(newDeveloperStatusEvent.getStatus() != null && newDeveloperStatusEvent.getStatus().getStatusName() != null) {
					DeveloperStatusEntity newStatus = getStatusByName(newDeveloperStatusEvent.getStatus().getStatusName());
					if(newStatus != null && newStatus.getId() != null) {
						existingDeveloperStatusEvent.setDeveloperStatus(newStatus);
						existingDeveloperStatusEvent.setDeveloperStatusId(newStatus.getId());
					}
					existingDeveloperStatusEvent.setStatusDate(newDeveloperStatusEvent.getStatusDate());
				}
			} else {
				//delete
				existingDeveloperStatusEvent.setDeleted(true);
			}
			existingDeveloperStatusEvent.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.merge(existingDeveloperStatusEvent);
		}
		entityManager.flush();
		
		//add passed-in developer status history
		for(DeveloperStatusEventDTO providedDeveloperStatusEvent : dto.getStatusEvents()) {
			if(providedDeveloperStatusEvent.getId() == null && 
				providedDeveloperStatusEvent.getStatus() != null && 
				!StringUtils.isEmpty(providedDeveloperStatusEvent.getStatus().getStatusName()) && 
				providedDeveloperStatusEvent.getStatusDate() != null) {
				DeveloperStatusEventEntity currDevStatus = new DeveloperStatusEventEntity();
				currDevStatus.setDeveloperId(entity.getId());
				DeveloperStatusEntity providedStatus = getStatusByName(providedDeveloperStatusEvent.getStatus().getStatusName());
				if(providedStatus != null) {
					currDevStatus.setDeveloperStatusId(providedStatus.getId());
					currDevStatus.setStatusDate(providedDeveloperStatusEvent.getStatusDate());
					currDevStatus.setDeleted(false);
					currDevStatus.setLastModifiedUser(entity.getLastModifiedUser());
					entityManager.persist(currDevStatus);
					entityManager.flush();
				} else {
					String msg = "Could not find status with name " + providedDeveloperStatusEvent.getStatus().getStatusName() + "; cannot insert this status history entry for developer " + entity.getName();
					logger.error(msg);
					throw new EntityCreationException(msg);
				}
			}
		}
		
		entityManager.clear();
		return getById(dto.getId());
	}

	@Override
	public void updateStatus(DeveloperStatusEventDTO newStatusEvent) throws EntityCreationException {
		//create a new status history entry
		if(newStatusEvent.getStatus() != null && 
			!StringUtils.isEmpty(newStatusEvent.getStatus().getStatusName()) && 
			newStatusEvent.getStatusDate() != null) {
			DeveloperStatusEventEntity currDevStatus = new DeveloperStatusEventEntity();
			currDevStatus.setDeveloperId(newStatusEvent.getDeveloperId());
			DeveloperStatusEntity defaultStatus = getStatusByName(newStatusEvent.getStatus().getStatusName());
			if(defaultStatus != null) {
				currDevStatus.setDeveloperStatusId(defaultStatus.getId());
				currDevStatus.setStatusDate(newStatusEvent.getStatusDate());
				currDevStatus.setDeleted(false);
				currDevStatus.setLastModifiedUser(Util.getCurrentUser().getId());
				entityManager.persist(currDevStatus);
				entityManager.flush();
			} else {
				String msg = "Could not find status with name " + newStatusEvent.getStatus().getStatusName() + "; cannot insert this status history entry for developer with id " + newStatusEvent.getDeveloperId();
				logger.error(msg);
				throw new EntityCreationException(msg);
			}
		} else {
			String msg = "Developer Status name and date must be provided but at least one was not found; cannot insert this status history for developer with id " + newStatusEvent.getDeveloperId();
			logger.error(msg);
			throw new EntityCreationException(msg);
		}
	}
	
	@Override
	public DeveloperACBMapDTO updateTransparencyMapping(DeveloperACBMapDTO dto) {
		DeveloperACBMapEntity mapping = getTransparencyMappingEntity(dto.getDeveloperId(), dto.getAcbId());
		if(mapping == null) {
			return null;
		}

		mapping.setTransparencyAttestation(AttestationType.getValue(dto.getTransparencyAttestation()));
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		return new DeveloperACBMapDTO(mapping);
	}

	@Override
	@Transactional
	public void delete(Long id) throws EntityRetrievalException {
		DeveloperEntity toDelete = getEntityById(id);

		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			update(toDelete);
		}
	}

	@Override
	public void deleteTransparencyMapping(Long developerId, Long acbId) {
		DeveloperACBMapEntity toDelete = getTransparencyMappingEntity(developerId, acbId);
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.persist(toDelete);
			entityManager.flush();
		}
	}

	@Override
	public List<DeveloperDTO> findAll() {

		List<DeveloperEntity> entities = getAllEntities();
		List<DeveloperDTO> dtos = new ArrayList<>();

		for (DeveloperEntity entity : entities) {
			DeveloperDTO dto = new DeveloperDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	public List<DeveloperDTO> findAllIncludingDeleted() {

		List<DeveloperEntity> entities = getAllEntitiesIncludingDeleted();
		List<DeveloperDTO> dtos = new ArrayList<>();

		for (DeveloperEntity entity : entities) {
			DeveloperDTO dto = new DeveloperDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public List<DeveloperTransparency> getAllDevelopersWithTransparencies() {
		Query query = entityManager.createQuery("SELECT dt "
				+ "FROM DeveloperTransparencyEntity dt "
				, DeveloperTransparencyEntity.class);
		
		List<DeveloperTransparencyEntity> entityResults = query.getResultList();		
		List<DeveloperTransparency> domainResults = new ArrayList<DeveloperTransparency>();
		for(DeveloperTransparencyEntity entity : entityResults) {
			DeveloperTransparency domain = new DeveloperTransparency();
			domain.setId(entity.getId());
			domain.setName(entity.getName());
			domain.setStatus(entity.getStatus());
			domain.getListingCounts().setActive(entity.getCountActiveListings());
			domain.getListingCounts().setRetired(entity.getCountRetiredListings());
			domain.getListingCounts().setPending(entity.getCountPendingListings());
			domain.getListingCounts().setSuspendedByOncAcb(entity.getCountSuspendedByOncAcbListings());
			domain.getListingCounts().setSuspendedByOnc(entity.getCountSuspendedByOncListings());
			domain.getListingCounts().setTerminatedByOnc(entity.getCountTerminatedByOncListings());
			domain.getListingCounts().setWithdrawnByDeveloper(entity.getCountWithdrawnByDeveloperListings());
			domain.getListingCounts().setWithdrawnByDeveloperUnderSurveillance(entity.getCountWithdrawnByDeveloperUnderSurveillanceListings());
			domain.getListingCounts().setWithdrawnByOncAcb(entity.getCountWithdrawnByOncAcbListings());
			domain.setAcbAttestations(entity.getAcbAttestations());
			domain.setTransparencyAttestationUrls(entity.getTransparencyAttestationUrls());
			domainResults.add(domain);
		}
		return domainResults;
	}
	
	@Override
	public DeveloperACBMapDTO getTransparencyMapping(Long developerId, Long acbId) {
		DeveloperACBMapEntity mapping = getTransparencyMappingEntity(developerId, acbId);
		if(mapping == null) {
			return null;
		}
		return new DeveloperACBMapDTO(mapping);
	}

	@Override
	public List<DeveloperACBMapDTO> getAllTransparencyMappings() {
		List<DeveloperACBTransparencyMapEntity> entities = getTransparencyMappingEntities();
        List<DeveloperACBMapDTO> dtos = new ArrayList<>();

        for (DeveloperACBTransparencyMapEntity entity : entities) {
            DeveloperACBMapDTO dto = new DeveloperACBMapDTO(entity);
            dtos.add(dto);
        }
        return dtos;
	}

	@Override
	public DeveloperDTO getById(Long id) throws EntityRetrievalException {

		DeveloperEntity entity = getEntityById(id);
		DeveloperDTO dto = null;
		if(entity != null) {
			dto = new DeveloperDTO(entity);
		}
		return dto;
	}

	@Override
	public DeveloperDTO getByName(String name) {
		DeveloperEntity entity = getEntityByName(name);
		DeveloperDTO dto = null;
		if(entity != null) {
			dto = new DeveloperDTO(entity);
		}
		return dto;
	}

	@Override
	public DeveloperDTO getByCode(String code) {
		DeveloperEntity entity = getEntityByCode(code);
		DeveloperDTO dto = null;
		if(entity != null) {
			dto = new DeveloperDTO(entity);
		}
		return dto;
	}

	@Override
	public DeveloperDTO getByVersion(Long productVersionId) throws EntityRetrievalException {
		if(productVersionId == null) {
			throw new EntityRetrievalException("Version ID cannot be null!");
		}
		Query getDeveloperByVersionIdQuery = entityManager.createQuery(
				"SELECT ve FROM ProductVersionEntity pve,"
				+ "ProductEntity pe, DeveloperEntity ve "
				+ "WHERE (NOT pve.deleted = true) "
				+ "AND pve.id = :versionId "
				+ "AND pve.productId = pe.id "
				+ "AND ve.id = pe.developerId ", DeveloperEntity.class);
		getDeveloperByVersionIdQuery.setParameter("versionId", productVersionId);
		List<DeveloperEntity> results = getDeveloperByVersionIdQuery.getResultList();
		if(results != null && results.size() > 0) {
			return new DeveloperDTO(results.get(0));
		}
		return null;
	}
	
	public List<DecertifiedDeveloperDTO> getDecertifiedDevelopers(){
		
		Query getDecertifiedDevelopers =
				entityManager.createQuery(
				"FROM CertifiedProductDetailsEntity "
				+"WHERE developerStatusName IN (:banned) AND deleted = false AND acbIsDeleted = false", CertifiedProductDetailsEntity.class);
		getDecertifiedDevelopers.setParameter("banned", String.valueOf(DeveloperStatusType.UnderCertificationBanByOnc));
		List<CertifiedProductDetailsEntity> result = getDecertifiedDevelopers.getResultList();
		List<DecertifiedDeveloperDTO> dtoList = new ArrayList<DecertifiedDeveloperDTO>();
		//populate dtoList from result
		for(CertifiedProductDetailsEntity e : result){
			logger.debug("CertifiedProductDetailsEntity: " + e.getDeveloperId() + " " + e.getCertificationBodyId() + " " + e.getMeaningfulUseUsers());
			Boolean dtoIsInList = false;
			if(dtoList.size() > 0){
				for(DecertifiedDeveloperDTO dto : dtoList){
					logger.debug("DeveloperDecertifiedDTO: " + dto.getDeveloperId() + " " + dto.getAcbIdList() + " " + dto.getNumMeaningfulUse());
					// if developer already exists, update it to include ACB and aggregate numMeaningfulUse
					if(dto.getDeveloperId().equals(e.getDeveloperId())){
						logger.debug(dto.getDeveloperId() + " == " + e.getDeveloperId());
						// If this developer is not associated with the ACB, add the ACB
						if(!dto.getAcbIdList().contains(e.getCertificationBodyId())){
							logger.debug("dto does not contain " + e.getCertificationBodyName());
							dto.addAcb(e.getCertificationBodyId());
							logger.debug("added acb " + e.getCertificationBodyId() + " to dto with dev id == " + dto.getDeveloperId());
							dto.setDeveloperStatus(e.getDeveloperStatusName());
							logger.debug("set dto dev status to " + e.getDeveloperStatusName());
							dto.setDecertificationDate(e.getDeveloperStatusDate());
							logger.debug("set dev decert date to " + e.getDeveloperStatusDate());
							if(dto.getNumMeaningfulUse() != null){
								dto.setNumMeaningfulUse(e.getMeaningfulUseUsers());
								logger.debug("adding numMeaningfulUse to dto with value " + e.getMeaningfulUseUsers());
							}
							else{
								dto.setNumMeaningfulUse(e.getMeaningfulUseUsers());
								logger.debug("set dto numMeaningfulUse to value " + e.getMeaningfulUseUsers());
							}
							dtoIsInList = true;
							break;
						}
						// if developer exists and is associated with ACB, add numMeaningfulUse for this CP
						else{
							if(e.getMeaningfulUseUsers() != null){
								dto.setNumMeaningfulUse(e.getMeaningfulUseUsers());
								logger.debug("adding to dto's numMeaningfulUse with value " + e.getMeaningfulUseUsers());
							}
							dtoIsInList = true;
							break;
						}
					}
				}
			}
			if(!dtoIsInList){
				List<Long> acbList = new ArrayList<Long>();
				acbList.add(e.getCertificationBodyId());
				DecertifiedDeveloperDTO newDto = new DecertifiedDeveloperDTO(e.getDeveloperId(), acbList, e.getDeveloperStatusName(), e.getDeveloperStatusDate(), e.getMeaningfulUseUsers());
				dtoList.add(newDto);
				logger.debug("adding newDto to list with values: " + e.getMeaningfulUseUsers());
			}
		}
		
		return dtoList;
	}

	private void create(DeveloperEntity entity) {
		entityManager.persist(entity);
		entityManager.flush();
	}
	
	private void update(DeveloperEntity entity) {
		entityManager.merge(entity);
		entityManager.flush();
	}

	private List<DeveloperEntity> getAllEntities() {
		List<DeveloperEntity> result = entityManager.createQuery( "SELECT DISTINCT v from "
				+ "DeveloperEntity v "
				+ "LEFT OUTER JOIN FETCH v.address "
				+ "LEFT OUTER JOIN FETCH v.contact "
				+ "LEFT OUTER JOIN FETCH v.statusEvents statusEvents "
				+ "LEFT OUTER JOIN FETCH statusEvents.developerStatus "
				+ "LEFT OUTER JOIN FETCH v.developerCertificationStatuses "
				+ "where (NOT v.deleted = true)", DeveloperEntity.class).getResultList();
		return result;
	}
	
	private List<DeveloperEntity> getAllEntitiesIncludingDeleted() {
		List<DeveloperEntity> result = entityManager.createQuery( "SELECT DISTINCT v from "
				+ "DeveloperEntity v "
				+ "LEFT OUTER JOIN FETCH v.address "
				+ "LEFT OUTER JOIN FETCH v.contact "
				+ "LEFT OUTER JOIN FETCH v.statusEvents statusEvents "
				+ "LEFT OUTER JOIN FETCH statusEvents.developerStatus "
				+ "LEFT OUTER JOIN FETCH v.developerCertificationStatuses ", DeveloperEntity.class).getResultList();
		return result;
	}

	private DeveloperEntity getEntityById(Long id) throws EntityRetrievalException {

		DeveloperEntity entity = null;

		Query query = entityManager.createQuery( "SELECT DISTINCT v from "
				+ "DeveloperEntity v "
				+ "LEFT OUTER JOIN FETCH v.address "
				+ "LEFT OUTER JOIN FETCH v.contact "
				+ "LEFT OUTER JOIN FETCH v.statusEvents statusEvents "
				+ "LEFT OUTER JOIN FETCH statusEvents.developerStatus "
				+ "where (NOT v.deleted = true) AND (v.id = :entityid) ", DeveloperEntity.class );
		query.setParameter("entityid", id);
		List<DeveloperEntity> result = query.getResultList();

		if (result.size() > 0){
			entity = result.get(0);
		}

		return entity;
	}

	private DeveloperEntity getEntityByName(String name) {

		DeveloperEntity entity = null;

		Query query = entityManager.createQuery( "SELECT DISTINCT v from "
				+ "DeveloperEntity v "
				+ "LEFT OUTER JOIN FETCH v.address "
				+ "LEFT OUTER JOIN FETCH v.contact "
				+ "LEFT OUTER JOIN FETCH v.statusEvents statusEvents "
				+ "LEFT OUTER JOIN FETCH statusEvents.developerStatus "
				+ "where (NOT v.deleted = true) AND (v.name = :name) ", DeveloperEntity.class );
		query.setParameter("name", name);
		List<DeveloperEntity> result = query.getResultList();

		if(result.size() > 0) {
			entity = result.get(0);
		}

		return entity;
	}

	private DeveloperEntity getEntityByCode(String code) {

		DeveloperEntity entity = null;

		Query query = entityManager.createQuery( "SELECT DISTINCT v from "
				+ "DeveloperEntity v "
				+ "LEFT OUTER JOIN FETCH v.address "
				+ "LEFT OUTER JOIN FETCH v.contact "
				+ "LEFT OUTER JOIN FETCH v.statusEvents statusEvents "
				+ "LEFT OUTER JOIN FETCH statusEvents.developerStatus "
				+ "where (NOT v.deleted = true) AND (v.developerCode = :code) ", DeveloperEntity.class );
		query.setParameter("code", code);
		List<DeveloperEntity> result = query.getResultList();

		if(result.size() > 0) {
			entity = result.get(0);
		}

		return entity;
	}

	private DeveloperACBMapEntity getTransparencyMappingEntity(Long developerId, Long acbId) {
		Query query = entityManager.createQuery( "FROM DeveloperACBMapEntity map "
				+ "LEFT OUTER JOIN FETCH map.certificationBody where "
				+ "(NOT map.deleted = true) "
				+ "AND map.developerId = :developerId "
				+ "AND map.certificationBodyId = :acbId", DeveloperACBMapEntity.class);
		query.setParameter("developerId", developerId);
		query.setParameter("acbId", acbId);

		List<DeveloperACBMapEntity> results = query.getResultList();
		if(results != null && results.size() > 0) {
			return results.get(0);
		}
		return null;
	}

	private List<DeveloperACBTransparencyMapEntity> getTransparencyMappingEntities() {
		List<DeveloperACBTransparencyMapEntity> result = entityManager.createQuery( "FROM DeveloperACBTransparencyMapEntity",DeveloperACBTransparencyMapEntity.class).getResultList();
		return result;
	}
	
	private DeveloperStatusEntity getStatusByName(String statusName) {
		DeveloperStatusDAOImpl statusDaoImpl = (DeveloperStatusDAOImpl) statusDao;
		List<DeveloperStatusEntity> statuses = statusDaoImpl.getEntitiesByName(statusName);
		if(statuses == null || statuses.size() == 0) {
			logger.error("Could not find the " + statusName + " status");
			return null;
		} 
		return statuses.get(0);
	}
}
