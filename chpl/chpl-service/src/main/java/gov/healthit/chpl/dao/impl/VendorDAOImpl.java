package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.VendorDAO;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.entity.VendorEntity;

@Repository("vendorDAO")
public class VendorDAOImpl extends BaseDAOImpl implements VendorDAO {

	private static final Logger logger = LogManager.getLogger(VendorDAOImpl.class);
	@Autowired AddressDAO addressDao;
	
	@Override
	@Transactional
	public VendorEntity create(VendorDTO dto) throws EntityCreationException, EntityRetrievalException {
		
		VendorEntity entity = null;
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
			entity = new VendorEntity();

			if(dto.getAddress() != null)
			{
				entity.setAddress(addressDao.mergeAddress(dto.getAddress()));
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
			return entity;
		}	
	}

	@Override
	@Transactional
	public VendorEntity update(VendorDTO dto) throws EntityRetrievalException {
		VendorEntity entity = this.getEntityById(dto.getId());
		
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
	@Transactional
	public void delete(Long id) throws EntityRetrievalException {
		VendorEntity toDelete = getEntityById(id);
		
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			update(toDelete);
		}
	}
	
	@Override
	public List<VendorDTO> findAll() {
		
		List<VendorEntity> entities = getAllEntities();
		List<VendorDTO> dtos = new ArrayList<>();
		
		for (VendorEntity entity : entities) {
			VendorDTO dto = new VendorDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public VendorDTO getById(Long id) throws EntityRetrievalException {
		
		VendorEntity entity = getEntityById(id);
		VendorDTO dto = null;
		if(entity != null) {
			dto = new VendorDTO(entity);
		}
		return dto;
	}
	
	@Override
	public VendorDTO getByName(String name) {
		VendorEntity entity = getEntityByName(name);
		VendorDTO dto = null;
		if(entity != null) {
			dto = new VendorDTO(entity);
		}
		return dto;
	}
	
	private void create(VendorEntity entity) {
		
		entityManager.persist(entity);
	}
	
	private void update(VendorEntity entity) {
		
		entityManager.merge(entity);	
	
	}
	
	private List<VendorEntity> getAllEntities() {
		List<VendorEntity> result = entityManager.createQuery( "SELECT v from VendorEntity v LEFT OUTER JOIN FETCH v.address where (NOT v.deleted = true)", VendorEntity.class).getResultList();
		return result;
		
	}

	private VendorEntity getEntityById(Long id) throws EntityRetrievalException {
		
		VendorEntity entity = null;
			
		Query query = entityManager.createQuery( "SELECT v from VendorEntity v LEFT OUTER JOIN FETCH v.address where (NOT v.deleted = true) AND (vendor_id = :entityid) ", VendorEntity.class );
		query.setParameter("entityid", id);
		List<VendorEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate vendor id in database.");
		} else if(result.size() == 1) {
			entity = result.get(0);
		}

		return entity;
	}
	
	private VendorEntity getEntityByName(String name) {
		
		VendorEntity entity = null;
			
		Query query = entityManager.createQuery( "SELECT v from VendorEntity v LEFT OUTER JOIN FETCH v.address where (NOT v.deleted = true) AND (v.name = :name) ", VendorEntity.class );
		query.setParameter("name", name);
		List<VendorEntity> result = query.getResultList();
		
		if(result.size() > 0) {
			entity = result.get(0);
		}

		return entity;
	}
}
