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

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.VendorDAO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.VendorACBMapDTO;
import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.entity.CertifiedProductEntity;
import gov.healthit.chpl.entity.VendorACBMapEntity;
import gov.healthit.chpl.entity.VendorEntity;

@Repository("vendorDAO")
public class VendorDAOImpl extends BaseDAOImpl implements VendorDAO {

	private static final Logger logger = LogManager.getLogger(VendorDAOImpl.class);
	@Autowired AddressDAO addressDao;
	
	@Override
	@Transactional
	public VendorDTO create(VendorDTO dto) throws EntityCreationException, EntityRetrievalException {
		
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
			return new VendorDTO(entity);
		}	
	}

	@Override
	public VendorACBMapDTO createTransparencyMapping(VendorACBMapDTO dto) {
		VendorACBMapEntity mapping = new VendorACBMapEntity();
		mapping.setVendorId(dto.getVendorId());
		mapping.setCertificationBodyId(dto.getAcbId());
		mapping.setTransparencyAttestation(dto.getTransparencyAttestation());
		mapping.setCreationDate(new Date());
		mapping.setDeleted(false);
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		return new VendorACBMapDTO(mapping);
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
	public VendorACBMapDTO updateTransparencyMapping(VendorACBMapDTO dto) {
		VendorACBMapEntity mapping = getTransparencyMappingEntity(dto.getVendorId(), dto.getAcbId());
		if(mapping == null) {
			return null;
		}
		
		mapping.setTransparencyAttestation(dto.getTransparencyAttestation());
		mapping.setLastModifiedDate(new Date());
		mapping.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(mapping);
		entityManager.flush();
		return new VendorACBMapDTO(mapping);
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
	public void deleteTransparencyMapping(Long vendorId, Long acbId) {
		VendorACBMapEntity toDelete = getTransparencyMappingEntity(vendorId, acbId);
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.persist(toDelete);
			entityManager.flush();
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
	public VendorACBMapDTO getTransparencyMapping(Long vendorId, Long acbId) {
		VendorACBMapEntity mapping = getTransparencyMappingEntity(vendorId, acbId);
		if(mapping == null) {
			return null;
		}
		return new VendorACBMapDTO(mapping);
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
	
	public VendorDTO getByCertifiedProduct(CertifiedProductDTO cpDto) throws EntityRetrievalException {
		if(cpDto == null || cpDto.getProductVersionId() == null) {
			throw new EntityRetrievalException("Version ID cannot be null!");
		}
		Query getVendorByVersionIdQuery = entityManager.createQuery(
				"FROM ProductVersionEntity pve,"
				+ "ProductEntity pe, VendorEntity ve " 
				+ "WHERE (NOT pve.deleted = true) "
				+ "AND pve.id = :versionId "
				+ "AND pve.productId = pe.id " 
				+ "AND ve.id = pe.vendorId ", VendorEntity.class);
		getVendorByVersionIdQuery.setParameter("versionid", cpDto.getProductVersionId());
		Object result = getVendorByVersionIdQuery.getSingleResult();
		if(result == null) {
			return null;
		}
		VendorEntity ve = (VendorEntity)result;
		return new VendorDTO(ve);
	}
	
	private void create(VendorEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
	}
	
	private void update(VendorEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
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
	
	private VendorACBMapEntity getTransparencyMappingEntity(Long vendorId, Long acbId) {
		Query query = entityManager.createQuery( "FROM VendorACBMapEntity where "
				+ "(NOT deleted = true) "
				+ "AND vendorId = :vendorId "
				+ "AND certificationBodyId = :acbId", VendorACBMapEntity.class);
		query.setParameter("vendorId", vendorId);
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
		return (VendorACBMapEntity)result;
	}
}
