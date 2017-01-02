package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductClassificationTypeDAO;
import gov.healthit.chpl.dto.ProductClassificationTypeDTO;
import gov.healthit.chpl.entity.ProductClassificationTypeEntity;

@Repository("productClassificationTypeDAO")
public class ProductClassificationTypeDAOImpl extends BaseDAOImpl implements ProductClassificationTypeDAO {

	@Override
	public void create(ProductClassificationTypeDTO dto) throws EntityCreationException,
			EntityRetrievalException {
		
		ProductClassificationTypeEntity entity = null;
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
			
			entity = new ProductClassificationTypeEntity();
			entity.setCreationDate(dto.getCreationDate());
			entity.setDeleted(dto.getDeleted());
			entity.setId(dto.getId());
			entity.setName(dto.getName());
			//entity.setLastModifiedDate(result.getLastModifiedDate());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
			create(entity);	
		}
		
	}

	@Override
	public void update(ProductClassificationTypeDTO dto) throws EntityRetrievalException {
		
		ProductClassificationTypeEntity entity = this.getEntityById(dto.getId());
		entity.setCreationDate(dto.getCreationDate());
		entity.setDeleted(dto.getDeleted());
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		//entity.setLastModifiedDate(result.getLastModifiedDate());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
		update(entity);
	}

	@Override
	public void delete(Long id) {
		Query query = entityManager.createQuery("UPDATE ProductClassificationTypeEntity SET deleted = true WHERE product_classification_type_id = :entityid");
		query.setParameter("entityid", id);
		query.executeUpdate();
	}

	@Override
	public List<ProductClassificationTypeDTO> findAll() {
		
		List<ProductClassificationTypeEntity> entities = getAllEntities();
		List<ProductClassificationTypeDTO> dtos = new ArrayList<>();
		
		for (ProductClassificationTypeEntity entity : entities) {
			ProductClassificationTypeDTO dto = new ProductClassificationTypeDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public ProductClassificationTypeDTO getById(Long id) throws EntityRetrievalException {
		
		ProductClassificationTypeDTO dto = null;
		ProductClassificationTypeEntity entity = getEntityById(id);
		if (entity != null){
			dto = new ProductClassificationTypeDTO(entity);
		}
		return dto;
		
	}
	
	@Override
	public ProductClassificationTypeDTO getByName(String name) {
		ProductClassificationTypeEntity entity = getEntityByName(name);
		if(entity != null) {
			ProductClassificationTypeDTO dto = new ProductClassificationTypeDTO(entity);
			return dto;
		}
		return null;
	}
	
	private void create(ProductClassificationTypeEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
	}
	
	private void update(ProductClassificationTypeEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private List<ProductClassificationTypeEntity> getAllEntities() {
		
		List<ProductClassificationTypeEntity> result = entityManager.createQuery( "from ProductClassificationTypeEntity where (NOT deleted = true) ", ProductClassificationTypeEntity.class).getResultList();
		return result;
		
	}
	
	private ProductClassificationTypeEntity getEntityById(Long id) throws EntityRetrievalException {
		
		ProductClassificationTypeEntity entity = null;
			
		Query query = entityManager.createQuery( "from ProductClassificationTypeEntity where (NOT deleted = true) AND (product_classification_type_id = :entityid) ", ProductClassificationTypeEntity.class );
		query.setParameter("entityid", id);
		List<ProductClassificationTypeEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate product version id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private ProductClassificationTypeEntity getEntityByName(String name) {
		
		ProductClassificationTypeEntity entity = null;
			
		Query query = entityManager.createQuery( "from ProductClassificationTypeEntity where (NOT deleted = true) AND (name = :name) ", ProductClassificationTypeEntity.class );
		query.setParameter("name", name);
		List<ProductClassificationTypeEntity> result = query.getResultList();
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
}
