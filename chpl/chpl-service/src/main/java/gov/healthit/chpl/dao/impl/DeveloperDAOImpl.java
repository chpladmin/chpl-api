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
import gov.healthit.chpl.dto.DecertifiedDeveloperDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusHistoryDTO;
import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.entity.CertifiedProductDetailsEntity;
import gov.healthit.chpl.entity.ContactEntity;
import gov.healthit.chpl.entity.DeveloperACBMapEntity;
import gov.healthit.chpl.entity.DeveloperACBTransparencyMapEntity;
import gov.healthit.chpl.entity.DeveloperEntity;
import gov.healthit.chpl.entity.DeveloperStatusEntity;
import gov.healthit.chpl.entity.DeveloperStatusHistoryEntity;
import gov.healthit.chpl.entity.DeveloperStatusType;

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
					Query query = entityManager.createQuery("from ContactEntity a where (NOT deleted = true) AND (contact_id = :entityid) ", ContactEntity.class );
					query.setParameter("entityid", dto.getContact().getId());
					List<ContactEntity> result = query.getResultList();
					if(result != null && result.size() > 0) {
						entity.setContact(result.get(0));
					}
				} else {
					entity.setContact(contactDao.create(dto.getContact()));
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
			if(dto.getStatusHistory() == null || dto.getStatusHistory().size() == 0) {
				DeveloperStatusHistoryEntity initialDeveloperStatus = new DeveloperStatusHistoryEntity();
				initialDeveloperStatus.setDeveloperId(entity.getId());
				DeveloperStatusEntity defaultStatus = getStatusByName(DEFAULT_STATUS.toString());
				initialDeveloperStatus.setDeveloperStatusId(defaultStatus.getId());
				initialDeveloperStatus.setStatusDate(entity.getCreationDate());
				initialDeveloperStatus.setDeleted(false);
				initialDeveloperStatus.setLastModifiedUser(entity.getLastModifiedUser());
				entityManager.persist(initialDeveloperStatus);
				entityManager.flush();
			} else {
				for(DeveloperStatusHistoryDTO providedDeveloperStatusHistory : dto.getStatusHistory()) {
					if(providedDeveloperStatusHistory.getStatus() != null && 
						!StringUtils.isEmpty(providedDeveloperStatusHistory.getStatus().getStatusName()) && 
						providedDeveloperStatusHistory.getStatusDate() != null) {
						DeveloperStatusHistoryEntity currDevStatus = new DeveloperStatusHistoryEntity();
						currDevStatus.setDeveloperId(entity.getId());
						DeveloperStatusEntity defaultStatus = getStatusByName(providedDeveloperStatusHistory.getStatus().getStatusName());
						if(defaultStatus != null) {
							currDevStatus.setDeveloperStatusId(defaultStatus.getId());
							currDevStatus.setStatusDate(providedDeveloperStatusHistory.getStatusDate());
							currDevStatus.setDeleted(false);
							currDevStatus.setLastModifiedUser(entity.getLastModifiedUser());
							entityManager.persist(currDevStatus);
							entityManager.flush();
						} else {
							String msg = "Could not find status with name " + providedDeveloperStatusHistory.getStatus().getStatusName() + "; cannot insert this status history entry for developer " + entity.getName();
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
			if(dto.getContact().getId() != null) {
				Query query = entityManager.createQuery("from ContactEntity a where (NOT deleted = true) AND (contact_id = :entityid) ", ContactEntity.class );
				query.setParameter("entityid", dto.getContact().getId());
				List<ContactEntity> result = query.getResultList();
				if(result != null && result.size() > 0) {
					entity.setContact(result.get(0));
				}
			} else {
				try {
					if(StringUtils.isEmpty(dto.getContact().getFirstName())) {
						dto.getContact().setFirstName("");
					}
					if(StringUtils.isEmpty(dto.getContact().getEmail())) {
						dto.getContact().setEmail("");
					}
					if(StringUtils.isEmpty(dto.getContact().getPhoneNumber())) {
						dto.getContact().setPhoneNumber("");
					}
					entity.setContact(contactDao.create(dto.getContact()));
				} catch(EntityCreationException ex) {
					logger.error("could not create contact.", ex);
				}
			}
		} else {
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
		
		//delete existing developer status history
		for(DeveloperStatusHistoryEntity existingDeveloperStatusHistory : entity.getStatusHistory()) {
			entityManager.remove(existingDeveloperStatusHistory);
		}
		entityManager.flush();
		
		//add passed-in developer status history
		for(DeveloperStatusHistoryDTO providedDeveloperStatusHistory : dto.getStatusHistory()) {
			if(providedDeveloperStatusHistory.getStatus() != null && 
				!StringUtils.isEmpty(providedDeveloperStatusHistory.getStatus().getStatusName()) && 
				providedDeveloperStatusHistory.getStatusDate() != null) {
				DeveloperStatusHistoryEntity currDevStatus = new DeveloperStatusHistoryEntity();
				currDevStatus.setDeveloperId(entity.getId());
				DeveloperStatusEntity defaultStatus = getStatusByName(providedDeveloperStatusHistory.getStatus().getStatusName());
				if(defaultStatus != null) {
					currDevStatus.setDeveloperStatusId(defaultStatus.getId());
					currDevStatus.setStatusDate(providedDeveloperStatusHistory.getStatusDate());
					currDevStatus.setDeleted(false);
					currDevStatus.setLastModifiedUser(entity.getLastModifiedUser());
					entityManager.persist(currDevStatus);
					entityManager.flush();
				} else {
					String msg = "Could not find status with name " + providedDeveloperStatusHistory.getStatus().getStatusName() + "; cannot insert this status history entry for developer " + entity.getName();
					logger.error(msg);
					throw new EntityCreationException(msg);
				}
			} else {
				String msg = "Developer Status name and date must be provided but at least one was not found; cannot insert this status history for developer " + entity.getName();
				logger.error(msg);
				throw new EntityCreationException(msg);
			}
		}
		
		entityManager.clear();
		return getById(dto.getId());
	}

	@Override
	public void updateStatus(DeveloperStatusHistoryDTO newStatusHistory) throws EntityCreationException {
		//create a new status history entry
		if(newStatusHistory.getStatus() != null && 
			!StringUtils.isEmpty(newStatusHistory.getStatus().getStatusName()) && 
			newStatusHistory.getStatusDate() != null) {
			DeveloperStatusHistoryEntity currDevStatus = new DeveloperStatusHistoryEntity();
			currDevStatus.setDeveloperId(newStatusHistory.getDeveloperId());
			DeveloperStatusEntity defaultStatus = getStatusByName(newStatusHistory.getStatus().getStatusName());
			if(defaultStatus != null) {
				currDevStatus.setDeveloperStatusId(defaultStatus.getId());
				currDevStatus.setStatusDate(newStatusHistory.getStatusDate());
				currDevStatus.setDeleted(false);
				currDevStatus.setLastModifiedUser(Util.getCurrentUser().getId());
				entityManager.persist(currDevStatus);
				entityManager.flush();
			} else {
				String msg = "Could not find status with name " + newStatusHistory.getStatus().getStatusName() + "; cannot insert this status history entry for developer with id " + newStatusHistory.getDeveloperId();
				logger.error(msg);
				throw new EntityCreationException(msg);
			}
		} else {
			String msg = "Developer Status name and date must be provided but at least one was not found; cannot insert this status history for developer with id " + newStatusHistory.getDeveloperId();
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
		Object result = getDeveloperByVersionIdQuery.getSingleResult();
		if(result == null) {
			return null;
		}
		DeveloperEntity ve = (DeveloperEntity)result;
		return new DeveloperDTO(ve);
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
				+ "LEFT OUTER JOIN FETCH v.statusHistory "
				+ "LEFT OUTER JOIN FETCH v.developerCertificationStatuses "
				+ "where (NOT v.deleted = true)", DeveloperEntity.class).getResultList();
		return result;
	}
	
	private List<DeveloperEntity> getAllEntitiesIncludingDeleted() {
		List<DeveloperEntity> result = entityManager.createQuery( "SELECT DISTINCT v from "
				+ "DeveloperEntity v "
				+ "LEFT OUTER JOIN FETCH v.address "
				+ "LEFT OUTER JOIN FETCH v.contact "
				+ "LEFT OUTER JOIN FETCH v.statusHistory "
				+ "LEFT OUTER JOIN FETCH v.developerCertificationStatuses ", DeveloperEntity.class).getResultList();
		return result;
	}

	private DeveloperEntity getEntityById(Long id) throws EntityRetrievalException {

		DeveloperEntity entity = null;

		Query query = entityManager.createQuery( "SELECT DISTINCT v from "
				+ "DeveloperEntity v "
				+ "LEFT OUTER JOIN FETCH v.address "
				+ "LEFT OUTER JOIN FETCH v.contact "
				+ "LEFT OUTER JOIN FETCH v.statusHistory "
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
				+ "LEFT OUTER JOIN FETCH v.statusHistory "
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
				+ "LEFT OUTER JOIN FETCH v.statusHistory "
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
