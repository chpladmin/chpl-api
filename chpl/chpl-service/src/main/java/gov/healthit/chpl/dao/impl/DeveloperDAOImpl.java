package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.entity.ContactEntity;
import gov.healthit.chpl.entity.DeveloperACBMapEntity;
import gov.healthit.chpl.entity.DeveloperACBTransparencyMapEntity;
import gov.healthit.chpl.entity.DeveloperEntity;
import gov.healthit.chpl.entity.DeveloperStatusEntity;
import gov.healthit.chpl.entity.DeveloperStatusType;

@Repository("developerDAO")
public class DeveloperDAOImpl extends BaseDAOImpl implements DeveloperDAO {

	private static final Logger logger = LogManager.getLogger(DeveloperDAOImpl.class);
	private static final DeveloperStatusType DEFAULT_STATUS = DeveloperStatusType.Active;
	@Autowired AddressDAO addressDao;
	@Autowired ContactDAO contactDao;
	@Autowired DeveloperStatusDAO statusDao;

	@Override
	@CacheEvict(value="searchOptionsCache", allEntries=true)
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
			
			//set the status; will be Active by default
			DeveloperStatusEntity statusResult = getStatusByName(DEFAULT_STATUS.toString());
			if(statusResult != null) {
				entity.setStatus(statusResult);
			} else {
				String msg = "Could not find the " + DEFAULT_STATUS + " status to create the new developer " + dto.getName();
				logger.error(msg);
				throw new EntityCreationException(msg);
			}
			
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
			return new DeveloperDTO(entity);
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
	@CacheEvict(value="searchOptionsCache", allEntries=true)
	public DeveloperDTO update(DeveloperDTO dto) throws EntityRetrievalException {
		DeveloperEntity entity = this.getEntityById(dto.getId());

		if(entity == null) {
			throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
		}

		if(dto.getStatus() != null) {
			DeveloperStatusEntity status = getStatusByName(dto.getStatus().getStatusName());
			if(status != null) {
				entity.setStatus(status);
			} else {
				throw new EntityRetrievalException("No status with name " + dto.getStatus().getStatusName() + " was found to update developer id " + dto.getId());
			}
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
		return new DeveloperDTO(entity);
	}

	@Override
	public DeveloperDTO updateStatus(DeveloperDTO toUpdate) throws EntityRetrievalException {
		DeveloperEntity entityToUpdate = this.getEntityById(toUpdate.getId());
		if(entityToUpdate == null) {
			throw new EntityRetrievalException("Developer with id " + toUpdate.getId() + " does not exist");
		}
		
		//set the status
		DeveloperStatusEntity status = getStatusByName(toUpdate.getStatus().getStatusName());
		if(status != null) {
			entityToUpdate.setStatus(status);
		} else {
			throw new EntityRetrievalException("No status with name " + toUpdate.getStatus().getStatusName() + " was found to update developer id " + toUpdate.getId());
		}
		
		update(entityToUpdate);
		return new DeveloperDTO(entityToUpdate);
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

	private void create(DeveloperEntity entity) {
		entityManager.persist(entity);
		entityManager.flush();
	}
	
	@CacheEvict(value="searchOptionsCache", allEntries=true)
	private void update(DeveloperEntity entity) {
		entityManager.merge(entity);
		entityManager.flush();
	}

	private List<DeveloperEntity> getAllEntities() {
		List<DeveloperEntity> result = entityManager.createQuery( "SELECT v from "
				+ "DeveloperEntity v "
				+ "LEFT OUTER JOIN FETCH v.address "
				+ "LEFT OUTER JOIN FETCH v.contact "
				+ "LEFT OUTER JOIN FETCH v.status "
				+ "LEFT OUTER JOIN FETCH v.developerCertificationStatuses "
				+ "where (NOT v.deleted = true)", DeveloperEntity.class).getResultList();
		return result;
	}
	
	private List<DeveloperEntity> getAllEntitiesIncludingDeleted() {
		List<DeveloperEntity> result = entityManager.createQuery( "SELECT v from "
				+ "DeveloperEntity v "
				+ "LEFT OUTER JOIN FETCH v.address "
				+ "LEFT OUTER JOIN FETCH v.contact ", DeveloperEntity.class).getResultList();
		return result;
	}

	private DeveloperEntity getEntityById(Long id) throws EntityRetrievalException {

		DeveloperEntity entity = null;

		Query query = entityManager.createQuery( "SELECT v from "
				+ "DeveloperEntity v "
				+ "LEFT OUTER JOIN FETCH v.address "
				+ "LEFT OUTER JOIN FETCH v.contact "
				+ "LEFT OUTER JOIN FETCH v.status "
				+ "where (NOT v.deleted = true) AND (vendor_id = :entityid) ", DeveloperEntity.class );
		query.setParameter("entityid", id);
		List<DeveloperEntity> result = query.getResultList();

		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate developer id in database.");
		} else if(result.size() == 1) {
			entity = result.get(0);
		}

		return entity;
	}

	private DeveloperEntity getEntityByName(String name) {

		DeveloperEntity entity = null;

		Query query = entityManager.createQuery( "SELECT v from "
				+ "DeveloperEntity v "
				+ "LEFT OUTER JOIN FETCH v.address "
				+ "LEFT OUTER JOIN FETCH v.contact "
				+ "LEFT OUTER JOIN FETCH v.status "
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

		Query query = entityManager.createQuery( "SELECT v from "
				+ "DeveloperEntity v "
				+ "LEFT OUTER JOIN FETCH v.address "
				+ "LEFT OUTER JOIN FETCH v.contact "
				+ "LEFT OUTER JOIN FETCH v.status "
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
