package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.entity.VendorEntity;

@Repository("productDAO")
public class ProductDAOImpl extends BaseDAOImpl implements ProductDAO {

	@Override
	@Transactional
	public ProductEntity create(ProductDTO dto) throws EntityCreationException,
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
			entity.setName(dto.getName());
			entity.setReportFileLocation(dto.getReportFileLocation());
			entity.setVendorId(dto.getVendorId());
			
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
	public ProductEntity update(ProductDTO dto) throws EntityRetrievalException {
		ProductEntity entity = this.getEntityById(dto.getId());
		
		entity.setReportFileLocation(dto.getReportFileLocation());
		
		if(dto.getName() != null) {
			entity.setName(dto.getName());
		}
		
		if(dto.getVendorId() != null)
		{
			entity.setVendorId(dto.getVendorId());
		}
				
		if(dto.getDeleted() != null) {
			entity.setDeleted(dto.getDeleted());
		} 
		
		if(dto.getCreationDate() != null) {
			entity.setCreationDate(dto.getCreationDate());
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
		ProductEntity toDelete = getEntityById(id);
		
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			update(toDelete);
		}
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
		if(entity == null) { 
			return null;
		}
		ProductDTO dto = new ProductDTO(entity);
		return dto;
		
	}
	
	public List<ProductDTO> getByVendor(Long vendorId) {		
		Query query = entityManager.createQuery( "from ProductEntity where (NOT deleted = true) AND (vendor_id = :entityid) ", ProductEntity.class );
		query.setParameter("entityid", vendorId);
		List<ProductEntity> results = query.getResultList();
		
		List<ProductDTO> dtoResults = new ArrayList<ProductDTO>();
		for(ProductEntity result : results) {
			dtoResults.add(new ProductDTO(result));
		}
		return dtoResults;
	}
	
	public List<ProductDTO> getByVendors(List<Long> vendorIds) {
		Query query = entityManager.createQuery( "from ProductEntity where (NOT deleted = true) AND vendor_id IN :idList ", ProductEntity.class );
		query.setParameter("idList", vendorIds);
		List<ProductEntity> results = query.getResultList();

		List<ProductDTO> dtoResults = new ArrayList<ProductDTO>();
		for(ProductEntity result : results) {
			dtoResults.add(new ProductDTO(result));
		}
		return dtoResults;
	}
	
	public ProductDTO getByVendorAndName(Long vendorId, String name) {
		Query query = entityManager.createQuery( "from ProductEntity where (NOT deleted = true) AND (vendor_id = :vendorId) and (name = :name)", ProductEntity.class );
		query.setParameter("vendorId", vendorId);
		query.setParameter("name", name);
		List<ProductEntity> results = query.getResultList();
		
		ProductDTO result = null;
		if(results != null && results.size() > 0) {
			result = new ProductDTO(results.get(0));
		}
		return result;
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
		
		if(result == null || result.size() == 0) {
			throw new EntityRetrievalException("No active product with id " + id + " was found in the database.");
		} else if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate product id in database.");
		} else if(result.size() == 1) {
			entity = result.get(0);
		}
		
		return entity;
	}
}
