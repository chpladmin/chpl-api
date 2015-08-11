package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.ProductVersionEntity;

@Repository("productVersionDAO")
public class ProductVersionDAOImpl extends BaseDAOImpl implements ProductVersionDAO {

	@Override
	public void create(ProductVersionDTO dto) throws EntityCreationException,
			EntityRetrievalException {
		
		ProductVersionEntity entity = null;
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
			
			entity = new ProductVersionEntity();
			entity.setCreationDate(dto.getCreationDate());
			entity.setDeleted(dto.getDeleted());
			entity.setId(dto.getId());
			entity.setProductId(dto.getProductId());
			entity.setVersion(dto.getVersion());
			//entity.setLastModifiedDate(result.getLastModifiedDate());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
			create(entity);	
		}
		
	}

	@Override
	public void update(ProductVersionDTO dto) throws EntityRetrievalException {
		
		ProductVersionEntity entity = this.getEntityById(dto.getId());
		entity.setCreationDate(dto.getCreationDate());
		entity.setDeleted(dto.getDeleted());
		entity.setId(dto.getId());
		entity.setProductId(dto.getProductId());
		entity.setVersion(dto.getVersion());
		//entity.setLastModifiedDate(result.getLastModifiedDate());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
		update(entity);
	}

	@Override
	public void delete(Long id) {
		Query query = entityManager.createQuery("UPDATE ProductVersionEntity SET deleted = true WHERE product_version_id = :entityid");
		query.setParameter("entityid", id);
		query.executeUpdate();
	}

	@Override
	public List<ProductVersionDTO> findAll() {
		
		List<ProductVersionEntity> entities = getAllEntities();
		List<ProductVersionDTO> dtos = new ArrayList<>();
		
		for (ProductVersionEntity entity : entities) {
			ProductVersionDTO dto = new ProductVersionDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public ProductVersionDTO getById(Long id) throws EntityRetrievalException {
		
		ProductVersionEntity entity = getEntityById(id);
		ProductVersionDTO dto = new ProductVersionDTO(entity);
		return dto;
		
	}
	
	
	private void create(ProductVersionEntity entity) {
		
		entityManager.persist(entity);
		
	}
	
	private void update(ProductVersionEntity entity) {
		
		entityManager.merge(entity);	
	
	}
	
	private List<ProductVersionEntity> getAllEntities() {
		
		List<ProductVersionEntity> result = entityManager.createQuery( "from ProductVersionEntity where (NOT deleted = true) ", ProductVersionEntity.class).getResultList();
		return result;
		
	}
	
	private ProductVersionEntity getEntityById(Long id) throws EntityRetrievalException {
		
		ProductVersionEntity entity = null;
			
		Query query = entityManager.createQuery( "from ProductVersionEntity where (NOT deleted = true) AND (product_version_id = :entityid) ", ProductVersionEntity.class );
		query.setParameter("entityid", id);
		List<ProductVersionEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate product version id in database.");
		}
		
		if (result.size() < 0){
			entity = result.get(0);
		}
			
		return entity;
	}
	
}
