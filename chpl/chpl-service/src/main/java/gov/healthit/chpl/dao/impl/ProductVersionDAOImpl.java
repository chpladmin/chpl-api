package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.entity.ProductVersionEntity;
import gov.healthit.chpl.entity.VendorEntity;

@Repository("productVersionDAO")
public class ProductVersionDAOImpl extends BaseDAOImpl implements ProductVersionDAO {

	@Override
	public ProductVersionEntity create(ProductVersionDTO dto) throws EntityCreationException,
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
			return entity;
		}
		
	}

	@Override
	public ProductVersionEntity update(ProductVersionDTO dto) throws EntityRetrievalException {
		
		ProductVersionEntity entity = this.getEntityById(dto.getId());
		entity.setCreationDate(dto.getCreationDate());
		entity.setDeleted(dto.getDeleted());
		entity.setId(dto.getId());
		entity.setProductId(dto.getProductId());
		entity.setVersion(dto.getVersion());
		//entity.setLastModifiedDate(result.getLastModifiedDate());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
		update(entity);
		return entity;
	}

	@Override
	public void delete(Long id) throws EntityRetrievalException {
		ProductVersionEntity toDelete = getEntityById(id);
		
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			update(toDelete);
		}
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
	
	@Override
	public List<ProductVersionDTO> getByProductId(Long productId) {
		Query query = entityManager.createQuery( "from ProductVersionEntity where (NOT deleted = true) AND (product_id = :productId)", ProductVersionEntity.class );
		query.setParameter("productId", productId);
		List<ProductVersionEntity> results = query.getResultList();
		
		List<ProductVersionDTO> dtoResults = new ArrayList<ProductVersionDTO>();
		for(ProductVersionEntity result : results) {
			dtoResults.add(new ProductVersionDTO(result));
		}
		return dtoResults;
	}
	
	public List<ProductVersionDTO> getByProductIds(List<Long> productIds) {
		Query query = entityManager.createQuery( "from ProductVersionEntity where (NOT deleted = true) AND product_id IN :idList ", ProductVersionEntity.class );
		query.setParameter("idList", productIds);
		List<ProductVersionEntity> results = query.getResultList();

		List<ProductVersionDTO> dtoResults = new ArrayList<ProductVersionDTO>();
		for(ProductVersionEntity result : results) {
			dtoResults.add(new ProductVersionDTO(result));
		}
		return dtoResults;
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
		} else if (result.size() == 1){
			entity = result.get(0);
		}
			
		return entity;
	}
	
}
