package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.ContactDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.entity.CertifiedProductEntity;
import gov.healthit.chpl.entity.ContactEntity;
import gov.healthit.chpl.entity.DeveloperACBMapEntity;
import gov.healthit.chpl.entity.DeveloperEntity;

@Repository("developerDAO")
public class DeveloperDAOImpl extends BaseDAOImpl implements DeveloperDAO {

	private static final Logger logger = LogManager.getLogger(DeveloperDAOImpl.class);
	@Autowired AddressDAO addressDao;
	@Autowired ContactDAO contactDao;
	
	@Override
	@Transactional
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
				ContactEntity contact = null;
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
	@Transactional
	public DeveloperEntity update(DeveloperDTO dto) throws EntityRetrievalException {
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
			ContactEntity contact = null;
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
		return entity;
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

	@Override
	public DeveloperACBMapDTO getTransparencyMapping(Long developerId, Long acbId) {
		DeveloperACBMapEntity mapping = getTransparencyMappingEntity(developerId, acbId);
		if(mapping == null) {
			return null;
		}
		return new DeveloperACBMapDTO(mapping);
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
	
	public DeveloperDTO getByCertifiedProduct(CertifiedProductDTO cpDto) throws EntityRetrievalException {
		if(cpDto == null || cpDto.getProductVersionId() == null) {
			throw new EntityRetrievalException("Version ID cannot be null!");
		}
		Query getDeveloperByVersionIdQuery = entityManager.createQuery(
				"FROM ProductVersionEntity pve,"
				+ "ProductEntity pe, DeveloperEntity ve " 
				+ "WHERE (NOT pve.deleted = true) "
				+ "AND pve.id = :versionId "
				+ "AND pve.productId = pe.id " 
				+ "AND ve.id = pe.developerId ", DeveloperEntity.class);
		getDeveloperByVersionIdQuery.setParameter("versionid", cpDto.getProductVersionId());
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
	
	private void update(DeveloperEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private List<DeveloperEntity> getAllEntities() {
		List<DeveloperEntity> result = entityManager.createQuery( "SELECT v from "
				+ "DeveloperEntity v "
				+ "LEFT OUTER JOIN FETCH v.address "
				+ "LEFT OUTER JOIN FETCH v.contact "
				+ "where (NOT v.deleted = true)", DeveloperEntity.class).getResultList();
		return result;
		
	}

	private DeveloperEntity getEntityById(Long id) throws EntityRetrievalException {
		
		DeveloperEntity entity = null;
			
		Query query = entityManager.createQuery( "SELECT v from "
				+ "DeveloperEntity v "
				+ "LEFT OUTER JOIN FETCH v.address "
				+ "LEFT OUTER JOIN FETCH v.contact "
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
				+ "where (NOT v.deleted = true) AND (v.developerCode = :code) ", DeveloperEntity.class );
		query.setParameter("code", code);
		List<DeveloperEntity> result = query.getResultList();
		
		if(result.size() > 0) {
			entity = result.get(0);
		}

		return entity;
	}

	private DeveloperACBMapEntity getTransparencyMappingEntity(Long developerId, Long acbId) {
		Query query = entityManager.createQuery( "FROM DeveloperACBMapEntity where "
				+ "(NOT deleted = true) "
				+ "AND developerId = :developerId "
				+ "AND certificationBodyId = :acbId", DeveloperACBMapEntity.class);
		query.setParameter("developerId", developerId);
		query.setParameter("acbId", acbId);
		
		Object result = null;
		try {
			result = query.getSingleResult();
		} 
		catch(NoResultException ex) {}
		catch(NonUniqueResultException ex) {}
		
		if(result == null) {
			return null;
		}
		return (DeveloperACBMapEntity)result;
	}
}
