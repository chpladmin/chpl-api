package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.VendorDAO;
import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.entity.VendorEntity;

public class VendorDAOImpl extends BaseDAOImpl implements VendorDAO {

	@Override
	public void create(VendorDTO dto) throws EntityCreationException,
			EntityRetrievalException {
		
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
			entity.setCreationDate(dto.getCreationDate());
			entity.setDeleted(dto.getDeleted());
			entity.setId(dto.getId());
			entity.setName(dto.getName());
			entity.setAddressId(dto.getAddressId());
			entity.setWebsite(dto.getWebsite());
			//entity.setLastModifiedDate(result.getLastModifiedDate());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
			create(entity);	
		}
		
	}

	@Override
	public void update(VendorDTO dto) throws EntityRetrievalException {
		
		VendorEntity entity = this.getEntityById(dto.getId());
		entity.setCreationDate(dto.getCreationDate());
		entity.setDeleted(dto.getDeleted());
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setAddressId(dto.getAddressId());
		entity.setWebsite(dto.getWebsite());
		//entity.setLastModifiedDate(result.getLastModifiedDate());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
		update(entity);
	}

	@Override
	public void delete(Long id) {
		Query query = entityManager.createQuery("UPDATE VendorEntity SET deleted = true WHERE vendor_id = :entityid");
		query.setParameter("entityid", id);
		query.executeUpdate();
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
		VendorDTO dto = new VendorDTO(entity);
		return dto;
		
	}
	
	
	private void create(VendorEntity entity) {
		
		entityManager.persist(entity);
		
	}
	
	private void update(VendorEntity entity) {
		
		entityManager.merge(entity);	
	
	}
	
	private List<VendorEntity> getAllEntities() {
		
		List<VendorEntity> result = entityManager.createQuery( "from VendorEntity where (NOT deleted = true) ", VendorEntity.class).getResultList();
		return result;
		
	}
	
	private VendorEntity getEntityById(Long id) throws EntityRetrievalException {
		
		VendorEntity entity = null;
			
		Query query = entityManager.createQuery( "from VendorEntity where (NOT deleted = true) AND (vendor_id = :entityid) ", VendorEntity.class );
		query.setParameter("entityid", id);
		List<VendorEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate vendor id in database.");
		}
		
		if (result.size() < 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
}
