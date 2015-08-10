package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.entity.ProductEntity;

public class ProductDAOImpl extends BaseDAOImpl implements ProductDAO {

	@Override
	public void create(ProductDTO dto) throws EntityCreationException,
			EntityRetrievalException {
		
		ProductEntity entity = null;
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
			
			entity = new ProductEntity();
			entity.setCreationDate(dto.getCreationDate());
			entity.setDeleted(dto.getDeleted());
			entity.setId(dto.getId());
			entity.setName(dto.getName());
			entity.setReportFileLocation(dto.getReportFileLocation());
			entity.setVendorId(dto.getVendorId());
			//entity.setLastModifiedDate(result.getLastModifiedDate());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
			create(entity);	
		}
		
	}

	@Override
	public void update(ProductDTO dto) throws EntityRetrievalException {
		
		ProductEntity entity = this.getEntityById(dto.getId());
		entity.setCreationDate(dto.getCreationDate());
		entity.setDeleted(dto.getDeleted());
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setReportFileLocation(dto.getReportFileLocation());
		entity.setVendorId(dto.getVendorId());
		//entity.setLastModifiedDate(result.getLastModifiedDate());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
		update(entity);
	}

	@Override
	public void delete(Long id) {
		Query query = entityManager.createQuery("UPDATE ProductEntity SET deleted = true WHERE product_id = :entityid");
		query.setParameter("entityid", id);
		query.executeUpdate();
	}

	@Override
	public List<ProductDTO> findAll() {
		
		List<ProductEntity> entities = getAllEntities();
		List<ProductDTO> dtos = new ArrayList<>();
		
		for (ProductEntity entity : entities) {
			ProductDTO dto = new ProductDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public ProductDTO getById(Long id) throws EntityRetrievalException {
		
		ProductEntity entity = getEntityById(id);
		ProductDTO dto = new ProductDTO(entity);
		return dto;
		
	}
	
	
	private void create(ProductEntity entity) {
		
		entityManager.persist(entity);
		
	}
	
	private void update(ProductEntity entity) {
		
		entityManager.merge(entity);	
	
	}
	
	private List<ProductEntity> getAllEntities() {
		
		List<ProductEntity> result = entityManager.createQuery( "from ProductEntity where (NOT deleted = true) ", ProductEntity.class).getResultList();
		return result;
		
	}
	
	private ProductEntity getEntityById(Long id) throws EntityRetrievalException {
		
		ProductEntity entity = null;
			
		Query query = entityManager.createQuery( "from ProductEntity where (NOT deleted = true) AND (product_id = :entityid) ", ProductEntity.class );
		query.setParameter("entityid", id);
		List<ProductEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate product version id in database.");
		}
		
		if (result.size() < 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
}
