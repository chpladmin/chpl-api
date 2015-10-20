package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.ProductManager;

@Service
public class ProductManagerImpl implements ProductManager {

	@Autowired ProductDAO productDao;
	
	@Autowired
	ActivityManager activityManager;
	
	@Override
	@Transactional(readOnly = true)
	public ProductDTO getById(Long id) throws EntityRetrievalException {
		return productDao.getById(id);
	}

	@Override
	@Transactional(readOnly = true) 
	public List<ProductDTO> getAll() {
		return productDao.findAll();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ProductDTO> getByVendor(Long vendorId) {
		return productDao.getByVendor(vendorId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductDTO> getByVendors(List<Long> vendorIds) {
		return productDao.getByVendors(vendorIds);
	}
	
	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public ProductDTO create(ProductDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		ProductDTO result = productDao.create(dto);
		
		String activityMsg = "Product "+dto.getName()+" was created.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, result.getId(), activityMsg, null, result);
		return result;
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public ProductDTO update(ProductDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		ProductEntity result = productDao.update(dto);
		ProductDTO afterDto = new ProductDTO(result);
		
		String activityMsg = "Product "+dto.getName()+" was updated.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, result.getId(), activityMsg, dto, afterDto);
		return new ProductDTO(result);
		
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public void delete(ProductDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		delete(dto.getId());
		String activityMsg = "Product "+dto.getName()+" was deleted.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, dto.getId(), activityMsg, dto, null);
	
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public void delete(Long productId) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		ProductDTO toDelete = productDao.getById(productId);
		productDao.delete(productId);
		String activityMsg = "Product "+productId.toString()+" was deleted.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, productId, activityMsg, toDelete , null);
		
	}
	
}
